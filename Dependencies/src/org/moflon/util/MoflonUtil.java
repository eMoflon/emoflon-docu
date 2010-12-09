package org.moflon.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
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
    * Set up logging globally
    * 
    * @param configFile
    *           Absolute path to log4j property configuration file
    * @param logFile
    *           Absolute path to log file
    * @return false if unable to setup and configure logging
    */
   public static boolean configureLogging(String configFile, String logFile)
   {
      URL configFileURL;
      try
      {
         configFileURL = new URL(configFile);
         return configureLogging(configFileURL, logFile);
      } catch (MalformedURLException e)
      {
         e.printStackTrace();
         return false;
      }
   }

   /**
    * Copies contents of file named from to file named to
    * 
    * @param from
    *           File whose contents are to be copied
    * @param to
    *           File to which contents are copied to
    * @throws IOException
    */
   public static void copyFileToFile(File from, File to) throws IOException
   {
      FileReader in = new FileReader(from);
      FileWriter out = new FileWriter(to);

      int c;
      while ((c = in.read()) != -1)
         out.write(c);

      in.close();
      out.close();
   }

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
         JarFile jar = new JarFile(sourceDir.getFile());
         destination.mkdir();
         Enumeration<? extends JarEntry> entries = jar.entries();
         while (entries.hasMoreElements())
         {
            JarEntry entry = entries.nextElement();
            if (entry.isDirectory())
            {
               // Handle directory
               File dir = new File(destination, entry.getName());
               if (filter.accept(dir))
                  dir.mkdir();
            } else
            {
               // Handle file
               copyFileToFile(new File(entry.getName()), new File(destination, entry.getName()));
            }
         }
      }
   }
}
