package org.moflon.admin;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ClasspathVariableInitializer;
import org.eclipse.jdt.core.JavaCore;

public abstract class BasicClasspathVariableInitializer extends ClasspathVariableInitializer
{

   private static final Logger logger = Logger.getLogger(BasicClasspathVariableInitializer.class);

   protected abstract URL getURLToVariable() throws IOException;

   @Override
   public void initialize(String variable)
   {
      logger.debug("Initializing " + variable);

      try
      {
         URL urlPath = getURLToVariable();
         String path = urlPath.getPath();
         String srcPath = path;

         if ("jar".equals(urlPath.getProtocol()))
         {
            // Normalize string representation of URL: i.e. extract path from "jar:<path>!"
            path = path.substring(path.indexOf(':') + 1, path.indexOf('!'));
            srcPath = path;
         } else if (!path.endsWith(".jar"))
         {
            // Eclipse has been spawned from a host eclipse so add /bin
            path = path + File.separator + "bin";
         }

         logger.debug("Gotten path: " + path);

         JavaCore.setClasspathVariable(variable, new Path(path), null);
         JavaCore.setClasspathVariable(variable + "_SRC", new Path(srcPath), null);
      } catch (Exception e)
      {
         logger.error("Unable to set classpath variable: " + variable);
         e.printStackTrace();
      }
   }
}
