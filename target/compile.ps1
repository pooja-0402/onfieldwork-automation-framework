$m2Jars = @()
if (Test-Path "C:\Users\admin\.m2\repository") {
    $m2Jars = Get-ChildItem "C:\Users\admin\.m2\repository" -Recurse -Filter "*.jar" -ErrorAction SilentlyContinue | ForEach-Object { $_.FullName }
}
$libJars = @()
if (Test-Path "lib") {
    $libJars = Get-ChildItem "lib" -Recurse -Filter "*.jar" -ErrorAction SilentlyContinue | ForEach-Object { $_.FullName }
}
$allJars = $m2Jars + $libJars
$cp = ($allJars + @("target\classes", "target\test-classes", ".")) -join ";"
Write-Host "Classpath JAR count: $($allJars.Count)"
if (!(Test-Path "target\test-classes")) {
    New-Item -ItemType Directory -Path "target\test-classes" -Force | Out-Null
}
Write-Host "Compiling src\test\java\*.java..."
javac -encoding UTF-8 -cp $cp -d target\test-classes src\test\java\*.java
$exit1 = $LASTEXITCODE

Write-Host "Compiling auxiliary and runner Java files..."
$auxFiles = @("TestLoginScenarios.java", "TestMarcoAIoTLogin.java", "target\InspectProjectCard.java", "target\InspectErrorPage.java", "target\CaptureError.java", "target\RunFullInfraSuite.java") | Where-Object { Test-Path $_ }
if ($auxFiles.Count -gt 0) {
    javac -encoding UTF-8 -cp $cp -d target\test-classes $auxFiles
}
$exit2 = $LASTEXITCODE

$finalExit = [Math]::Max($exit1, $exit2)
Write-Host "Compilation complete. Overall Exit code: $finalExit"
exit $finalExit
