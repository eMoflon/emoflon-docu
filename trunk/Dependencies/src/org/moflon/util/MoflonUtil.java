package org.moflon.util;

import java.awt.geom.GeneralPath;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EcorePackage;

/**
 * A collection of useful helper methods.
 * 
 */
public class MoflonUtil
{

   /**
    * Marker for code passages generated through eMoflon/EMF that are eligible for 
    * extracting injections.
    */
   public static final String EOPERATION_MODEL_COMMENT = "// [user code injected with eMoflon]";

   /**
    * Code corresponding to the default implementation of a java method. Is used, when no SDM implementation could be
    * retrieved.
    */
   public final static String DEFAULT_METHOD_BODY = "\n" + EOPERATION_MODEL_COMMENT
         + "\n\n// TODO: implement this method here but do not remove the injection marker \nthrow new UnsupportedOperationException();";

   public static String getMoflonDefaultURIForProject(final String projectName)
   {
      return "http://www.moflon.org." + projectName;
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
    * Derive the java data type of a given Ecore data type. All numerical data types (e.g. byte, int etc.) are converted
    * to the Java class "Number".
    * 
    * @param eCoreType
    *           the name of the Ecore data type class (e.g. EString)
    * @return the name of the java type class (e.g. String)
    */
   public static String eCoreTypeToJavaType(final String eCoreType, final boolean numericalToNumber) throws IllegalArgumentException
   {
      String javaType = "";
      List<String> primitiveNumberTypes = Arrays.asList(new String[] { "byte", "short", "int", "long", "float", "double" });

      // Derive the java data type from the Ecore class name
      try
      {
         javaType = EcorePackage.eINSTANCE.getEClassifier(eCoreType).getInstanceClass().getSimpleName();
      } catch (Exception e)
      {
         System.err.println("Can not derive java data type from the given Ecore data type = " + eCoreType);

         javaType = eCoreType;
      }

      // Convert all numerical data types to Number
      if (numericalToNumber)
         if (primitiveNumberTypes.contains(javaType))
            javaType = "Number";

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
   
   public static String lastSegmentOf(String name)
   {
      int startOfLastSegment = name.lastIndexOf(".");
      
      if(startOfLastSegment == -1)
         startOfLastSegment = 0;
      else
         startOfLastSegment++;
      
      return name.substring(startOfLastSegment);
   }
   
   public static String lastCapitalizedSegmentOf(String name){
      return StringUtils.capitalize(lastSegmentOf(name));
   }
}
