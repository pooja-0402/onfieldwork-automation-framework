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
if (!(Test-Path "target\test-classes")) {
    New-Item -ItemType Directory -Path "target\test-classes" -Force | Out-Null
}
javac -encoding UTF-8 -cp $cp -d target\test-classes target\InspectProjectCard.java
java -cp $cp InspectProjectCard
