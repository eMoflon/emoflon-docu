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
import java.util.Set;

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
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.emf.codegen.util.CodeGenUtil;
import org.eclipse.emf.common.util.UniqueEList;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.wizards.JavaCapabilityConfigurationPage;
import org.eclipse.ui.PlatformUI;

/**
 * A collection of useful helper methods when dealing with a workspace in an eclipse plugin.
 */
public class WorkspaceHelper
{

   private static final Logger logger = Logger.getLogger(WorkspaceHelper.class);

   public final static String PATH_SEPARATOR = "/";

   public final static String MODEL_FOLDER = "model";

   public final static String DEBUG_FOLDER = "debug";

   public static final String LIB_FOLDER = "lib";

   public static final String GEN_FOLDER = "gen";

   public final static String TEMP_FOLDER = ".temp";

   public final static String SNAPSHOT_FOLDER = "snapshot";

   public final static int PROGRESS_SCALE = 1000;

   public static final String ECORE_FILE_EXTENSION = ".ecore";

   public static final String TGG_FILE_EXTENSION = ".tgg.xmi";

   public static final String INJECTION_FOLDER = "injection";

   public static final String INJECTION_FILE_EXTENSION = "inject";

   public static final String JAVA_FILE_EXTENSION = "java";

   public static final String INSTANCES_FOLDER = "instances";

   public static final String GEN_MODEL_EXT = ".genmodel";

   public static final String MOCA_CONTAINER = "org.moflon.moca.MOCA_CONTAINER";

   public static final String MOFLON_CONTAINER = "org.moflon.ide.MOFLON_CONTAINER";

   public static final String TIE_CONTAINER = "org.moflon.tie.TIE_CONTAINER";

   public static final String MOSL_CONTAINER = "org.moflon.ide.MOSL";

   public static final String REPOSITORY_NATURE_ID = "org.moflon.ide.ui.runtime.natures.RepositoryNature";

   public static final String INTEGRATION_NATURE_ID = "org.moflon.ide.ui.runtime.natures.IntegrationNature";

   public static final String METAMODEL_NATURE_ID = "org.moflon.ide.ui.runtime.natures.MetamodelNature";

   public static final String ANTLR_3 = "/lib/antlr-3.5.2-complete.jar";

   public static final String LOG4J_JAR = "/lib/log4j-1.2.17.jar";

   public static final String SUFFIX_GEN_ECORE = ".gen.ecore";

   public static final String PLUGIN_ID_MOFLON_DEPENDENCIES = "org.moflon.dependencies";

