package org.moflon.admin;

import java.net.URL;

import org.moflon.Activator;

public class ANTLRClasspathVariableInitializer extends BasicClasspathVariableInitializer
{
   @Override
   protected URL getURLToVariable()
   {
      return Activator.getDefault().getPathRelToPlugIn("/lib/antlr-3.3-complete.jar", Activator.PLUGIN_ID);
   }

}
