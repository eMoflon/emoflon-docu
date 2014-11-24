$OLD_DIR = $(get-location).Path
$OUTPUT_DIRECTORY="$HOME\emoflon_autotest_tmp_dir"

# List of all test workspaces
$WORKSPACES = "TestWorkspace_Democles_0",
 	"TestWorkspace_Misc",
	"TestWorkspace_TGG_0",
	"TestWorkspace_TGG_1",
	"TransformationZoo_0",
	"TransformationZoo_1"

# Path to eclipse - must contain eclipse.exe
$ECLIPSE_HOME = "C:\Program Files\eclipse"

echo "OUTPUT_DIRECTORY:   $OUTPUT_DIRECTORY"
echo "ECLIPSE_HOME:       $ECLIPSE_HOME"
#echo "org.moflon.ide version: $(Get-ChildItem -Name "$ECLIPSE_HOME/plugins/org.moflon.ide*")[0]"
echo ""

# Clean up root folder
echo "Cleaning output directory (possibly with multiple runs)..."
Write-Host -NoNewline "    "
$i=1
while(Test-Path $OUTPUT_DIRECTORY) {
	
	Write-Host -NoNewline "Trial $i.."
	
	Remove-Item -Recurse -Force "$OUTPUT_DIRECTORY"
	$i = $i + 1
	if($i -gt 10) {
		echo "Maximum number of trials reached. Please try to clean the output directory, manually."
		echo "Will now stop."
		exit
	}
}  	
echo ""
echo "Cleaning output directory done."

echo "Creating output directory"
mkdir $OUTPUT_DIRECTORY | out-null
echo "Changing to output directory"
cd $OUTPUT_DIRECTORY


echo "Starting Eclipse instances..."
# Start Eclipse for all workspaces			  
foreach ($WORKSPACE in $WORKSPACES) {
  	$eclipse = Start-Process -FilePath $ECLIPSE_HOME\eclipse.exe -ArgumentList '-data',$WORKSPACE,'-application','org.moflon.testapplication','-showLocation'   -PassThru
  	echo "    [$($eclipse.Id)] Workspace '$WORKSPACE'"
  	$eclipse.Id >> "$OUTPUT_DIRECTORY\pids.txt"
}


cd $OLD_DIR