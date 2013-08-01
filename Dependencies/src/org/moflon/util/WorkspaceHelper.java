package org.moflon.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.emf.codegen.util.CodeGenUtil;
import org.eclipse.emf.common.util.UniqueEList;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.wizards.JavaCapabilityConfigurationPage;
import org.eclipse.ui.PlatformUI;

/**
 * A collection of useful helper methods when dealing with a workspace in an eclipse plugin.
 * 
 * @author anjorin
 * @author (last editor) $Author: mwieber $
 * @version $Revision: 1995 $ $Date: 2013-06-25 15:58:55 +0200 (Di, 25 Jun 2013) $
 */
public class WorkspaceHelper
{
   private static final Logger logger = Logger.getLogger(WorkspaceHelper.class);

   public final static String SEPARATOR = "/"; 
   
   public final static String MODEL_FOLDER = "model";
   
   public final static String DEBUG_FOLDER = "debug";

   public final static String TEMP_FOLDER = ".temp";

   public final static int PROGRESS_SCALE = 1000;

   public static final String ECORE_FILE_EXTENSION = ".ecore";
   
   public static final String TGG_FILE_EXTENSION = ".tgg.xmi";

   public static final String INSTANCES_FOLDER = "instances";

   public static final String LIB_FOLDER = "lib";
   
   public static final String GEN_FOLDER = "gen";

   public static final String GEN_MODEL_EXT = ".genmodel";

   public static final String MOCA_CONTAINER = "org.moflon.moca.MOCA_CONTAINER";

   public static final QualifiedName EMOFLON_SUPPORT = new QualifiedName("", "EMOFLON_SUPPORT");

   public static final String MOFLON_CONTAINER =  "org.moflon.ide.MOFLON_CONTAINER";

   public static final String TIE_CONTAINER = "org.moflon.tie.TIE_CONTAINER";

   public static final String MOSL_CONTAINER = "org.moflon.ide.MOSL";

   /**
    * Checks if given name is a valid name for a new project in the current workspace.
    * 
    * @param projectName
    *           Name of project to be created in current workspace
    * @param pluginId
    *           ID of bundle
    * @return A status object indicating success or failure and a relevant message.
    */
   public static IStatus validateProjectName(String projectName, String pluginId)
   {
      // Check if anything was entered at all
      if (projectName.length() == 0)
         return new Status(IStatus.ERROR, pluginId, "Name must be specified");

      // Check if name is a valid path for current platform
      IStatus validity = ResourcesPlugin.getWorkspace().validateName(projectName, IResource.PROJECT);
      if (!validity.isOK())
         return new Status(IStatus.ERROR, pluginId, validity.getMessage());

      // Check if no other project with the same name already exists in workspace
      IProject[] workspaceProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
      for (IProject project : workspaceProjects)
      {
         if (project.getName().equals(projectName))
         {
            return new Status(IStatus.ERROR, pluginId, "A project with this name exists already.");
         }
      }

      // Everything was fine
      return new Status(IStatus.OK, pluginId, "Project name is valid");
   }

   /**
    * Creates a new project in current workspace
    * 
    * @param projectName
    *           name of the new project
    * @param monitor
    *           a progress monitor, or null if progress reporting is not desired
    * @return handle to newly created project
    * @throws CoreException
    */
   public static IProject createProject(String projectName, String pluginId, IProgressMonitor monitor) throws CoreException
   {
      monitor.beginTask("", 2 * PROGRESS_SCALE);

      // Get project handle
      IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
      IProject newProject = root.getProject(projectName);

      // Use default location (in workspace)
      final IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(newProject.getName());
      description.setLocation(null);

      // Complain if project already exists
      if (newProject.exists())
      {
         throw new CoreException(new Status(IStatus.ERROR, pluginId, projectName + " exists already!"));
      }

      // Create project
      newProject.create(description, new SubProgressMonitor(monitor, 1 * PROGRESS_SCALE));
      newProject.open(new SubProgressMonitor(monitor, 1 * PROGRESS_SCALE));

      monitor.done();

      return newProject;
   }

