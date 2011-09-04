package autobuildplugin;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.ide.ChooseWorkspaceData;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;

@SuppressWarnings("restriction")
public class BuildCommandLine
{
   
   private static final String PROP_VM = "eclipse.vm"; //$NON-NLS-1$

   private static final String PROP_VMARGS = "eclipse.vmargs"; //$NON-NLS-1$

   private static final String PROP_COMMANDS = "eclipse.commands"; //$NON-NLS-1$

   private static final String PROP_EXIT_CODE = "eclipse.exitcode"; //$NON-NLS-1$

   private static final String PROP_EXIT_DATA = "eclipse.exitdata"; //$NON-NLS-1$

   private static final String CMD_DATA = "-data"; //$NON-NLS-1$

   private static final String CMD_VMARGS = "-vmargs"; //$NON-NLS-1$

   private static final String NEW_LINE = "\n"; //$NON-NLS-1$
   
   IWorkbenchWindow window;
   ChooseWorkspaceData data;
   
   public BuildCommandLine(IWorkbenchWindow window){
      this.window = window;
      data = new ChooseWorkspaceData(
            Platform.getInstanceLocation().getURL());
      data.readPersistedData();
   }
   
   public void restartWorkspace(String location){
      WorkspaceMRUAction ws = new WorkspaceMRUAction(location, data);
      ws.run();
   }
   
   class WorkspaceMRUAction extends Action {

      private ChooseWorkspaceData data;

      private String location;

      WorkspaceMRUAction(String location, ChooseWorkspaceData data) {
         this.location = location; // preserve the location directly -
         // setText mucks with accelerators so we
         // can't necessarily use it safely for
         // manipulating the location later.
         setText(location);
         setToolTipText(location);
         this.data = data;
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.jface.action.Action#run()
       */
      public void run() {
         data.workspaceSelected(location);
         data.writePersistedData();
         restart(location);
      }
   }
   
   private void restart(String path) {
      String command_line = buildCommandLine(path);
      if (command_line == null) {
         return;
      }

//      System.out.println(Integer.toString(24));
      System.setProperty(PROP_EXIT_CODE, Integer.toString(IApplication.EXIT_RELAUNCH));
      System.setProperty(PROP_EXIT_DATA, command_line);
//      Workbench.getInstance().restart();
      window.getWorkbench().restart();
   }

   
   private String buildCommandLine(String workspace) {
      String property = System.getProperty(PROP_VM);
      if (property == null) {
         MessageDialog
               .openError(
                     window.getShell(),
                     IDEWorkbenchMessages.OpenWorkspaceAction_errorTitle,
                     NLS
                           .bind(
                                 IDEWorkbenchMessages.OpenWorkspaceAction_errorMessage,
                                 PROP_VM));
         return null;
      }

      StringBuffer result = new StringBuffer(512);
//      result.append("awt.toolkit=sun.awt.windows.WToolkit");
//      result.append(NEW_LINE);
//      result.append("eclipse.application=org.eclipse.ui.ide.workbench");
//      result.append(NEW_LINE);
//      result.append("eclipse.buildId=M20110210-1200");
//      result.append(NEW_LINE);
      result.append(property);
      result.append(NEW_LINE);

      // append the vmargs and commands. Assume that these already end in \n
      String vmargs = System.getProperty(PROP_VMARGS);
      if (vmargs != null) {
         result.append(vmargs);
      }

      // append the rest of the args, replacing or adding -data as required
      property = System.getProperty(PROP_COMMANDS);
      if (property == null) {
         result.append(CMD_DATA);
         result.append(NEW_LINE);
         result.append(workspace);
         result.append(NEW_LINE);
      } else {
         property = PROP_COMMANDS+"="+property;
         // find the index of the arg to replace its value
         int cmd_data_pos = property.lastIndexOf(CMD_DATA);
         if (cmd_data_pos != -1) {
            cmd_data_pos += CMD_DATA.length() + 1;
            result.append(property.substring(0, cmd_data_pos));
            result.append(workspace);
            result.append(property.substring(property.indexOf('\n',
                  cmd_data_pos)));
         } else {
            result.append(CMD_DATA);
            result.append(NEW_LINE);
            result.append(workspace);
            result.append(NEW_LINE);
            result.append(property);
         }
      }

      // put the vmargs back at the very end (the eclipse.commands property
      // already contains the -vm arg)
      if (vmargs != null) {
         result.append(CMD_VMARGS);
         result.append(NEW_LINE);
         result.append(vmargs);
      }
//      
//      result.append("eclipse.consoleLog=true");
//      result.append(NEW_LINE);
//      result.append("eclipse.launcher=E:\\programs\\eclipse-plugin\\eclipse.exe");
//      result.append(NEW_LINE);
//      result.append("eclipse.home.location=file:/E:/programs/eclipse-plugin/");
//      result.append(NEW_LINE);
//      result.append("eclipse.launcher.name=Eclipse");
//      result.append(NEW_LINE);
//      result.append("eclipse.p2.data.area=@config.dir/.p2");
//      result.append(NEW_LINE);
//      result.append("eclipse.p2.profile=SDKProfile");
//      result.append(NEW_LINE);
//      result.append("eclipse.pde.launch=true");
//      result.append(NEW_LINE);
//      result.append("eclipse.product=org.eclipse.sdk.ide");
//      result.append(NEW_LINE);
//      result.append("eclipse.vm=C:\\Windows\\system32\\javaw.exe");
//      result.append(NEW_LINE);

      return result.toString();
   }


}
