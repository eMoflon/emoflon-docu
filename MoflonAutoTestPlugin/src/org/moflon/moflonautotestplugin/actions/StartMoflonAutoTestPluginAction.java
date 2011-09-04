package org.moflon.moflonautotestplugin.actions;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import javax.mail.Session;

import org.eclipse.core.resources.IProject;
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
import org.eclipse.jdt.junit.launcher.JUnitLaunchShortcut;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ui.wizards.ImportProjectSetOperation;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;

import autobuildplugin.Activator;
import autobuildplugin.preferences.PreferenceConstants;

/**
 * Starting the Moflon AutoBuild Process.
 * The hook is implemented as an Action Set, which can be customized by every view.
 *
 *The AutoBuild Process consists of the following steps:
 * a) delete the current workspace
 * b) import the selected project set
 * c) building EAP files
 * d) refreshing and code generating the workspace
 * e) running the junit test suite
 * 
 * @see IWorkbenchWindowActionDelegate
 */
public class StartMoflonAutoTestPluginAction implements
		IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;

	/**
	 * The constructor.
	 */
	public StartMoflonAutoTestPluginAction() {
	}

	/**
	 * The action has been activated. The argument of the method represents the
	 * 'real' action sitting in the workbench UI.
	 * 
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		if (performAutoBuild()) {
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(
					window.getShell());
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
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Selection in the workbench has been changed. We can change the state of
	 * the 'real' action here if we want, but this can only happen after the
	 * delegate has been created.
	 * 
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system resources we previously
	 * allocated.
	 * 
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to be able to provide parent shell
	 * for the message dialog.
	 * 
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	/**
	 * Graphical Dialog, which pops up to ask the user, if the Moflon AutoBuild Process should start
	 * @return
	 */
	private boolean performAutoBuild() {
		String[] buttons = { "Start", "Cancel" };
		Dialog start = new MessageDialog(
				window.getShell(),
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

	/**
	 * Disables Eclipse Auto Build Process
	 */
	private void turnOffAutoBuild() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceDescription description = workspace.getDescription();
		if (description.isAutoBuilding()) {
			description.setAutoBuilding(false);
			try {
				workspace.setDescription(description);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Enables Eclipse Auto Build Process:
	 * 	Eclipse buildes automatically the project, whenever files have changed
	 */
	private void turnOnAutoBuild() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceDescription description = workspace.getDescription();
		if (!description.isAutoBuilding()) {
			description.setAutoBuilding(true);
			try {
				workspace.setDescription(description);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Performing all 5 steps to generate and test the eap files
	 * @param monitor
	 */
	public void startBuildProcess(IProgressMonitor monitor) {
		monitor.beginTask("auto build process", 100);
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		// // 2. delete folder
		deletingOldProjects(monitor, store);
		// 3. import project set
		importingProjectSet(monitor, store);
		// 4. build moflon
		buildingEAP(monitor, store);
		// 5. refreshworkspace
		refreshWorkSpace(monitor, store);
		// 6. junit
		runJUnitTest(monitor, store);
		monitor.done();
	}

	/**
	 * Deleting all projects of the current workspace, expect projects, which starts with a '.' in the name
	 * @param monitor
	 * @param store
	 */
	private void deletingOldProjects(IProgressMonitor monitor,
			IPreferenceStore store) {
		if (store.getString(PreferenceConstants.AB_NEXTOP).compareTo(
				PreferenceConstants.NEXTOPERATION.DELETEWORKSPACE.toString()) == 0) {
			monitor.subTask("deleting projects");
			deleteFolder( monitor);
			monitor.worked(10);
			// refreshWorkspace(monitor);
			try {
				ResourcesPlugin.getWorkspace().getRoot()
						.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			} catch (CoreException e) {
				e.printStackTrace();
			}
			store.setValue(PreferenceConstants.AB_NEXTOP,
					PreferenceConstants.NEXTOPERATION.IMPORTPROJECTSET
							.toString());
		}
	}

	/**
	 * Importing all projects of the choosen project set.
	 * The used *.psf file is specified over the preference menu.
	 * @param monitor
	 * @param store
	 */
	@SuppressWarnings("restriction")
	private void importingProjectSet(IProgressMonitor monitor,
			IPreferenceStore store) {
		if (store.getString(PreferenceConstants.AB_NEXTOP).compareTo(
				PreferenceConstants.NEXTOPERATION.IMPORTPROJECTSET.toString()) == 0) {
			monitor.subTask("importing project set");
			ImportProjectSetOperation op = new ImportProjectSetOperation(null,
					Activator.getDefault().getPreferenceStore()
							.getString(PreferenceConstants.AB_PROJECTSET),
					new IWorkingSet[0]);
			try {
				op.run(monitor);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			monitor.worked(30);
			store.setValue(PreferenceConstants.AB_NEXTOP,
					PreferenceConstants.NEXTOPERATION.MOFLONBUILD.toString());
		}
	}

	/**
	 * Looks up every project in the workspace for *.eap files, which are used to generate the eap models
	 * @param monitor
	 * @param store
	 */
	private void buildingEAP(IProgressMonitor monitor, IPreferenceStore store) {
		if (store.getString(PreferenceConstants.AB_NEXTOP).compareTo(
				PreferenceConstants.NEXTOPERATION.MOFLONBUILD.toString()) == 0) {
			monitor.subTask("building eap");
			try {
				URL pathToExe = getPathRelToPlugIn(
						"/commandLineExeAndJunitTest/MOFLON2EAExportImportTest.exe",
						Activator.PLUGIN_ID);
				File exe = new File(pathToExe.getPath());
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				IProject[] projects = root.getProjects();
				for (int i = 0; i < projects.length; i++) {
					// if(projects[i].getNature("org.moflon.ide.ui.runtime.natures.MetamodelNature ")!=null){
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
						// }
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			monitor.worked(20);
			store.setValue(PreferenceConstants.AB_NEXTOP,
					PreferenceConstants.NEXTOPERATION.REFRESH.toString());
		}
	}

	/**
	 * Refreshes the workspace, enables the eclipse autobuild process until this operation is finished to generate the models by the moflon EAAddin.
	 * @param monitor
	 * @param store
	 */
	private void refreshWorkSpace(IProgressMonitor monitor,
			IPreferenceStore store) {
		if (store.getString(PreferenceConstants.AB_NEXTOP).compareTo(
				PreferenceConstants.NEXTOPERATION.REFRESH.toString()) == 0) {
			IWorkspace ws = ResourcesPlugin.getWorkspace();
			try {
				ws.getRoot().refreshLocal(IResource.DEPTH_INFINITE, monitor);
				turnOnAutoBuild();
				try {
					Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD,
							monitor);
				} catch (OperationCanceledException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				turnOffAutoBuild();
				ws.getRoot().refreshLocal(IResource.DEPTH_INFINITE, monitor);
			} catch (CoreException e1) {
				e1.printStackTrace();
			}
			monitor.worked(20);
			store.setValue(PreferenceConstants.AB_NEXTOP,
					PreferenceConstants.NEXTOPERATION.JUNIT.toString());
		}
	}

	/**
	 * Looking up for a project with the name 'EclipseTestSuite' and perfoming this project as junit test suite.
	 * @param monitor
	 * @param store
	 */
	private void runJUnitTest(IProgressMonitor monitor, IPreferenceStore store) {
		if (store.getString(PreferenceConstants.AB_NEXTOP).compareTo(
				PreferenceConstants.NEXTOPERATION.JUNIT.toString()) == 0) {
			monitor.subTask("running junit tests");

			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IProject[] projects = root.getProjects();
			IProject testProject = null;
			for (int i = 0; i < projects.length; i++) {
				if (projects[i].getName().compareTo("EclipseTestSuite") == 0)
					testProject = projects[i];
			}

			ILaunchShortcut shortcut = new JUnitLaunchShortcut();
			if (testProject != null) {
				shortcut.launch(new StructuredSelection(testProject),
						ILaunchManager.RUN_MODE);
			}
			store.setValue(PreferenceConstants.AB_NEXTOP,
					PreferenceConstants.NEXTOPERATION.IMPORTPROJECTSET
							.toString());
		}
	}

	/**
	 * Deleting all projects in the workspace, which doesn#t start with a dot ('.').
	 * @param root
	 * @param monitor
	 */
	private void deleteFolder(IProgressMonitor monitor) {
		IWorkspaceRoot root2 = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = root2.getProjects();
		for (int i = 0; i < projects.length; i++) {
			if (!projects[i].getName().startsWith(".")) {
				System.out.println("DELETED " + projects[i].getName());
				try {
					projects[i].delete(true, true, null);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
			monitor.worked(1);
		}
	}

	/**
	 * opens a path relative to the plugin location.
	 * @param filePath
	 * @param PLUGIN_ID
	 * @return
	 * @throws IOException
	 */
	public URL getPathRelToPlugIn(String filePath, String PLUGIN_ID)
			throws IOException {
		return FileLocator.resolve(Platform.getBundle(PLUGIN_ID).getEntry(
				filePath));
	}
}