package org.moflon.util.plugins;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.moflon.util.WorkspaceHelper;

public class ManifestFileBuilder {

	public enum AttributeUpdatePolicy {
		FORCE, KEEP;
	}

	// TODO@rkluge: is this used anywhere?
	// This means that the dependency is not available as a plugin -> the user
	// must manipulate the projects buildpath manually!
	public static final Object IGNORE_PLUGIN_ID = "__ignore__";

	public void manipulateManifest(final IProject project, final Function<Manifest, Boolean> consumer) {
		try {
			IFile manifestFile = WorkspaceHelper.getManifestFile(project);
			Manifest manifest = new Manifest();

			if (manifestFile.exists()) {
				readManifestFile(manifestFile, manifest);
			}

			final boolean hasManifestChanged = consumer.apply(manifest);

			if (hasManifestChanged) {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();

				manifest.write(stream);
				String formattedManifestString = prettyPrintManifest(stream.toString());
				if (!manifestFile.exists()) {
					WorkspaceHelper.addAllFoldersAndFile(project, manifestFile.getProjectRelativePath(),
							formattedManifestString, new NullProgressMonitor());
				} else {
					manifestFile.setContents(new ByteArrayInputStream(formattedManifestString.getBytes()), true, true,
							new NullProgressMonitor());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private String prettyPrintManifest(final String string) {
		return new ManifestPrettyPrinter().print(string);
	}

	//TODO@rkluge: is there a lock that is kept in case of an exception?
	private void readManifestFile(final IFile manifestFile, final Manifest manifest) throws CoreException {
		try {
			manifest.read(manifestFile.getContents());
		} catch (IOException e) {
			//TODO@rkluge use constant when moved
			throw new CoreException(new Status(IStatus.ERROR, "org.moflon.ide.core",
					"Failed to read existing MANIFEST.MF: " + e.getMessage(), e));
		}
	}

	/**
	 * Updates the given attribute in the manifest.
	 * 
	 * @return whether the value of the attribute changed
	 */
	public static boolean updateAttribute(final Manifest manifest, final Name attribute, final String value,
			final AttributeUpdatePolicy updatePolicy) {
		Attributes attributes = manifest.getMainAttributes();
		if ((!attributes.containsKey(attribute) || attributes.get(attribute) == null //
		|| attributes.get(attribute).equals("null")) //
				|| (attributes.containsKey(attribute) && updatePolicy == AttributeUpdatePolicy.FORCE)) {
			Object previousValue = attributes.get(attributes);
			if (!value.equals(previousValue)) {
				attributes.put(attribute, value);
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * Updates the manifest (if necessary) to contain the given dependencies.
	 * 
	 * @param newDependencies
	 *            the dependencies to be added (if not present yet)
	 * @return whether the manifest was changed
	 */
	public static boolean updateDependencies(final Manifest manifest, final List<String> newDependencies) {
		final Set<String> missingNewDependencies = new HashSet<>(newDependencies);

		final String currentDependencies = (String) manifest.getMainAttributes().get(
				PluginManifestConstants.REQUIRE_BUNDLE);
		final List<String> dependencies = ManifestFileBuilder.extractDependencies(currentDependencies);

		dependencies.forEach(existingDependency -> missingNewDependencies.remove(extractPluginId(existingDependency)));

		if (!newDependencies.isEmpty()) {
			for (final String newDependency : newDependencies) {
				if (missingNewDependencies.contains(newDependency) && !newDependency.equals(IGNORE_PLUGIN_ID)) {
					dependencies.add(newDependency);
				}
			}

			addDependenciesToManifest(manifest, dependencies);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns the plugin Id for a given dependency entry, which may contain
	 * additional metadata, e.g.
	 *
	 * org.moflon.ide.core;bundle-version="1.0.0"
	 */
	public static String extractPluginId(final String existingDependency) {
		int indexOfSemicolon = existingDependency.indexOf(";");
		if (indexOfSemicolon > 0) {
			return existingDependency.substring(0, indexOfSemicolon);
		} else {
			return existingDependency;
		}
	}

	private static void addDependenciesToManifest(final Manifest manifest, final List<String> dependencies) {
		String dependenciesString = ManifestFileBuilder.createDependenciesString(dependencies);

		if (!dependenciesString.matches("\\s*")) {
			manifest.getMainAttributes().put(PluginManifestConstants.REQUIRE_BUNDLE, dependenciesString);
		}
	}

	private static String createDependenciesString(final List<String> dependencies) {
		return dependencies.stream().filter(dep -> !dep.equals("")).collect(Collectors.joining(","));
	}

	/**
	 * Extracts the dependencies from the given properties. The dependencies are
	 * used as in
	 */
	public static List<String> extractDependencies(final String dependenciesFromMetamodelProperties) {
		List<String> extractedDependencies = new ArrayList<>();
		if (dependenciesFromMetamodelProperties != null && !dependenciesFromMetamodelProperties.isEmpty()) {
			extractedDependencies.addAll(Arrays.asList(dependenciesFromMetamodelProperties.split(",")));
		}

		return extractedDependencies;
	}

}
