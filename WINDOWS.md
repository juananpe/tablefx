# Windows Installer Build Guide

This guide explains how to build the TableFX Windows `.exe` installer.

## Prerequisites

### 1. JDK 25

Download and install [JDK 25](https://jdk.java.net/25/) (or later).  
Set the `JAVA_HOME` environment variable system-wide or per-session:

```powershell
# System-wide (admin PowerShell):
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\path\to\jdk-25", "Machine")

# Per-session:
$env:JAVA_HOME = "C:\path\to\jdk-25"
```

### 2. Maven

Install Maven to build the project JAR and manage dependencies.

```powershell
# Via Chocolatey:
choco install maven -y

# Or download manually from https://maven.apache.org/download.cgi
# and add its bin\ folder to your PATH.
```

Verify:
```powershell
mvn --version
# Should show Java 25 and Maven home
```

### 3. WiX Toolset v3

Required by `jpackage --type exe` to produce the `.exe` installer.  
Install from an **administrator** PowerShell:

```powershell
# Via Chocolatey (admin shell):
choco install wixtoolset -y
```

WiX will install to `C:\Program Files (x86)\WiX Toolset v3.14\`.  
Add it to PATH:

```powershell
# System-wide (admin PowerShell):
[Environment]::SetEnvironmentVariable("PATH", [Environment]::GetEnvironmentVariable("PATH", "Machine") + ";C:\Program Files (x86)\WiX Toolset v3.14\bin", "Machine")

# Per-session:
$env:PATH = "C:\Program Files (x86)\WiX Toolset v3.14\bin;$env:PATH"
```

Verify:
```powershell
light.exe -?
# Should print: "Windows Installer XML Toolset Linker version 3.14.x"
```

## Build

### 1. Compile and package JAR

```powershell
$env:JAVA_HOME = "C:\path\to\jdk-25"
$env:PATH    = "$env:JAVA_HOME\bin;$env:PATH"

mvn package
```

This creates `target\tablefx-1.0-SNAPSHOT.jar` and copies JavaFX dependencies into `target\libs\`.

### 2. Run the installer build script

```powershell
$env:JAVA_HOME       = "C:\path\to\jdk-25"
$env:PROJECT_VERSION = "1.0-SNAPSHOT"
$env:APP_VERSION     = "1.0.0"
$env:PATH            = "$env:JAVA_HOME\bin;C:\Program Files (x86)\WiX Toolset v3.14\bin;$env:PATH"

cmd /c build_app_windows.bat
```

> **Note:** The script must be run via `cmd /c` because it's a `.bat` file.  
> It will take 1–2 minutes to complete (jlink + jpackage).

## Output

```
target\installer\
├── DemoFX-1.0.0.exe   ← The Windows installer (≈42 MB)
└── DemoFX\              ← Standalone app-image (optional, for testing)
    ├── DemoFX.exe
    ├── app\
    └── runtime\
```

### Installer features

- Per-user install (no admin rights needed)
- Choose install directory
- Start Menu shortcut
- Bundled Java 25 runtime + JavaFX — no Java install required on target machine

## How the script works

`build_app_windows.bat` performs three steps automatically:

| Step | Tool | What it does |
|------|------|--------------|
| 1. Detect modules | `jdeps` | Scans the app classes to find required JDK modules |
| 2. Runtime image | `jlink` | Creates a trimmed Java runtime with the required modules + JavaFX |
| 3. Packaging | `jpackage` | Bundles the app + runtime into a `.exe` installer using WiX |

> JavaFX modules (`javafx.base`, `javafx.controls`, `javafx.fxml`, `javafx.graphics`) are added **explicitly** because `jdeps --print-module-deps` only detects JDK modules, not third-party ones.

## Troubleshooting

### `mvn` not found
Make sure Maven is installed and its `bin` folder is on your PATH.  
If you just installed it, restart your terminal or run `refreshenv`.

### `Error: Invalid or unsupported type: [exe]`
This means WiX Toolset v3 is not on your PATH.  
Verify with `light.exe -?` and add the WiX `bin` folder to PATH.

### App exits silently when run
The JavaFX modules are missing from the runtime image.  
Ensure the `build_app_windows.bat` includes the `JAVAFX_MODULE_PATH` and `JAVAFX_MODULES` variables in the `jlink` command.

### `Access denied` during WiX install
Run the Chocolatey command from an **elevated** (Run as Administrator) PowerShell.