   /**
    * Adds a new folder with name 'folderName' to project
    * 
    * @param project
    *           the project on which the folder will be added
    * @param folderName
    *           name of the new folder
    * @param monitor
    *           a progress monitor, or null if progress reporting is not desired
    * @return newly created folder
    * @throws CoreException
    */
   public static IFolder addFolder(IProject project, String folderName, IProgressMonitor monitor) throws CoreException
   {
      monitor.beginTask("", 1 * PROGRESS_SCALE);

      IFolder projFolder = project.getFolder(folderName);
      if(!projFolder.exists())
         projFolder.create(true, true, new SubProgressMonitor(monitor, 1 * PROGRESS_SCALE));
      monitor.done();
      return projFolder;
   }

   /**
    * Adds a file to project root, retrieving its contents from the specified location
    * 
    * @param project
    *           the project to which the file will be added
    * @param fileName
    *           name of the new file relative to the project
    * @param pathToContent
    *           path to a file relative to the project, that contains the contents of the new file to be created
    * @param pluginID
    *           id of plugin that is adding the file
    * @param monitor
    *           a progress monitor, or null if progress reporting is not desired
    * @throws CoreException
    * @throws URISyntaxException
    * @throws IOException
    */
   public static void addFile(IProject project, String fileName, URL pathToContent, String pluginID, IProgressMonitor monitor) throws CoreException,
         URISyntaxException, IOException
   {
      monitor.beginTask("", 1 * PROGRESS_SCALE);

      IFile projectFile = project.getFile(fileName);
      InputStream contents = pathToContent.openStream();
      projectFile.create(contents, true, new SubProgressMonitor(monitor, 1 * PROGRESS_SCALE));

      monitor.done();
   }

   /**
    * Adds a file to project root, containing specified contents as a string
    * 
    * @param project
    *           Name of project the file should be added to
    * @param fileName
    *           Name of file to add to project
    * @param contents
    *           What the file should contain as a String
    * @param monitor
    *           Monitor to indicate progress
    * @throws CoreException
    */
   public static void addFile(IProject project, String fileName, String contents, IProgressMonitor monitor) throws CoreException
   {
      monitor.beginTask("", 1 * PROGRESS_SCALE);
      IFile projectFile = project.getFile(fileName);
      ByteArrayInputStream source = new ByteArrayInputStream(contents.getBytes());
      projectFile.create(source, true, new SubProgressMonitor(monitor, 1 * PROGRESS_SCALE));
      monitor.done();
   }

   /**
    * Sets the folderNames as source-folders in java build path of java project javaProject
    * 
    * @param javaProject
    *           java project whose build path will be modified
    * @param folderNames
    *           source folder names to add to build path of project javaProject
    * @param monitor
    *           a progress monitor, or null if progress reporting is not desired
    * @throws JavaModelException
    */
   public static void setAsSourceFolderInBuildpath(IJavaProject javaProject, IFolder[] folderNames, IProgressMonitor monitor) throws JavaModelException
   {
      monitor.beginTask("", 2 * PROGRESS_SCALE);

      // Get current entries on the classpath
      Collection<IClasspathEntry> entries = new HashSet<IClasspathEntry>();
      for (IClasspathEntry entry : javaProject.getRawClasspath())
         entries.add(entry);

      // Add new entries for the classpath
      if (folderNames != null)
      {
         for (IFolder folder : folderNames)
         {
            if (folder != null)
            {
               IClasspathEntry prjEntry = JavaCore.newSourceEntry(folder.getFullPath());
               entries.add(prjEntry);
            }
         }
      }
      monitor.worked(1 * PROGRESS_SCALE);

      // Fill new classpath (must be an array)
      IClasspathEntry[] newEntries = new IClasspathEntry[entries.size()];
      entries.toArray(newEntries);

      // Set new classpath with added entries
      javaProject.setRawClasspath(newEntries, new SubProgressMonitor(monitor, 1 * PROGRESS_SCALE));

      monitor.done();
   }

