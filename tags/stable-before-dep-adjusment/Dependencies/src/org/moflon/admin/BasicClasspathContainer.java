package org.moflon.admin;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public abstract class BasicClasspathContainer implements IClasspathContainer
{
   private IPath path;
   
   public BasicClasspathContainer(IPath containerPath, IJavaProject project)
   {
      path = containerPath;
   }
   
   protected IClasspathEntry getEntryFor(URL urlPath)
   {
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
      
      return JavaCore.newLibraryEntry(new Path(path), new Path(srcPath), new Path("/"));
   }


   @Override
   public int getKind()
   {
     return IClasspathContainer.K_APPLICATION;
   }

   @Override
   public IPath getPath()
   {
      return path;
   }

   public boolean isValid()
   {
      return true;
   }

}
