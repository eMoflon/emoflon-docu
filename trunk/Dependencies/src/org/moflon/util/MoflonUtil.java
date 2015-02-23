package org.moflon.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Pair;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.moflon.util.plugins.ManifestFileUpdater;
import org.moflon.util.plugins.PluginManifestConstants;


/**
 * A collection of useful helper methods.
 * 
 */
public class MoflonUtil
{
   private static final Logger logger = Logger.getLogger(MoflonUtil.class);

   /**
    * Marker for code passages generated through eMoflon/EMF that are eligible for extracting injections.
    */
   public static final String EOPERATION_MODEL_COMMENT = "// [user code injected with eMoflon]";

   /**
    * Code corresponding to the default implementation of a java method. Is used, when no SDM implementation could be
    * retrieved.
    */
   public final static String DEFAULT_METHOD_BODY = "\n" + EOPERATION_MODEL_COMMENT
         + "\n\n// TODO: implement this method here but do not remove the injection marker \nthrow new UnsupportedOperationException();";

   public static String getDefaultPathToEcoreFileInProject(final String projectName)
   {
      return getDefaultPathToFileInProject(projectName, ".ecore");
   }

   public static String getDefaultPathToGenModelInProject(final String projectName)
   {
      return getDefaultPathToFileInProject(projectName, ".genmodel");
   }

   public static String getDefaultPathToFileInProject(final String projectName, final String ending)
   {
      return "model/" + MoflonUtil.lastCapitalizedSegmentOf(projectName) + ending;
   }

   public static URI getDefaultURIToEcoreFileInPlugin(final String pluginID)
   {
      return URI.createPlatformPluginURI("/" + pluginID + "/" + getDefaultPathToEcoreFileInProject(pluginID), true);
   }

   /**
    * Copies contents of directory named at sourceDir to directory at destination
    * 
    * @param sourceDir
    *           Directory to be copied. Can be located in a jar or in the local filesystem
    * @param destination
    *           Directory to be created
    * @param filter
    *           Specifies what file/subdirectories are to be ignored
    * @throws IOException
    */
   public static void copyDirToDir(final URL sourceDir, final File destination, final FileFilter filter) throws IOException
   {
      if ("file".equals(sourceDir.getProtocol()))
      {
         // Copy from filesystem
         File dir = new File(sourceDir.getFile());
         FileUtils.copyDirectory(dir, destination, filter);
      } else if ("jar".equals(sourceDir.getProtocol()))
      {
         // Copy from jar
         JarURLConnection conn = (JarURLConnection) sourceDir.openConnection();
         JarFile jar = conn.getJarFile();

         destination.mkdir();
         Enumeration<? extends JarEntry> entries = jar.entries();
         while (entries.hasMoreElements())
         {
            JarEntry entry = entries.nextElement();

            if (entry.getName().startsWith(conn.getEntryName()) && !entry.getName().equals(conn.getEntryName()))
            {
               int beginIndex = entry.getName().indexOf(conn.getEntryName()) + conn.getEntryName().length();
               String childPath = entry.getName().substring(beginIndex);

               if (entry.isDirectory())
               {
                  // Handle directory
                  File dir = new File(destination, childPath);
                  if (filter.accept(dir))
                     dir.mkdir();
               } else
               {
                  // Handle file
                  FileUtils.copyInputStreamToFile(jar.getInputStream(entry), new File(destination, childPath));
               }
            }
         }
      }
   }

   /**
    * Derive the java data type of a given Ecore data type. 
    * 
    * @param eCoreType
    *           the name of the Ecore data type class (e.g. EString)
    * @return the name of the java type class (e.g. String)
    */
   public static String eCoreTypeToJavaType(final String eCoreType) throws IllegalArgumentException
   {
      String javaType = "";

      // Derive the java data type from the Ecore class name
      try
      {
         javaType = EcorePackage.eINSTANCE.getEClassifier(eCoreType).getInstanceClass().getSimpleName();
      } catch (Exception e)
      {
         logger.debug("Can not derive java data type from the given Ecore data type = " + eCoreType);

         javaType = eCoreType;
      }

      return javaType;
   }

   /**
    * Determine fully qualified name of given element by iterating through package hierarchy.
    * 
    * @param ENamedElement
    * @return
    */
   public static String getFQN(final ENamedElement element)
   {
      String fqn = element.getName();

      ENamedElement e = element;

      while (e.eContainer() != null)
      {
         e = (ENamedElement) e.eContainer();
         fqn = e.getName() + "." + fqn;
      }

      return fqn;
   }

   /**
    * Determine fully qualified name of given GenPackage by iterating through package hierarchy.
    * 
    */
   public static String getFQN(final GenPackage genPackage)
   {
      String fqn = genPackage.getPackageName();

      GenPackage p = genPackage;

      while (p.getSuperGenPackage() != null)
      {
         p = p.getSuperGenPackage();
         fqn = p.getPackageName() + "." + fqn;
      }

      return fqn;
   }

   /**
    * This method realizes our convention for determining the package name of a given EPackage.name In the future this
    * could be replaced by using the genmodel.
    * 
    * @param packageName
    * @return
    */
   public static String determinePackageName(final String packageName)
   {
      switch (packageName)
      {
      case "uml":
         return "UMLPackage";

      default:
         return packageName.substring(0, 1).toUpperCase() + packageName.substring(1) + "Package";
      }
   }

