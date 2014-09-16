package org.moflon.util;

import java.net.URL;

import org.moflon.Activator;

public final class AntlrUtil
{
   private AntlrUtil()
   {
      throw new UtilityClassNotInstantiableException();
   }

   public static URL getAntrlPathUrl()
   {
      final URL url = Activator.getPathRelToPlugIn(WorkspaceHelper.ANTLR_3, WorkspaceHelper.PLUGIN_ID_MOFLON_DEPENDENCIES);
      if (url == null)
      {
         throw new IllegalStateException(String.format("Could not find Antlr at expected location [path=%s, plugin=%s]", WorkspaceHelper.ANTLR_3,
               WorkspaceHelper.PLUGIN_ID_MOFLON_DEPENDENCIES));
      }
      return url;
   }
}
