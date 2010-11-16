package org.moflon.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;

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
}
