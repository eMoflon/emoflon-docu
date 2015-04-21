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

public class MoflonDependenciesActivator extends Plugin
{
   public final static String PLUGIN_ID = "org.moflon.dependencies";

   // The shared instance
   private static MoflonDependenciesActivator plugin;

   // Singleton instance
   public static MoflonDependenciesActivator getDefault()
   {
      return plugin;
   }

   /**
    * Used to retrieve resources embedded in the plugin (jar files when installed on client machine).
    * 
    * @param filePath
    *           Must be relative to the plugin root and indicate an existing resource (packaged in build)
    * @param pluginId
    *           The id of the plugin bundle to be searched
    * @return URL to the resource or null if nothing was found (URL because resource could be inside a jar).
    */
   public static URL getPathRelToPlugIn(final String filePath, final String pluginId)
   {
      try
      {
         return FileLocator.resolve(Platform.getBundle(pluginId).getEntry(filePath));
      } catch (Exception e)
      {
         e.printStackTrace();
         return null;
      }
   }

   @Override
   public void start(final BundleContext context) throws Exception
   {
      super.start(context);
      plugin = this;
   }

   @Override
   public void stop(final BundleContext context) throws Exception
   {
      plugin = null;
      super.stop(context);
   }

   public static void throwCoreExceptionAsError(final String message, final String plugin, final Exception lowLevelException) throws CoreException
   {
      IStatus status = new Status(IStatus.ERROR, plugin, IStatus.OK, message, lowLevelException);
      throw new CoreException(status);
   }

   public static String displayExceptionAsString(final Exception e)
   {
      try
      {
         final String message;
         if (null == e.getCause())
         {
            message = "Cause: " + ExceptionUtils.getRootCauseMessage(e) + "\n StackTrace: " + ExceptionUtils.getStackTrace(ExceptionUtils.getRootCause(e));
         } else
         {
            message = "Reason: " + e.getMessage();
         }
         return message;
      } catch (Exception new_e)
      {
         return e.getMessage();
      }
   }
}
