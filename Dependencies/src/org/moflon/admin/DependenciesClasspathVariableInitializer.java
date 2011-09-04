package org.moflon.admin;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ClasspathVariableInitializer;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.moflon.Activator;

public class DependenciesClasspathVariableInitializer extends BasicClasspathVariableInitializer
{
   @Override
   protected URL getURLToVariable()
   {
     return Activator.getDefault().getPathRelToPlugIn("/", Activator.PLUGIN_ID);
   }

}
