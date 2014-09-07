function Find-ChildProcess {
param($ID=$PID)

$CustomColumnID = @{
	Name = 'Id'
	Expression = { [Int[]]$_.ProcessID }
}

$result = Get-WmiObject -Class Win32_Process -Filter "ParentProcessID=$ID" |
Select-Object -Property ProcessName, $CustomColumnID, CommandLine

$result
$result | Where-Object { $_.ID -ne $null } | ForEach-Object {
	Find-ChildProcess -id $_.Id
}
}


$OUTPUT_DIRECTORY="$HOME\emoflon_autotest_tmp_dir"
$filename = "$OUTPUT_DIRECTORY\pids.txt"
foreach ($id in [System.IO.File]::ReadLines($filename)) {
   #echo "Stopping $id"
   #Stop-Process $id
   
   $child = Find-ChildProcess $id
	(Get-Process -Id $child.Id).CloseMainWindow()
}