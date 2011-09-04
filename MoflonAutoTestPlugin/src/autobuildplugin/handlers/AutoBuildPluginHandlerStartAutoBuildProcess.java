package autobuildplugin.handlers;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ui.wizards.ImportProjectSetOperation;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BuildAction;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.ide.actions.BuildUtilities;
import org.eclipse.ui.internal.ide.dialogs.CleanDialog;
import org.eclipse.ui.internal.ide.handlers.BuildAllProjectsHandler;
import org.eclipse.jdt.junit.launcher.JUnitLaunchShortcut;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.IShellProvider;

import autobuildplugin.Activator;
import autobuildplugin.preferences.PreferenceConstants;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
@SuppressWarnings("restriction")
public class AutoBuildPluginHandlerStartAutoBuildProcess extends
		AbstractHandler {

	public AutoBuildPluginHandlerStartAutoBuildProcess() {
	}

	/**
	 * Used to retrieve resources embedded in the plugin (jar files when
	 * installed on client machine).
	 * 
	 * @param filePath
	 *            Must be relative to the plugin root and indicate an existing
	 *            resource (packaged in build)
	 * @param PLUGIN_ID
	 *            The id of the plugin bundle to be searched
	 * @return URL to the resource or null if nothing was found (URL because
	 *         resource could be inside a jar).
	 * @throws IOException
	 */
	public URL getPathRelToPlugIn(String filePath, String PLUGIN_ID)
			throws IOException {
		return FileLocator.resolve(Platform.getBundle(PLUGIN_ID).getEntry(
				filePath));
		// try
		// {
		// } catch (Exception e)
		// {
		// // logger.error("Unable to resolve: " + filePath + " in plugin!");
		// e.printStackTrace();
		// return null;
		// }
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		// IWorkbenchWindow window =
		// HandlerUtil.getActiveWorkbenchWindowChecked(event);
		if (performAutoBuild()) {
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(
					HandlerUtil.getActiveShell(event).getShell());

			try {
				dialog.run(true, true, new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor) {
						if (monitor.isCanceled())
							return;
						turnOffAutoBuild();
						startBuildProcess(monitor);
						turnOnAutoBuild();
					}
				});
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return null;
	}

	public boolean performAutoBuild() {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getShell();
		String[] buttons = { "Start", "Cancel" };
		Dialog start = new MessageDialog(
				shell,
				"Start Moflon Auto Build Process?",
				null,
				"Start autobuild and testing from "
						+ Activator.getDefault().getPreferenceStore()
								.getString(PreferenceConstants.AB_NEXTOP) + "?",
				MessageDialog.QUESTION_WITH_CANCEL, buttons, 0);
		start.setBlockOnOpen(true);
		int returnCode = start.open();
		return (returnCode == MessageDialog.OK);
	}

	private void turnOffAutoBuild() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceDescription description = workspace.getDescription();
		if (description.isAutoBuilding()) {
			System.out.println("SWITCH OFF AUTOBUILD");
			description.setAutoBuilding(false);
			try {
				workspace.setDescription(description);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		} else
			System.out.println("AUTOBUILD WAS OFF");

	}
	
	private void turnOnAutoBuild() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceDescription description = workspace.getDescription();
		if (!description.isAutoBuilding()) {
			System.out.println("SWITCH ON AUTOBUILD");
			description.setAutoBuilding(true);
			try {
				workspace.setDescription(description);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		} else
			System.out.println("AUTOBUILD WAS ON");

	}

	public void startBuildProcess(IProgressMonitor monitor) {
		monitor.beginTask("auto build process", 100);
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		// // 2. delete folder
		// if
		// (store.getString(PreferenceConstants.AB_NEXTOP).compareTo(PreferenceConstants.NEXTOPERATION.DELETEWORKSPACE.toString())
		// == 0)
		// {
		// monitor.subTask("deleting projects");
		// deleteFolder(ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile(),
		// monitor);
		// monitor.worked(10);
		// refreshWorkspace(monitor);
		// store.setValue(PreferenceConstants.AB_NEXTOP,
		// PreferenceConstants.NEXTOPERATION.IMPORTPROJECTSET.toString());
		// }
		// 3. import project set
		if (store.getString(PreferenceConstants.AB_NEXTOP).compareTo(
				PreferenceConstants.NEXTOPERATION.IMPORTPROJECTSET.toString()) == 0) {
			monitor.subTask("importing project set");
			importProjectSet(monitor);
			monitor.worked(30);
			 store.setValue(PreferenceConstants.AB_NEXTOP,
			 PreferenceConstants.NEXTOPERATION.MOFLONBUILD.toString());
		}
		// 4. build moflon
		if (store.getString(PreferenceConstants.AB_NEXTOP).compareTo(
				PreferenceConstants.NEXTOPERATION.MOFLONBUILD.toString()) == 0) {
			monitor.subTask("building eap");
			buildEAP();
			monitor.worked(20);
			 store.setValue(PreferenceConstants.AB_NEXTOP,
			 PreferenceConstants.NEXTOPERATION.REFRESH.toString());
		}
		// 5. refreshworkspace
		if (store.getString(PreferenceConstants.AB_NEXTOP).compareTo(
				PreferenceConstants.NEXTOPERATION.REFRESH.toString()) == 0) {
			refreshWorkspace(monitor);
			monitor.worked(20);
			 store.setValue(PreferenceConstants.AB_NEXTOP,
			 PreferenceConstants.NEXTOPERATION.JUNIT.toString());
		}
		// 6. junit
		if (store.getString(PreferenceConstants.AB_NEXTOP).compareTo(
				PreferenceConstants.NEXTOPERATION.JUNIT.toString()) == 0) {
			monitor.subTask("running junit tests");
			runJunitTests();
			monitor.worked(20);
			 store.setValue(PreferenceConstants.AB_NEXTOP,
			 PreferenceConstants.NEXTOPERATION.IMPORTPROJECTSET.toString());
			// store.setValue(PreferenceConstants.AB_AUTOMODE, false);
		}
		monitor.done();
	}

	private void deleteFolder(File root, IProgressMonitor monitor){
		IWorkspaceRoot root2 = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = root2.getProjects();
		for (int i = 0; i < projects.length; i++) {
			// try
			// {
			if (!projects[i].getName().startsWith(".")) {
				System.out.println("DELETED " + projects[i].getName());
				try {
					projects[i].delete(true, true, null);
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			monitor.worked(1);
			// } catch (CoreException e)
			// {
			// e.printStackTrace();
			// }
		}
	}

	private void refreshWorkspace(IProgressMonitor monitor) {
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		try {
			ws.getRoot().refreshLocal(IResource.DEPTH_INFINITE, monitor);
			ws.build(IncrementalProjectBuilder.CLEAN_BUILD, monitor);
			ws.getRoot().refreshLocal(IResource.DEPTH_INFINITE, monitor);
			ws.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
//		try {
//			ws.getRoot().refreshLocal(IResource.DEPTH_INFINITE, monitor);
//			IProject[] projects = ws.getRoot().getProjects();
//			for(IProject p : projects){
//				if(p.getNature("org.moflon.ide.ui.runtime.natures.IntegrationNature")==null){
//					System.out.println("BUILDING " + p.getName());
//					Thread.sleep(5000);					
//					p.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
//					ws.getRoot().refreshLocal(IResource.DEPTH_INFINITE, monitor);
//				}
//			}
//			ws.getRoot().refreshLocal(IResource.DEPTH_INFINITE, monitor);
//			for(IProject p : projects){
//				if(p.getNature("org.moflon.ide.ui.runtime.natures.IntegrationNature")==null){
//					System.out.println("BUILDING " + p.getName());
//					Thread.sleep(5000);
//					p.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
//					Thread.sleep(500);
//					ws.getRoot().refreshLocal(IResource.DEPTH_INFINITE, monitor);
//				}
//			}
//			ws.getRoot().refreshLocal(IResource.DEPTH_INFINITE, monitor);
//			for(IProject p : projects){
//				if(p.getNature("org.moflon.ide.ui.runtime.natures.IntegrationNature")==null){
//					System.out.println("BUILDING " + p.getName());
//					Thread.sleep(5000);
//					p.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
//					Thread.sleep(500);
//					ws.getRoot().refreshLocal(IResource.DEPTH_INFINITE, monitor);
//				}
//			}
////			IProjectDescription description = projects[i].getDescription();
//////	      System.out.print("\tNatures: ");
//////	      String[] natures = description.getNatureIds();
//////	      for(String nature : natures){
//////	    	  System.out.print(nature+" ");
//////	      }
//////	      System.out.print("\n");
//////	      System.out.println(projects[i].getNature("org.moflon.ide.ui.runtime.natures.IntegrationNature"));
////			ws.getRoot().refreshLocal(IResource.DEPTH_INFINITE, monitor);
////			ws.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
////			ws.getRoot().refreshLocal(IResource.DEPTH_INFINITE, monitor);
////			ws.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
////			ws.getRoot().refreshLocal(IResource.DEPTH_INFINITE, monitor);
//		} catch (CoreException e) {
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	private void importProjectSet(IProgressMonitor monitor) {
		ImportProjectSetOperation op = new ImportProjectSetOperation(null,
				Activator.getDefault().getPreferenceStore()
						.getString(PreferenceConstants.AB_PROJECTSET),
				new IWorkingSet[0]);
		// try
		// {
		try {
			op.run(monitor);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void buildEAP() {
		try {
			URL pathToExe = getPathRelToPlugIn(
					"/commandLineExeAndJunitTest/MOFLON2EAExportImportTest.exe",
					Activator.PLUGIN_ID);
			File exe = new File(pathToExe.getPath());
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IProject[] projects = root.getProjects();
			for (int i = 0; i < projects.length; i++) {
//				if(projects[i].getNature("org.moflon.ide.ui.runtime.natures.MetamodelNature ")!=null){
				IPath path = projects[i].getLocation();
				if (path.toFile().isDirectory()) {
					for (File file : path.toFile().listFiles()) {
						if (file.getName().contains(".eap")) {
							System.out.println("Moflon Building:"
									+ file.getName());
							File eap = new File(file.getAbsolutePath());
							Runtime rt = Runtime.getRuntime();
							Process pr = rt.exec(exe.getAbsolutePath()
									+ " -e --eap " + eap.getAbsolutePath());
							// int exitVal = pr.waitFor();
							pr.waitFor();
						}
					}
//				}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		catch (CoreException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	private void runJunitTests() {
		// System.out.println("### PROJECTS ###");
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		// System.out.println("ROOT: " + root.getFullPath().toString());
		IProject[] projects = root.getProjects();
		IProject testProject = null;
		for (int i = 0; i < projects.length; i++) {
			if (projects[i].getName().compareTo("EclipseTestSuite") == 0)
				testProject = projects[i];
		}

		// Create shortcut for launching JUnit tests
		ILaunchShortcut shortcut = new JUnitLaunchShortcut();

		// Determine Test project (convention)
		// IProject project = determineTestProject();

		// Displays a message/warning when no tests are found (pass project so
		// progress shows name)
		if (testProject != null) {
			System.out.println("### STARTING JUNIT TESTS ###");
			shortcut.launch(new StructuredSelection(testProject),
					ILaunchManager.RUN_MODE);
			System.out.println("### FINISHED ###");
		} else
			System.out.println("-- Project IS NULL, JUNIT");
	}
}