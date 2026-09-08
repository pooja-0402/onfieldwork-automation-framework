param(
    [string]$TestClass = "TestInfraProjectScenarios"
)
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
Write-Host "Running Test Class: $TestClass"
java -cp $cp $TestClass
