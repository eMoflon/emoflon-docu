package org.moflon.util;

import java.net.URL;

import org.moflon.MoflonUtilitiesActivator;

public final class AntlrUtil
{
   private AntlrUtil()
   {
      throw new UtilityClassNotInstantiableException();
   }

   public static URL getAntrlPathUrl()
   {
      final URL url = MoflonUtilitiesActivator.getPathRelToPlugIn(WorkspaceHelper.ANTLR_3, MoflonUtilitiesActivator.PLUGIN_ID);
      if (url == null)
      {
         throw new IllegalStateException(String.format("Could not find Antlr at expected location [path=%s, plugin=%s]", WorkspaceHelper.ANTLR_3,
               MoflonUtilitiesActivator.PLUGIN_ID));
      }
      return url;
   }
}
