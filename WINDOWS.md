# Windows Installer Build Guide

This guide explains how to build the TableFX Windows `.exe` installer.

## Prerequisites

### 1. JDK 21 or later

**Check if already installed:**

```powershell
java --version
# Should print something like: openjdk 21.0.x 2024-xx-xx
#                              or: openjdk 25.0.2 2026-xx-xx
```

Also verify `JAVA_HOME` is set:

```powershell
$env:JAVA_HOME
# Should print the JDK path, e.g. C:\Users\juanan\se1\jdk-25.0.2
# If blank, set it (see below).
```

> If `java` is not recognized, check `C:\Program Files\`, `C:\Program Files (x86)\`, or common locations like `C:\Users\<name>\se1\`.  
> The project compiles with `--release 21` so any JDK 21–25 works.

**If missing — install:**

Download and install [JDK 21+](https://jdk.java.net/21/) or [JDK 25](https://jdk.java.net/25/).  
Set the `JAVA_HOME` environment variable:

```powershell
# System-wide (admin PowerShell):
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\path\to\jdk-21", "Machine")

# Per-session:
$env:JAVA_HOME = "C:\path\to\jdk-21"
```

> **Note:** The build script sets `JAVA_VERSION=25` for `jdeps --multi-release`. If you use JDK 21, edit `build_app_windows.bat` and change `set JAVA_VERSION=25` to `set JAVA_VERSION=21` (or your JDK major version).

### 2. Maven

**Check if already installed:**

```powershell
mvn --version
# Should print: Apache Maven x.x.x  ...  Java version: 21.x.x (or 25.x.x)
```

If you see a version and it shows the correct Java version under "Java version", you're all set.

**If missing — install:**

```powershell
# Via Chocolatey:
choco install maven -y

# Or download manually from https://maven.apache.org/download.cgi
# and add its bin\ folder to your PATH.
```

Verify again with `mvn --version`.

### 3. WiX Toolset v3

**Check if already installed:**

```powershell
light.exe -?
# Should print: Windows Installer XML Toolset Linker version 3.x.x
# If not recognized, WiX is missing or not on PATH.
```

Common install location: `C:\Program Files (x86)\WiX Toolset v3.14\bin\`

**If missing — install** from an **administrator** PowerShell:

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
