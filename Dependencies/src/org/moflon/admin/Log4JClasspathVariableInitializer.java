package org.moflon.admin;

import java.net.URL;

import org.moflon.Activator;

public class Log4JClasspathVariableInitializer extends BasicClasspathVariableInitializer
{
   @Override
   protected URL getURLToVariable()
   {
      return Activator.getDefault().getPathRelToPlugIn("/lib/log4j.jar", Activator.PLUGIN_ID);
   }
   
}
