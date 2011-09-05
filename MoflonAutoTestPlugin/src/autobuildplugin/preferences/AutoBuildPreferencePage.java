package autobuildplugin.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import autobuildplugin.Activator;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class AutoBuildPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {

	public AutoBuildPreferencePage() {
		super(GRID);
		init(PlatformUI.getWorkbench());
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	public void createFieldEditors() {
		FileFieldEditor filter = new FileFieldEditor(
				PreferenceConstants.AB_PROJECTSET, "ProjectSet:",
				getFieldEditorParent());
		String[] ext = { "*.psf" };
		filter.setFileExtensions(ext);
		addField(filter);
		DirectoryFieldEditor pathToLog = new DirectoryFieldEditor(
				PreferenceConstants.AB_LOG, "Store log:",
				getFieldEditorParent());
		addField(pathToLog);
		addField(new RadioGroupFieldEditor(PreferenceConstants.AB_NEXTOP,
				"Start AutoBuild Process From Here",
				1,
				new String[][] { // {
						// "1. Switch Workspace",
						// PreferenceConstants.NEXTOPERATION.SWITCHWORKSPACE.toString()
						// },
						{
								"1. Delete Workspace Contents",
								PreferenceConstants.NEXTOPERATION.DELETEWORKSPACE
										.toString() },
						{
								"2. Import Project Set",
								PreferenceConstants.NEXTOPERATION.IMPORTPROJECTSET
										.toString() },
						{
								"3. Export projects from EA",
								PreferenceConstants.NEXTOPERATION.MOFLONBUILD
										.toString() },
						{
								"4. Building Workspace",
								PreferenceConstants.NEXTOPERATION.REFRESH
										.toString() },
						{
								"5. Run Junit Tests",
								PreferenceConstants.NEXTOPERATION.JUNIT
										.toString() } }, getFieldEditorParent()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.AB_LOG,
				PreferenceConstants.AB_LOG_PATH);
		store.setDefault(PreferenceConstants.AB_NEXTOP,
				PreferenceConstants.AB_DEFAULT_NEXTOP);
		store.setDefault(PreferenceConstants.AB_PROJECTSET,
				PreferenceConstants.AB_DEFAULT_PROJECTSET);
//
//		if (store.getString(PreferenceConstants.AB_PROJECTSET).length() < 1) {
//			store.setValue(PreferenceConstants.AB_PROJECTSET,
//					PreferenceConstants.AB_DEFAULT_PROJECTSET);
//		}
//		 if (store.getString(PreferenceConstants.AB_LOG).length() < 1)
//		 {
//		 store.setValue(PreferenceConstants.AB_LOG,
//		 PreferenceConstants.AB_LOG_PATH);
//		 }
//		if (store.getString(PreferenceConstants.AB_NEXTOP).length() < 1) {
//			store.setValue(PreferenceConstants.AB_NEXTOP,
//					PreferenceConstants.AB_DEFAULT_NEXTOP);
//		}
		setDescription("AutoBuild Plugin Preferences");
		setPreferenceStore(store);
	}
}