package org.moflon.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;

/**
 * A collection of useful helper methods.
 * 
 * @author anjorin
 * @author (last editor) $Author$
 * @version $Revision$ $Date$
 */
public class MoflonUtil
{
   public static String getMoflonDefaultURIForProject(String projectName){
      return "http://www.moflon.org." + projectName;
   }
   
   
   /**
    * Set up logging globally
    * 
    * @param configFile
    *           URL to log4j property configuration file
    * @param logFile
    *           Absolute path to log file
    * @return false if unable to setup and configure logging
    */
   public static boolean configureLogging(URL configFile, String logFile)
   {
      try
      {
         Logger root = Logger.getRootLogger();
         String configurationStatus = "";
         if (configFile != null)
         {
            // Configure system using config
            PropertyConfigurator.configure(configFile);
            configurationStatus = "Log4j successfully configured using " + configFile;
         } else
         {
            configurationStatus = "Set up logging without config file!";
         }

         // Set format and scheme for output in logfile
         PatternLayout layout = new PatternLayout("%d %5p [%c{2}::%L] - %m%n");
         DailyRollingFileAppender fileAppender;
         fileAppender = new DailyRollingFileAppender(layout, logFile, "'.'yyyy-MM-dd");
         root.addAppender(fileAppender);

         // Indicate success
         root.info(configurationStatus);
         root.info("Logging to: " + logFile + "\n\n");
         return true;
      } catch (Exception e)
      {
         e.printStackTrace();
         return false;
      }
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
}
