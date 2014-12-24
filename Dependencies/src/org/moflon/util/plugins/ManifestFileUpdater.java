package org.moflon.util.plugins;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.moflon.MoflonDependenciesPlugin;
import org.moflon.util.WorkspaceHelper;

public class ManifestFileUpdater
{

   public enum AttributeUpdatePolicy {
      FORCE, KEEP;
   }

   /** This means that the dependency is not available as a plugin -> the user must manipulate the projects buildpath manually! **/
   public static final Object IGNORE_PLUGIN_ID = "__ignore__";

   /**
    * Modifies the manifest of the given project.
    * 
    * The method reads the manifest, applies the given function, and, if the function returns true, saves the manifest
    * again.
    * 
    * @param consumer
    *           A function that returns whether it has modified the manifest.
    * @throws CoreException 
    * @throws IOException 
    */
   public void processManifest(final IProject project, final Function<Manifest, Boolean> consumer) throws CoreException, IOException
   {
         IFile manifestFile = WorkspaceHelper.getManifestFile(project);
         Manifest manifest = new Manifest();

         if (manifestFile.exists())
         {
            readManifestFile(manifestFile, manifest);
         }

         final boolean hasManifestChanged = consumer.apply(manifest);

         if (hasManifestChanged)
         {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            new ManifestWriter().write(manifest, stream);
            String formattedManifestString = prettyPrintManifest(stream.toString());
            if (!manifestFile.exists())
            {
               WorkspaceHelper.addAllFoldersAndFile(project, manifestFile.getProjectRelativePath(), formattedManifestString, new NullProgressMonitor());
            } else
            {
               manifestFile.setContents(new ByteArrayInputStream(formattedManifestString.getBytes()), true, true, new NullProgressMonitor());
            }
         }
   }

   private String prettyPrintManifest(final String string)
   {
      return new ManifestPrettyPrinter().print(string);
   }

   private void readManifestFile(final IFile manifestFile, final Manifest manifest) throws CoreException
   {
      try
      {
         manifest.read(manifestFile.getContents());
      } catch (IOException e)
      {
         throw new CoreException(new Status(IStatus.ERROR, MoflonDependenciesPlugin.PLUGIN_ID, "Failed to read existing MANIFEST.MF: " + e.getMessage(), e));
      }
   }

   /**
    * Updates the given attribute in the manifest.
    * 
    * @return whether the value of the attribute changed
    */
   public static boolean updateAttribute(final Manifest manifest, final Name attribute, final String value, final AttributeUpdatePolicy updatePolicy)
   {
      Attributes attributes = manifest.getMainAttributes();
      if ((!attributes.containsKey(attribute) || attributes.get(attribute) == null //
      || attributes.get(attribute).equals("null")) //
            || (attributes.containsKey(attribute) && updatePolicy == AttributeUpdatePolicy.FORCE))
      {
         Object previousValue = attributes.get(attributes);
         if (!value.equals(previousValue))
         {
            attributes.put(attribute, value);
            return true;
         } else
         {
            return false;
         }
      } else
      {
         return false;
      }
   }

   /**
    * Updates the manifest (if necessary) to contain the given dependencies.
    * 
    * @param newDependencies
    *           the dependencies to be added (if not present yet)
    * @return whether the manifest was changed
    */
   public static boolean updateDependencies(final Manifest manifest, final List<String> newDependencies)
   {
      final Set<String> missingNewDependencies = new HashSet<>(newDependencies);

      final String currentDependencies = (String) manifest.getMainAttributes().get(PluginManifestConstants.REQUIRE_BUNDLE);
      final List<String> dependencies = ManifestFileUpdater.extractDependencies(currentDependencies);

      dependencies.forEach(existingDependency -> missingNewDependencies.remove(extractPluginId(existingDependency)));

      if (!newDependencies.isEmpty())
      {
         for (final String newDependency : newDependencies)
         {
            if (missingNewDependencies.contains(newDependency) && !newDependency.equals(IGNORE_PLUGIN_ID))
            {
               dependencies.add(newDependency);
            }
         }

         addDependenciesToManifest(manifest, dependencies);
         return true;
      } else
      {
         return false;
      }
   }
   
   public Map<String, IProject> extractPluginIDToProjectMap(final Collection<IProject> projects){
      Map<String, IProject> idToProject = new HashMap<>();
      projects.stream().forEach(p -> {
         try{
            processManifest(p, manifest -> {
            idToProject.put(extractPluginId(getID(p,manifest)), p);
            return false;
         });
         } catch(Exception e){
            idToProject.put(p.getName(), p);
         }
      });
      
      return idToProject;
   }

   private String getID(final IProject p, final Manifest manifest)
   {
     return (String)manifest.getMainAttributes().get(PluginManifestConstants.BUNDLE_SYMBOLIC_NAME);
   }
   
   public Collection<String> getDependenciesAsPluginIDs(final IProject project){
      Collection<String> dependencies = new ArrayList<>();
      
      try
      {
         processManifest(project, manifest -> {
            dependencies.addAll(extractDependencies((String) manifest.getMainAttributes().get(PluginManifestConstants.REQUIRE_BUNDLE)));
            return false;
         });
      } catch (Exception e)
      {
         e.printStackTrace();
      }  
      
      return dependencies.stream().map(dep -> extractPluginId(dep)).collect(Collectors.toList());
   }

   /**
    * Returns the plugin Id for a given dependency entry, which may contain additional metadata, e.g.
    *
    * org.moflon.ide.core;bundle-version="1.0.0"
    */
   public static String extractPluginId(final String existingDependency)
   {
      int indexOfSemicolon = existingDependency.indexOf(";");
      if (indexOfSemicolon > 0)
      {
         return existingDependency.substring(0, indexOfSemicolon);
      } else
      {
         return existingDependency;
      }
   }

   private static void addDependenciesToManifest(final Manifest manifest, final List<String> dependencies)
   {
      String dependenciesString = ManifestFileUpdater.createDependenciesString(dependencies);

      if (!dependenciesString.matches("\\s*"))
      {
         manifest.getMainAttributes().put(PluginManifestConstants.REQUIRE_BUNDLE, dependenciesString);
      }
   }

   private static String createDependenciesString(final List<String> dependencies)
   {
      return dependencies.stream().filter(dep -> !dep.equals("")).collect(Collectors.joining(","));
   }

   /**
    * Extracts the dependencies from the given list of properties.
    */
   public static List<String> extractDependencies(final String dependencies)
   {
      List<String> extractedDependencies = new ArrayList<>();
      if (dependencies != null && !dependencies.isEmpty())
      {
         extractedDependencies.addAll(Arrays.asList(dependencies.split(",")));
      }

      return extractedDependencies;
   }

}
