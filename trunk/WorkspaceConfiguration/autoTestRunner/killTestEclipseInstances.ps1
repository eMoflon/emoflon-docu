echo "Does not work yet, because Eclipse keeps running, even after stopping its process"
exit

$OUTPUT_DIRECTORY="$HOME\emoflon_autotest_tmp_dir"
$filename = "$OUTPUT_DIRECTORY\pids.txt"
foreach ($id in [System.IO.File]::ReadLines($filename)) {
   echo "Stopping $id"
   Stop-Process $id
}