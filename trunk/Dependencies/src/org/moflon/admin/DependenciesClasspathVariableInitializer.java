package org.moflon.admin;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ClasspathVariableInitializer;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.moflon.Activator;

public class DependenciesClasspathVariableInitializer extends ClasspathVariableInitializer
{

   @Override
   public void initialize(String variable)
   {
      URL urlPath = Activator.getDefault().getPathRelToPlugIn("/", Activator.PLUGIN_ID);

      String path = urlPath.getPath();

      if ("jar".equals(urlPath.getProtocol()))
      {
         // Normalize string representation of URL: i.e. extract path from "jar:<path>!"
         path = path.substring(path.indexOf(':') + 1, path.indexOf('!'));
      } else
      {
         // Eclipse has been spawned from a host eclipse so add /bin
         path = path + File.separator + "bin";
      }

      try
      {
         JavaCore.setClasspathVariable(variable, new Path(path), null);
      } catch (JavaModelException e)
      {
         e.printStackTrace();
      }
   }

}