   /**
    * Adds natureId to project
    * 
    * @param project
    *           Handle to existing project
    * @param natureId
    *           ID of nature to be added
    * @param monitor
    *           a progress monitor, or null if progress reporting is not desired
    * @throws CoreException
    *            if unable to add nature
    */
   public static void addNature(IProject project, String natureId, IProgressMonitor monitor) throws CoreException
   {
      monitor.beginTask("", 2 * PROGRESS_SCALE);

      // Get existing natures
      IProjectDescription description = project.getDescription();
      String[] natures = description.getNatureIds();

      // Postpend our new nature
      String[] newNatures = new String[natures.length + 1];
      System.arraycopy(natures, 0, newNatures, 0, natures.length);
      newNatures[natures.length] = natureId;
      monitor.worked(1 * PROGRESS_SCALE);

      // Set new list of natures
      description.setNatureIds(newNatures);
      project.setDescription(description, new SubProgressMonitor(monitor, 1 * PROGRESS_SCALE));

      monitor.done();
   }

   public static void setJarAsLibOnBuildpath(IJavaProject javaProject, IFile jar, IProgressMonitor monitor) throws JavaModelException
   {
      monitor.beginTask("", 2 * PROGRESS_SCALE);

      // Get current entries on the classpath
      LinkedList<IClasspathEntry> entries = new LinkedList<IClasspathEntry>();
      for (IClasspathEntry entry : javaProject.getRawClasspath())
         entries.add(entry);

      // Add new entry for the classpath
      if (jar != null)
      {
         IClasspathEntry libEntry = JavaCore.newLibraryEntry(jar.getFullPath(), null, null);
         entries.add(libEntry);
      }

      // Fill new classpath (must be an array)
      IClasspathEntry[] newEntries = new IClasspathEntry[entries.size()];
      entries.toArray(newEntries);

      monitor.worked(1 * PROGRESS_SCALE);

      // Set new classpath with added entries
      javaProject.setRawClasspath(newEntries, new SubProgressMonitor(monitor, 1 * PROGRESS_SCALE));

      monitor.done();
   }

   public static void setProjectOnBuildpath(IJavaProject javaProject, IJavaProject dependency, IProgressMonitor monitor) throws JavaModelException
   {
      monitor.beginTask("", 2 * PROGRESS_SCALE);

      // Get current entries on the classpath
      Collection<IClasspathEntry> classpathEntries = new ArrayList<IClasspathEntry>();
      Collection<IPath> pathsForProjects = new HashSet<IPath>();
      
      // Collect project paths in Set to get rid of duplicates
      for (IClasspathEntry entry : javaProject.getRawClasspath()){
         if(entry.getEntryKind() == IClasspathEntry.CPE_PROJECT)
            pathsForProjects.add(entry.getPath());
         else
            classpathEntries.add(entry);
      }

      // Add new project path to set of project paths 
      if (dependency != null)
      {
         IClasspathEntry projectEntry = JavaCore.newProjectEntry(dependency.getPath());
         pathsForProjects.add(projectEntry.getPath());
      }

      // Add set projects to classpath entries
      for (IPath iPath : pathsForProjects)
         classpathEntries.add(JavaCore.newProjectEntry(iPath));
      
      // Create new buildpath
      IClasspathEntry[] newEntries = new IClasspathEntry[classpathEntries.size()];
      classpathEntries.toArray(newEntries);

      monitor.worked(1 * PROGRESS_SCALE);

      // Set new classpath with added entries
      javaProject.setRawClasspath(newEntries, new SubProgressMonitor(monitor, 1 * PROGRESS_SCALE));

      monitor.done();
   }