   public static boolean toBePluralized(final String roleName, final String packageName, final String typeName, final boolean toOne)
   {
      switch (packageName)
      {
      case "uml":
         // For UML, toMany links are pluralized!
         return !toOne;

      default:
         return false;
      }
   }

   public static String handlePrefixForBooleanAttributes(final String packageName, final String attribute)
   {
      final String is = "is";
      final String prefix = ".is" + StringUtils.capitalize(attribute);

      switch (packageName)
      {
      case "uml":
         // For UML only return prefix if the attribute does not already start with an "is"
         return attribute.startsWith(is) ? "." + attribute : prefix;

      default:
         return prefix;
      }
   }

   public static String lastSegmentOf(final String name)
   {
      int startOfLastSegment = name.lastIndexOf(".");

      if (startOfLastSegment == -1)
         startOfLastSegment = 0;
      else
         startOfLastSegment++;

      return name.substring(startOfLastSegment);
   }

   public static String lastCapitalizedSegmentOf(final String name)
   {
      return StringUtils.capitalize(lastSegmentOf(name));
   }

   public static final void calculatePluginToResourceMap(final ResourceSet set)
   {
      for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects())
         try
         {
            createMapping(set, project);
         } catch (IOException e)
         {
            e.printStackTrace();
         }
   }

   public static void createMapping(final ResourceSet set, final String projectName) throws IOException
   {
      IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
      createMapping(set, project);
   }

   public static void createMapping(final ResourceSet set, final IProject project) throws IOException
   {
      if (project.isAccessible())
      {
         try
         {
            if (project.hasNature(WorkspaceHelper.PLUGIN_NATURE_ID))
            {
               new ManifestFileUpdater().processManifest(project, manifest -> {
                  String pluginId = project.getName();
                  String symbolicName = (String) manifest.getMainAttributes().get(PluginManifestConstants.BUNDLE_SYMBOLIC_NAME);
                  
                  if (symbolicName != null) 
                  {
                     int strip = symbolicName.indexOf(";singleton:=");
                     if(strip != -1)
                        symbolicName = symbolicName.substring(0, symbolicName.indexOf(";singleton:="));
                     
                     pluginId = symbolicName;
                  }
                  else {
                     logger.warn("Unable to extract plugin id from manifest of project " + project.getName() + ". Falling back to project name.");
                  }
                  URI pluginURI = URI.createPlatformPluginURI(pluginId + "/", true);
                  URI resourceURI = URI.createPlatformResourceURI(project.getName() + "/", true);
                  set.getURIConverter().getURIMap().put(pluginURI, resourceURI);
                  logger.debug("Created mapping: " + pluginURI + " -> " + resourceURI);
                  return false;
               });
               
            }
         } catch (CoreException e)
         {
            logger.error("Failed to check nature for project " + project.getName());
         }

      }
   }

   public static final URI lookupProjectURI(final IProject project)
   {
      IPluginModelBase pluginModel = PluginRegistry.findModel(project);
      if (pluginModel != null)
      {
         String pluginID = project.getName();

         if (pluginModel.getBundleDescription() != null)
            pluginID = pluginModel.getBundleDescription().getSymbolicName();

         // Plugin projects in the workspace
         return URI.createPlatformPluginURI(pluginID + "/", true);
      } else
      {
         // Regular projects in the workspace
         return URI.createPlatformResourceURI(project.getName() + "/", true);
      }
   }

   public static final boolean isAPluginDependency(final IProject project, final IProject dependency)
   {
      IPluginModelBase pluginModel = PluginRegistry.findModel(project);
      IPluginModelBase dependencyPlugin = PluginRegistry.findModel(dependency);

      String pluginID = dependency.getName();

      if (pluginModel == null || dependencyPlugin == null)
         return false;

      if (dependencyPlugin.getBundleDescription() != null)
         pluginID = dependencyPlugin.getBundleDescription().getName();

      if (pluginModel.getBundleDescription() == null)
         return false;

      for (BundleSpecification spec : pluginModel.getBundleDescription().getRequiredBundles())
      {
         if (spec.getName().equals(pluginID))
            return true;
      }

      return false;
   }

   public static String correctPathWithImportMappings(String typePath, Map<String, String> importMappings)
   {
      // Break path up into all segments
      List<String> segments = Arrays.asList(typePath.split(Pattern.quote(".")));
      Optional<String> EMPTY_OPTION = Optional.<String> empty();
      Pair<Optional<String>, Optional<String>> EMPTY_PAIR = Pair.of(EMPTY_OPTION, EMPTY_OPTION);

      // Check all possible segment prefixes for fitting entry in import mappings 
      return segments.stream().reduce(EMPTY_PAIR, (oldPair, newSegment) -> {
         if (oldPair.right.isPresent())
            return oldPair;
         else
         {
            String oldPrefix = oldPair.left.isPresent() ? oldPair.left.get() + "." : "";
            String newPrefix = oldPrefix + newSegment;

            if (importMappings.containsKey(newPrefix))
               return Pair.of(Optional.of(newPrefix), Optional.of(importMappings.get(newPrefix)));
            else
               return Pair.of(Optional.of(newPrefix), EMPTY_OPTION);
         }
      }, (p1, p2) -> Pair.of(p1.left.isPresent() ? p1.left : p2.left, p1.right.isPresent() ? p1.right : p2.right)).right.orElse(typePath);
   }
}
