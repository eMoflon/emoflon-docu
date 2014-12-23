## Set-up your script in a few easy steps

0.  Copy eMoflonTestRunner.ps1 to a folder of your choice (test workspaces will be checked out here!)

1.  Correct the variable $ECLIPSE_HOME in the script to fit to your Eclipse installation

2.  Set the variable $OUTPUT_DIRECTORY to a suitable temporary directory.

3.  Open powershell (press Windows button, type in powershell, hit enter), and enter the following command in the shell.
	As normal user:
		Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
	As administrator:
		Set-ExecutionPolicy RemoteSigned
	
	Suspend the rule by pressing 'J' or 'Y' to confirm the changed settings.
		
3.  Close the shell, select and right-click eMoflonTestRunner.ps1, and choose "run in powershell".

## Troubleshooting

### Problem 1
**Issue:** Eclipse crashes and the log tells you: 'java.lang.RuntimeException: Application "org.moflon.testapplication" could not be found in the registry.'

**Solution:** Your Moflon devtools plugin is probably too old. Update to at least version 201407141709.

### Problem 2
**Issue:** Eclipse starts but your workspace is empty.

**Solution:** Make sure that your default SVN client/interface is *SVNKit* (Window > Preferences > Team/SVN). 
If *JavaHL* is installed, uninstall it.
