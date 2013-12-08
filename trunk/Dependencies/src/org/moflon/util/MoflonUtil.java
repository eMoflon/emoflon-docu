package org.moflon.util;

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
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EcorePackage;

/**
 * A collection of useful helper methods.
 * 
 * @author anjorin
 * @author (last editor) $Author$
 * @version $Revision$ $Date$
 */
public class MoflonUtil
{
   public static final String MEMBERS_END = "// [user code injected with eMoflon] -->";
   public static final String MEMBERS_BEGIN = "// <-- [user code injected with eMoflon]";
   public static final String IMPORT_END = "// [user defined imports] -->";
   public static final String IMPORT_BEGIN = "// <-- [user defined imports]";
   public static final String MODEL_COMMENT = "// [user code injected with eMoflon]";
   
   
   public static String getMoflonDefaultURIForProject(String projectName){
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
   public static void copyDirToDir(URL sourceDir, File destination, FileFilter filter) throws IOException
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

            if (entry.getName().startsWith(conn.getEntryName()) && 
                  !entry.getName().equals(conn.getEntryName()))
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
	 * All numerical data types (e.g. byte, int etc.) are converted to the Java class "Number".
	 * 
	 * @param eCoreType the name of the Ecore data type class (e.g. EString)
	 * @return the name of the java type class (e.g. String)
	 */
	public static String eCoreTypeToJavaType(String eCoreType, boolean numericalToNumber) throws IllegalArgumentException 
	{
		String javaType = "";
		List<String> primitiveNumberTypes = Arrays.asList(new String[] {
				"byte", "short", "int", "long", "float", "double" });
	   
		// Derive the java data type from the Ecore class name 
		try {
			javaType = EcorePackage.eINSTANCE.getEClassifier(eCoreType).getInstanceClass().getSimpleName();
		} catch (Exception e) {
			System.err.println("Can not derive java data type from the given Ecore data type = " + eCoreType);
			
			javaType = eCoreType;
		}
	 
		// Convert all numerical data types to Number
		if(numericalToNumber)
			if (primitiveNumberTypes.contains(javaType))
				javaType = "Number";
		   
		return javaType;
	}
	
	/**
	 * Determine fully qualified name of given eclassifier by iterating through package hierarchy.
	 * @param eClass
	 * @return
	 */
	public static String getFQN(ENamedElement eClass) {
		String fqn = eClass.getName();

		ENamedElement e = eClass;

		while (e.eContainer() != null) {
			e = (ENamedElement) e.eContainer();
			fqn = e.getName() + "." + fqn;
		}

		return fqn;
	}
}
