package org.moflon;

import java.net.URL;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

public class Activator extends Plugin
{

   public static final String PLUGIN_ID = "org.moflon.dependencies";

   // The shared instance
   private static Activator plugin;

   // Singleton instance
   public static Activator getDefault()
   {
      return plugin;
   }

   /**
    * Used to retrieve resources embedded in the plugin (jar files when installed on client machine).
    * 
    * @param filePath
    *           Must be relative to the plugin root and indicate an existing resource (packaged in build)
    * @param PLUGIN_ID
    *           The id of the plugin bundle to be searched
    * @return URL to the resource or null if nothing was found (URL because resource could be inside a jar).
    */
   public static URL getPathRelToPlugIn(String filePath, String PLUGIN_ID)
   {
      try
      {
         return FileLocator.resolve(Platform.getBundle(PLUGIN_ID).getEntry(filePath));
      } catch (Exception e)
      {
         e.printStackTrace();
         return null;
      }
   }

   @Override
   public void start(BundleContext context) throws Exception
   {
      super.start(context);
      plugin = this;
   }

   @Override
   public void stop(BundleContext context) throws Exception
   {
      plugin = null;
      super.stop(context);
   }

   public static void throwCoreExceptionAsError(String message, String plugin, Exception lowLevelException) throws CoreException
   {
      IStatus status = new Status(IStatus.ERROR, plugin, IStatus.OK, message, lowLevelException);
      throw new CoreException(status);
   }

   public static String displayExceptionAsString(Exception e)
   {
      return "Cause: " + ExceptionUtils.getRootCauseMessage(e) + "\n StackTrace: " + ExceptionUtils.getRootCauseStackTrace(e);
   }

}
