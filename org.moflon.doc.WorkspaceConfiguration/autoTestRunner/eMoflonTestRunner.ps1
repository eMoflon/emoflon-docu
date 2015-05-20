#$OLD_DIR = $(get-location).Path

# Directory for temporarily storing the checked out projects
$OUTPUT_DIRECTORY="[set output directory]"

# Path to eclipse - must contain eclipse.exe
$ECLIPSE_HOME = "[set Eclipse home]"

# Time between two executions of Eclipse
$sleepTimeBetweenStartsInSeconds = 120

# Array of all test workspaces
# (see EMoflonStandardWorkspaces)
[System.Collections.ArrayList]$WORKSPACES = @()
$WORKSPACES.add("TestWorkspace_Democles_0")
$WORKSPACES.add("TestWorkspace_Misc")
# $WORKSPACES.add("TestWorkspace_TGG_0")
# $WORKSPACES.add("TestWorkspace_TGG_1")
# $WORKSPACES.add("TransformationZoo_0")
# $WORKSPACES.add("TransformationZoo_1")
# $WORKSPACES.add("DeveloperWorkspace")
# $WORKSPACES.add("TextualSyntaxWorkspace")

# Whether to spawn a new console for each Eclipse instance, showing standard output/standard error messages
$USE_CONSOLE = $FALSE # either $TRUE or $FALSE


# Number of trials to clean the output directory
$NUM_TRIALS_FOR_CLEANING_OUTPUT_DIRECTORY = 5

echo "OUTPUT_DIRECTORY:         $OUTPUT_DIRECTORY"
echo "ECLIPSE_HOME:             $ECLIPSE_HOME"
echo "Use console?              $USE_CONSOLE"
echo "Interval between starts:  $sleepTimeBetweenStartsInSeconds"
echo "org.moflon.ide version:   $(Get-ChildItem -Name "$ECLIPSE_HOME/plugins/org.moflon.ide.core*")"
echo "Workspaces to be run:     $WORKSPACES" 
echo ""
$confirmed = Read-Host "Continue? [Y/n]"
if($confirmed -ne "" -and ($confirmed -ne "y" -or $confirmed -ne "Y")) {
    echo "User aborted. Bye."
    exit
}

# Clean up root folder
echo "Cleaning output directory (possibly with multiple runs)..."
Write-Host -NoNewline "    "
$i=1
while(Test-Path $OUTPUT_DIRECTORY) {
	
	Write-Host -NoNewline "Trial $i.."
	
	Remove-Item -Recurse -Force "$OUTPUT_DIRECTORY"
	$i = $i + 1
	if($i -gt $NUM_TRIALS_FOR_CLEANING_OUTPUT_DIRECTORY) {
		echo "Maximum number of trials reached. Please try to clean the output directory, manually."
		echo "Will now stop."
		exit
	}
}  	
echo ""
echo "Cleaning output directory done."

echo "Creating output directory"
mkdir $OUTPUT_DIRECTORY | out-null

echo "Starting Eclipse instances..."
# Start Eclipse for all workspaces
$firstIteration = $TRUE
foreach ($WORKSPACE in $WORKSPACES) {
    if (!($WORKSPACE -eq $WORKSPACES[0]) ) {
        $firstIteration = $FALSE
        echo "    Sleeping for $sleepTimeBetweenStartsInSeconds seconds..."
        Start-Sleep -s $sleepTimeBetweenStartsInSeconds
    }
    
    [System.Collections.ArrayList]$argumentList = '-data',$WORKSPACE,'-application','org.moflon.testapplication','-showLocation','-perspective','org.moflon.ide.ui.perspective'
    if($USE_CONSOLE) {
        $argumentList.Add('-console')
        $argumentList.Add('-consoleLog')
    }
    
  	$eclipse = Start-Process -WorkingDirectory $OUTPUT_DIRECTORY -FilePath $ECLIPSE_HOME\eclipse.exe -ArgumentList $argumentList   -PassThru
  	echo "    [$($eclipse.Id)] Workspace '$WORKSPACE'"
  	$eclipse.Id >> "$OUTPUT_DIRECTORY\pids.txt"
}