   /**
    * Set up the project to a consistent java project
    * 
    * @param project
    *           project to set up as java project
    * @param monitor
    *           a progress monitor, or null if progress reporting is not desired
    * @return
    */
   public static IJavaProject setUpAsJavaProject(IProject project, final IProgressMonitor monitor)
   {
      monitor.beginTask("", 1 * PROGRESS_SCALE);

      final JavaCapabilityConfigurationPage jcpage = new JavaCapabilityConfigurationPage();
      final IJavaProject javaProject = JavaCore.create(project);

      PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
         public void run()
         {
            jcpage.init(javaProject, null, null, true);
            try
            {
               jcpage.configureJavaProject(new SubProgressMonitor(monitor, 1 * PROGRESS_SCALE));
            } catch (Exception e)
            {
               e.printStackTrace();
            }
         }
      });

      monitor.done();

      return javaProject;
   }
   
   /**
    * Add dependencies of generated EMF code to classpath
    * 
    * @return
    */
   public static boolean addEMFDependenciesToClassPath(IProgressMonitor monitor, IProject iproject)
   {
      monitor.beginTask("", 1 * WorkspaceHelper.PROGRESS_SCALE);

      try
      {
         IJavaProject project = JavaCore.create(iproject);
         
         List<IClasspathEntry> classpathEntries = new UniqueEList<IClasspathEntry>();
         classpathEntries.addAll(Arrays.asList(project.getRawClasspath()));

         // Add EMF specific dependencies
         CodeGenUtil.EclipseUtil.addClasspathEntries(classpathEntries, "ECLIPSE_CORE_RUNTIME", "org.eclipse.core.runtime");
         CodeGenUtil.EclipseUtil.addClasspathEntries(classpathEntries, "ECLIPSE_CORE_RESOURCES", "org.eclipse.core.resources");
         CodeGenUtil.EclipseUtil.addClasspathEntries(classpathEntries, "EMF_COMMON", "org.eclipse.emf.common");
         CodeGenUtil.EclipseUtil.addClasspathEntries(classpathEntries, "EMF_ECORE", "org.eclipse.emf.ecore");
         CodeGenUtil.EclipseUtil.addClasspathEntries(classpathEntries, "EMF_ECORE_XMI", "org.eclipse.emf.ecore.xmi");
         
         // Remove duplicate entries
         List<IClasspathEntry> filteredEntries = new ArrayList<IClasspathEntry>();
         HashSet<IPath> pathes = new HashSet<IPath>();
         for (IClasspathEntry entry : classpathEntries)
         {
            IPath entryPath = entry.getPath();
            if (pathes.add(entryPath))
            {
               filteredEntries.add(entry);
            }
         }

         project.setRawClasspath(filteredEntries.toArray(new IClasspathEntry[filteredEntries.size()]), new SubProgressMonitor(monitor,
               1 * WorkspaceHelper.PROGRESS_SCALE, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
      } catch (Exception e)
      {
         e.printStackTrace();
         return false;
      }

      monitor.done();

      return true;
   }

   public static List<IProject> getProjectsOnBuildPath(IProject project)
   {
      IJavaProject javaProject = JavaCore.create(project);

      // Get current entries on the classpath
      ArrayList<IProject> projectsOnBuildPath = new ArrayList<IProject>();
      try
      {
         for (IClasspathEntry entry : javaProject.getRawClasspath())
         {
            if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT)
            {
               projectsOnBuildPath.add(ResourcesPlugin.getWorkspace().getRoot().getProject(entry.getPath().lastSegment()));
            }
         }
      } catch (JavaModelException e)
      {
         logger.error("Unable to determine projects on buildpath for: " + project.getName());
         e.printStackTrace();
      }

      Collections.reverse(projectsOnBuildPath);
      return projectsOnBuildPath;
   }

   public static void setContainerOnBuildPath(Collection<IClasspathEntry> classpathEntries, String container)
   {
      IClasspathEntry entry = JavaCore.newContainerEntry(new Path(container));
      for (IClasspathEntry iClasspathEntry : classpathEntries)
      {
         if(iClasspathEntry.getPath().equals(entry.getPath())){
            // No need to add variable - already on classpath
            return;
         }
      }
      
      classpathEntries.add(entry);
   }

   public static void setContainerOnBuildPath(IProject project, String container) {
      setContainerOnBuildPath(JavaCore.create(project), container);
   }
   
   public static void transferContainersOnBuildPath(IJavaProject from, IJavaProject to) {
      try
      {
         for (IClasspathEntry iClasspathEntry : from.getRawClasspath())
         {
            if (iClasspathEntry.getEntryKind() == IClasspathEntry.CPE_CONTAINER){
               setContainerOnBuildPath(to, iClasspathEntry.getPath().toString());
            }
         }
      } catch (JavaModelException e)
      {
         logger.error("Unable to get classpath of source(from) IJavaProject");
         e.printStackTrace();
      }      
   }
   
   public static void setContainerOnBuildPath(IJavaProject iJavaProject, String container)
   {
      try
      {
         // Get current entries on the classpath
         Collection<IClasspathEntry> classpathEntries = new ArrayList<IClasspathEntry>();
         for (IClasspathEntry iClasspathEntry : iJavaProject.getRawClasspath())
            classpathEntries.add(iClasspathEntry);

         setContainerOnBuildPath(classpathEntries, container);

         // Create new buildpath
         IClasspathEntry[] newEntries = new IClasspathEntry[classpathEntries.size()];
         classpathEntries.toArray(newEntries);

         // Set new classpath with added entries
         iJavaProject.setRawClasspath(newEntries, null);
      } catch (JavaModelException e)
      {
         logger.error("Unable to set classpath variable");
         e.printStackTrace();
      }
   }

   public static void removeProjectFromBuildPath(IJavaProject iJavaProject, IProject project)
   {      
      try
      {
         // Get current entries on the classpath and filter project out
         Collection<IClasspathEntry> classpathEntries = new ArrayList<IClasspathEntry>();
         for (IClasspathEntry iClasspathEntry : iJavaProject.getRawClasspath())
         {
            if (!(iClasspathEntry.getPath().equals(project.getFullPath()) && iClasspathEntry.getEntryKind() == IClasspathEntry.CPE_PROJECT))
               classpathEntries.add(iClasspathEntry);
         }

         // Create new buildpath
         IClasspathEntry[] newEntries = new IClasspathEntry[classpathEntries.size()];
         classpathEntries.toArray(newEntries);

         // Set new classpath
         iJavaProject.setRawClasspath(newEntries, null);
      } catch (JavaModelException e)
      {
         logger.error("Unable to set classpath variable");
         e.printStackTrace();
      }
   }

   public static boolean isContainerOnBuildPath(IProject project, String container)
   {
      IJavaProject iJavaProject = JavaCore.create(project);
      
      try
      {
         // Get current entries on the classpath and filter project out
         for (IClasspathEntry iClasspathEntry : iJavaProject.getRawClasspath())
         {
            if (iClasspathEntry.getEntryKind() == IClasspathEntry.CPE_CONTAINER && iClasspathEntry.getPath().toString().equals(container))
               return true;
         }
      } catch (JavaModelException e)
      {
         logger.error("Unable to check if " + container + " is on the classpath of " + iJavaProject);
         e.printStackTrace();
      }

      return false;
   }

   public static void addAllFolders(IProject project, String path, IProgressMonitor monitor) throws CoreException
   {
      String[] folders = path.split(SEPARATOR);
      String currentFolder = "";
      for (String folder : folders)
      {
         currentFolder += SEPARATOR + folder;
         addFolder(project, currentFolder, monitor);
      }
   }

   public static void addFile(IFile file, String contents, IProgressMonitor monitor) throws CoreException
   {
      ByteArrayInputStream source = new ByteArrayInputStream(contents.getBytes());
      file.create(source, true, new SubProgressMonitor(monitor, 1 * PROGRESS_SCALE));
      monitor.done();
   }

   /**
    * Creates a file at pathToFile with specified contents fileContent. All folders in the path are created if
    * necessary.
    * 
    * @param project
    *           Project containing file to be created
    * @param pathToFile
    *           Project relative path to file to be created
    * @param fileContent
    *           String content of file to be created
    * @param monitor
    * @throws CoreException
    */
   public static void addAllFoldersAndFile(IProject project, IPath pathToFile, String fileContent, IProgressMonitor monitor) throws CoreException
   {
      // Remove file segment
      IPath folders = pathToFile.removeLastSegments(1);

      // Create all necessary folders
      addAllFolders(project, folders.toString(), monitor);

      // Create file
      addFile(project.getFile(pathToFile), fileContent, monitor);
   }
   
   public static SubProgressMonitor createSubMonitor(IProgressMonitor monitor){
      return createSubMonitor(monitor, 1);
   }
   
   public static SubProgressMonitor createSubMonitor(IProgressMonitor monitor, int ticks) {
      return new SubProgressMonitor(monitor, ticks*PROGRESS_SCALE);
   }

   public static final String SUFFIX_GEN_ECORE = ".gen.ecore";
}
