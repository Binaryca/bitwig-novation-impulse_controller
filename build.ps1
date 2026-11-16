#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Builds the Novation Impulse Bitwig extension.

.DESCRIPTION
    Looks for a JDK and Maven in JAVA_HOME/MAVEN_HOME, on PATH, or in
    ~/.local/tools. The build output is target/NovationImpulse.bwextension.

.PARAMETER Install
    Copy the built extension to the Bitwig Studio Extensions directory.

.PARAMETER SkipTests
    Pass -DskipTests to Maven.

.EXAMPLE
    ./build.ps1
    ./build.ps1 -Install
#>
[CmdletBinding()]
param(
    [switch] $Install,
    [switch] $SkipTests
)

$ErrorActionPreference = 'Stop'

# Resolve directory junctions (for example a localized Documents folder).
# javac on Windows cannot read a classpath containing such a junction.
function Resolve-RealPath([string] $path) {
    $resolved = (Resolve-Path -LiteralPath $path).Path
    $item = Get-Item -LiteralPath $resolved -Force
    while ($item.Target) {
        $resolved = @($item.Target)[0]
        $item = Get-Item -LiteralPath $resolved -Force
    }
    return $resolved
}

$repoRoot = Resolve-RealPath (Split-Path -Parent $MyInvocation.MyCommand.Path)

function Find-JavaHome {
    $candidates = @()

    if ($env:JAVA_HOME) {
        $candidates += $env:JAVA_HOME
    }

    $toolsDir = Join-Path $HOME '.local/tools'
    if (Test-Path $toolsDir) {
        $candidates += Get-ChildItem $toolsDir -Directory -Filter 'jdk-*' -ErrorAction SilentlyContinue |
            Sort-Object Name -Descending |
            ForEach-Object { $_.FullName }
    }

    foreach ($base in @('C:\Program Files\Eclipse Adoptium', 'C:\Program Files\Java', 'C:\Program Files\Microsoft')) {
        if (Test-Path $base) {
            $candidates += Get-ChildItem $base -Directory -ErrorAction SilentlyContinue |
                Sort-Object Name -Descending |
                ForEach-Object { $_.FullName }
        }
    }

    $javac = Get-Command javac.exe -ErrorAction SilentlyContinue
    if ($javac) {
        $candidates += Split-Path -Parent (Split-Path -Parent $javac.Source)
    }

    foreach ($candidate in $candidates) {
        if ($candidate -and (Test-Path (Join-Path $candidate 'bin/javac.exe'))) {
            return $candidate
        }
    }

    throw 'No JDK found. Set JAVA_HOME or install a JDK (8 or newer).'
}

function Find-Maven {
    $candidates = @()

    if ($env:MAVEN_HOME) {
        $candidates += $env:MAVEN_HOME
    }

    $toolsDir = Join-Path $HOME '.local/tools'
    if (Test-Path $toolsDir) {
        $candidates += Get-ChildItem $toolsDir -Directory -Filter 'apache-maven-*' -ErrorAction SilentlyContinue |
            Sort-Object Name -Descending |
            ForEach-Object { $_.FullName }
    }

    $mvn = Get-Command mvn.cmd -ErrorAction SilentlyContinue
    if (-not $mvn) {
        $mvn = Get-Command mvn -ErrorAction SilentlyContinue
    }
    if ($mvn) {
        $candidates += $mvn.Source
    }

    foreach ($candidate in $candidates) {
        if (-not $candidate) {
            continue
        }

        if (Test-Path $candidate -PathType Leaf) {
            return $candidate
        }

        $mvnCmd = Join-Path $candidate 'bin/mvn.cmd'
        if (Test-Path $mvnCmd) {
            return $mvnCmd
        }
    }

    throw 'No Maven found. Set MAVEN_HOME or install Maven 3.9+.'
}

$javaHome = Find-JavaHome
$javacVersion = (& (Join-Path $javaHome 'bin/javac.exe') -version 2>&1 | Out-String)
if ($javacVersion -match 'javac\s+(\d+)') {
    $majorVersion = [int] $matches[1]
    if ($majorVersion -lt 11) {
        throw "JDK 11 or newer is required (found $majorVersion). Set JAVA_HOME to a newer JDK."
    }
}

$env:JAVA_HOME = $javaHome
$env:PATH = (Join-Path $javaHome 'bin') + [IO.Path]::PathSeparator + $env:PATH

$maven = Find-Maven

Write-Host "Java:  $javaHome"
Write-Host "Maven: $maven"

$arguments = @('-B', 'clean', 'package')
if ($SkipTests) {
    $arguments += '-DskipTests'
}

Push-Location $repoRoot
try {
    & $maven @arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Maven build failed with exit code $LASTEXITCODE."
    }
}
finally {
    Pop-Location
}

$extension = Join-Path $repoRoot 'target/NovationImpulse.bwextension'
Write-Host ''
Write-Host "Built: $extension"

if ($Install) {
    $destinationDir = Join-Path $HOME 'Documents/Bitwig Studio/Extensions'
    New-Item -ItemType Directory -Force -Path $destinationDir | Out-Null
    Copy-Item $extension $destinationDir -Force
    Write-Host "Installed: $(Join-Path $destinationDir 'NovationImpulse.bwextension')"
    Write-Host 'Restart Bitwig Studio (or reload the controller script) to use it.'
}
