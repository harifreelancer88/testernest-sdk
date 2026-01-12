param(
    [string]$GradleRoot = ".."
)

$ErrorActionPreference = "Stop"

$RepoRoot = (Resolve-Path "$PSScriptRoot\..").Path
if (-not $PSBoundParameters.ContainsKey("GradleRoot")) {
    $GradleRoot = $RepoRoot
} else {
    $GradleRoot = (Resolve-Path $GradleRoot).Path
}
Push-Location $RepoRoot
try {
    .\gradlew :testernest-android:assembleRelease :testernest-core:assembleRelease
} finally {
    Pop-Location
}

$srcAndroid = Join-Path $GradleRoot "testernest-android/build/outputs/aar/testernest-android-release.aar"
$srcCore = Join-Path $GradleRoot "testernest-core/build/outputs/aar/testernest-core-release.aar"
$destDir = Join-Path $PSScriptRoot "Assets/Testernest/Plugins/Android"

New-Item -ItemType Directory -Force -Path $destDir | Out-Null

Copy-Item -Force $srcAndroid $destDir
Copy-Item -Force $srcCore $destDir

Write-Host "Copied AARs to $destDir"