   /**
    * Checks if given name is a valid name for a new project in the current workspace.
    * 
    * @param projectName
    *           Name of project to be created in current workspace
    * @param pluginId
    *           ID of bundle
    * @return A status object indicating success or failure and a relevant message.
    */
   public static IStatus validateProjectName(final String projectName, final String pluginId)
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
   public static IProject createProject(final String projectName, final String pluginId, final IProgressMonitor monitor) throws CoreException
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
      newProject.create(description, createSubMonitor(monitor));
      newProject.open(createSubMonitor(monitor));

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
   public static IFolder addFolder(final IProject project, final String folderName, final IProgressMonitor monitor) throws CoreException
   {
      monitor.beginTask("", 1 * PROGRESS_SCALE);

      IFolder projFolder = project.getFolder(folderName);
      if (!projFolder.exists())
         projFolder.create(true, true, createSubMonitor(monitor));
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
   public static void addFile(final IProject project, final String fileName, final URL pathToContent, final String pluginID, final IProgressMonitor monitor)
         throws CoreException, URISyntaxException, IOException
   {
      monitor.beginTask("", 1 * PROGRESS_SCALE);

      IFile projectFile = project.getFile(fileName);
      InputStream contents = pathToContent.openStream();
      projectFile.create(contents, true, createSubMonitor(monitor));

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
   public static void addFile(final IProject project, final String fileName, final String contents, final IProgressMonitor monitor) throws CoreException
   {
      monitor.beginTask("", 1 * PROGRESS_SCALE);
      IFile projectFile = project.getFile(fileName);
      ByteArrayInputStream source = new ByteArrayInputStream(contents.getBytes());
      projectFile.create(source, true, createSubMonitor(monitor));
      monitor.done();
   }

   /**
    * Sets the folderNames as source-folders in java build path of java project javaProject
    * 
    * @param javaProject
    *           java project whose build path will be modified
    * @param folderNames
    *           source folder names to add to build path of project javaProject
    * @param extraAttributes
    * @param monitor
    *           a progress monitor, or null if progress reporting is not desired
    * @throws JavaModelException
    */
   public static void setAsSourceFolderInBuildpath(final IJavaProject javaProject, final IFolder[] folderNames, final IClasspathAttribute[] extraAttributes,
         final IProgressMonitor monitor) throws JavaModelException
   {
      monitor.beginTask("", 2 * PROGRESS_SCALE);

      Collection<IClasspathEntry> entries = getClasspathEntries(javaProject);

      // Add new entries for the classpath
      if (folderNames != null)
      {
         for (IFolder folder : folderNames)
         {
            if (folder != null)
            {
               IClasspathEntry prjEntry = JavaCore.newSourceEntry(folder.getFullPath(), null, null, null, extraAttributes);
               entries.add(prjEntry);
            }
         }
      }
      monitor.worked(1 * PROGRESS_SCALE);

      setBuildPath(javaProject, entries, monitor);

      monitor.done();
   }

   /**
    * Returns the set of classpath entries of the given Java project
    */
   public static Collection<IClasspathEntry> getClasspathEntries(final IJavaProject javaProject) throws JavaModelException
   {
      return new HashSet<>(Arrays.asList(javaProject.getRawClasspath()));
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
   public static void addNature(final IProject project, final String natureId, final IProgressMonitor monitor) throws CoreException
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
      project.setDescription(description, createSubMonitor(monitor));

      monitor.done();
   }

   public static void setJarAsLibOnBuildpath(final IJavaProject javaProject, final IFile jar, final IProgressMonitor monitor) throws JavaModelException
   {
      monitor.beginTask("", 2 * PROGRESS_SCALE);

      // Get current entries on the classpath
      LinkedList<IClasspathEntry> classpathEntries = new LinkedList<IClasspathEntry>();
      for (IClasspathEntry entry : javaProject.getRawClasspath())
         classpathEntries.add(entry);

      // Add new entry for the classpath
      if (jar != null)
      {
         IClasspathEntry libEntry = JavaCore.newLibraryEntry(jar.getFullPath(), null, null);
         classpathEntries.add(libEntry);
      }

      setBuildPath(javaProject, classpathEntries, monitor);
      
      monitor.worked(1 * PROGRESS_SCALE);

      monitor.done();
   }

   public static void setProjectOnBuildpath(final IJavaProject javaProject, final IJavaProject dependency, final IProgressMonitor monitor)
         throws JavaModelException
   {
      monitor.beginTask("", 2 * PROGRESS_SCALE);

      // Helper collections to determine the current entries of the java project's classpath
      List<IClasspathEntry> classpathEntries = new LinkedList<>();
      Set<IPath> tempSetOfPathsForProjects = new HashSet<>(); // used for filtering (s. below)

      // Collect project paths in list and filter out duplicates
      for (IClasspathEntry entry : javaProject.getRawClasspath())
      {
         if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT)
         {
            // we found a project dependency
            IPath pathOfEntry = entry.getPath();
            // if we have already encountered the entry (should not happen though [mw])
            // do not add it again
            if (!tempSetOfPathsForProjects.contains(pathOfEntry))
            {
               // we have found a new entry
               // update the helper set
               tempSetOfPathsForProjects.add(pathOfEntry);
               // add the entry also to the classpath (to maintain the original ordering)
               classpathEntries.add(entry);
            }
         } else
         {
            // this classpathEntry does not reference another project
            // so just add it (can this assumption potentially lead to an error? [mw])
            classpathEntries.add(entry);
         }
      }

      // Add new project path to list of project paths (if not already present)
      if (dependency != null)
      {
         IClasspathEntry projectEntry = JavaCore.newProjectEntry(dependency.getPath());
         if (tempSetOfPathsForProjects.add(projectEntry.getPath()))
            classpathEntries.add(projectEntry);
      }

      setBuildPath(javaProject, classpathEntries, monitor);
      
      monitor.worked(1 * PROGRESS_SCALE);

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
   public static IJavaProject setUpAsJavaProject(final IProject project, final IProgressMonitor monitor)
   {
      monitor.beginTask("", 1 * PROGRESS_SCALE);

      final JavaCapabilityConfigurationPage jcpage = new JavaCapabilityConfigurationPage();
      final IJavaProject javaProject = JavaCore.create(project);

      PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
         @Override
         public void run()
         {
            jcpage.init(javaProject, null, null, true);
            try
            {
               jcpage.configureJavaProject(createSubMonitor(monitor));
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
    * Add dependencies of generated EMF code to the build path
    * 
    * @return
    */
   public static boolean addEMFDependenciesToBuildPath(final IProgressMonitor monitor, final IProject iproject)
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

   /**
    * Reversed list of {@link #getProjectsOnBuildPath(IProject)}
    */
   public static List<IProject> getProjectsOnBuildPathInReversedOrder(final IProject project)
   {
      List<IProject> result = getProjectsOnBuildPath(project);
      Collections.reverse(result);
      return result;
   }

   /**
    * Returns a list of all classpath entries of type {@link IClasspathEntry#CPE_PROJECT} of the given project.
    */
   public static List<IProject> getProjectsOnBuildPath(final IProject project)
   {
      // Fetch or create java project view of the given project
      IJavaProject javaProject = JavaCore.create(project);

      // Get current entries on the classpath
      ArrayList<IProject> projectsOnBuildPath = new ArrayList<>();
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

      return projectsOnBuildPath;
   }

   /**
    * Adds the given container to the list of build path entries (if not included, yet)
    */
   public static void addContainerToBuildPath(final Collection<IClasspathEntry> classpathEntries, final String container)
   {
      IClasspathEntry entry = JavaCore.newContainerEntry(new Path(container));
      for (IClasspathEntry iClasspathEntry : classpathEntries)
      {
         if (iClasspathEntry.getPath().equals(entry.getPath()))
         {
            // No need to add variable - already on classpath
            return;
         }
      }

      classpathEntries.add(entry);
   }

   /**
    * Adds the given container to the build path of the given project.
    */
   public static void addContainerToBuildPath(final IProject project, final String container)
   {
      addContainerToBuildPath(JavaCore.create(project), container);
   }

   /**
    * Adds the given container to the build path of the given java project.
    */
   public static void addContainerToBuildPath(final IJavaProject iJavaProject, final String container)
   {
      try
      {
         // Get current entries on the classpath
         Collection<IClasspathEntry> classpathEntries = new ArrayList<>(Arrays.asList(iJavaProject.getRawClasspath()));

         addContainerToBuildPath(classpathEntries, container);

         setBuildPath(iJavaProject, classpathEntries);
      } catch (JavaModelException e)
      {
         logger.error("Unable to set classpath variable");
         e.printStackTrace();
      }
   }

   /**
    * Removes a project from the build path of another project
    * 
    * @param javaProject
    *           the project whose build path is manipulated
    * @param toBeRemovedProject
    *           the project to be removed
    */
   public static void removeProjectFromBuildPath(final IJavaProject javaProject, final IProject toBeRemovedProject)
   {
      try
      {
         // Get current entries on the classpath and filter project out
         Collection<IClasspathEntry> classpathEntries = new ArrayList<>();
         for (IClasspathEntry iClasspathEntry : javaProject.getRawClasspath())
         {
            if (!(iClasspathEntry.getPath().equals(toBeRemovedProject.getFullPath()) && iClasspathEntry.getEntryKind() == IClasspathEntry.CPE_PROJECT))
               classpathEntries.add(iClasspathEntry);
         }

         setBuildPath(javaProject, classpathEntries);
      } catch (JavaModelException e)
      {
         logger.error("Unable to set classpath variable");
         e.printStackTrace();
      }
   }

   /**
    * Returns whether the given container appears on the build path of the given project.
    */
   public static boolean isContainerOnBuildPath(final IProject project, final String container)
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

   private static void setBuildPath(final IJavaProject javaProject, final Collection<IClasspathEntry> entries, final IProgressMonitor monitor) throws JavaModelException
   {
      // Create new buildpath
      IClasspathEntry[] newEntries = new IClasspathEntry[entries.size()];
      entries.toArray(newEntries);

      // Set new classpath with added entries
      javaProject.setRawClasspath(newEntries, monitor != null ? createSubMonitor(monitor) : null);
   }
   
   private static void setBuildPath(final IJavaProject javaProject, final Collection<IClasspathEntry> entries) throws JavaModelException
   {
      setBuildPath(javaProject, entries, null);
   }

   /**
    * Creates a folder denoted by the path inside the given project.
    * 
    * @param project
    * @param path
    *           the path, separated with {@link WorkspaceHelper#PATH_SEPARATOR}
    * @param monitor
    * @throws CoreException
    */
   public static void addAllFolders(final IProject project, final String path, final IProgressMonitor monitor) throws CoreException
   {
      String[] folders = path.split(PATH_SEPARATOR);
      StringBuilder currentFolder = new StringBuilder();
      for (String folder : folders)
      {
         currentFolder.append(PATH_SEPARATOR).append(folder);
         addFolder(project, currentFolder.toString(), monitor);
      }
   }

   /**
    * Creates the given file and stores the given contents in it.
    * 
    * @param file
    * @param contents
    * @param monitor
    *           the monitor that reports on the progress
    * @throws CoreException
    */
   public static void addFile(final IFile file, final String contents, final IProgressMonitor monitor) throws CoreException
   {
      ByteArrayInputStream source = new ByteArrayInputStream(contents.getBytes());
      file.create(source, true, createSubMonitor(monitor));
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
   public static void addAllFoldersAndFile(final IProject project, final IPath pathToFile, final String fileContent, final IProgressMonitor monitor)
         throws CoreException
   {
      // Remove file segment
      IPath folders = pathToFile.removeLastSegments(1);

      // Create all necessary folders
      addAllFolders(project, folders.toString(), monitor);

      // Create file
      addFile(project.getFile(pathToFile), fileContent, monitor);
   }

   /**
    * Creates a submonitor of the given monitor with 1 tick length.
    */
   public static SubProgressMonitor createSubMonitor(final IProgressMonitor monitor)
   {
      return createSubMonitor(monitor, 1);
   }

   /**
    * Creates a submonitor of the given monitor with the given number of ticks.
    */
   public static SubProgressMonitor createSubMonitor(final IProgressMonitor monitor, final int ticks)
   {
      return new SubProgressMonitor(monitor, ticks * PROGRESS_SCALE);
   }

   public static boolean isMoflonProject(final IProject project) throws CoreException
   {
      return isRepositoryProject(project) || isIntegrationProject(project);
   }

   public static boolean isMoflonOrMetamodelProject(final IProject project) throws CoreException
   {
      return isMoflonProject(project) || isMetamodelProject(project);
   }

   public static boolean isIntegrationProject(final IProject project) throws CoreException
   {
      return project.hasNature(INTEGRATION_NATURE_ID);
   }

   public static boolean isRepositoryProject(final IProject project) throws CoreException
   {
      return project.hasNature(REPOSITORY_NATURE_ID);
   }

   public static boolean isMetamodelProject(final IProject project) throws CoreException
   {
      return project.hasNature(METAMODEL_NATURE_ID);
   }

   public static boolean isInjectionFile(final IResource resource)
   {
      return resource != null && isFile(resource) && resource.getName().endsWith(INJECTION_FILE_EXTENSION);
   }

   public static boolean isJavaFile(final IResource resource)
   {
      return resource != null && isFile(resource) && resource.getName().endsWith(".java");
   }

   /**
    * Returns the project in the workspace with the given project name.
    * 
    * The returned project has to be checked for existence
    */
   public static IProject getProjectRoot(final String projectName)
   {
      return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
   }

   /**
    * Returns whether the given file has a name that ends with 'pdf'
    */
   public static boolean isPdfFile(final IResource resource)
   {
      return isFile(resource) && resource.getName().endsWith(".pdf");
   }

   /**
    * Returns whether the given resource is of type {@link IResource#FILE}
    */
   public static boolean isFile(final IResource resource)
   {
      return resource != null && resource.getType() == IResource.FILE;
   }

   /**
    * Returns whether the given resource is of type {@link IResource#FOLDER}
    */
   public static boolean isFolder(final IResource resource)
   {
      return resource.getType() == IResource.FOLDER;
   }

   public static IPath getPathToInjection(final IFile javaFile)
   {
      final IPath packagePath = javaFile.getProjectRelativePath().removeFirstSegments(1);
      final IPath pathToInjection = packagePath.removeFileExtension().addFileExtension(INJECTION_FILE_EXTENSION);
      final IFolder injectionFolder = javaFile.getProject().getFolder(WorkspaceHelper.INJECTION_FOLDER);
      final IPath fullInjectionPath = injectionFolder.getProjectRelativePath().append(pathToInjection);
      return fullInjectionPath;
   }

   public static IPath getPathToJavaFile(final IFile file)
   {
      final IPath packagePath = file.getProjectRelativePath().removeFirstSegments(1);
      final IPath pathToJavaFile = packagePath.removeFileExtension().addFileExtension(JAVA_FILE_EXTENSION);
      final IFolder genFolder = file.getProject().getFolder(WorkspaceHelper.GEN_FOLDER);
      final IPath fullJavaPath = genFolder.getProjectRelativePath().append(pathToJavaFile);
      return fullJavaPath;
   }
}
