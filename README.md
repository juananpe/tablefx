# DemoFX — Native Installer Build Guide

A JavaFX desktop application packaged into native installers using **jlink** + **jpackage**.

## Prerequisites

- **JDK 21 or later** (a full JDK, not just a JRE — `jlink` and `jpackage` are required)
- **Maven 3.6+**
- **macOS only**: Xcode command line tools (for `.pkg`/`.dmg` packaging)
- **Linux only**: `rpm-build` or `dpkg-deb` (depending on chosen installer type)
- **Windows only**: Nothing extra for `.exe` installers. [WiX Toolset v3](https://github.com/wixtoolset/wix3/releases) is only needed for `.msi` installers (see [Windows details](#windows) below).

Set `JAVA_HOME` to your JDK installation:

```bash
# macOS — find available JDKs:
/usr/libexec/java_home -V

# Then export:
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

## Quick Start

```bash
# Build everything + create native installer (auto-detects your OS):
mvn clean install
```

On macOS this produces a `.pkg`; on Linux an `.rpm`; on Windows an `.exe`.

The installer lands in `target/installer/`.

## What Happens During Build

| Phase | Tool | What it does |
|-------|------|--------------|
| `compile` | `javac` | Compiles sources with `--release 21` |
| `package` | `maven-jar-plugin` | Creates `tablefx-<version>.jar` |
| `copy-dependencies` | `maven-dependency-plugin` | Copies all runtime JARs into `target/libs/` |
| `jdeps` | `jdeps` | Detects the minimal set of JDK modules needed |
| `jlink` | `jlink` | Creates a stripped-down custom JRE in `target/java-runtime/` |
| `jpackage` | `jpackage` | Bundles everything into a native installer |

## Platform-Specific Details

### macOS

- **Installer type**: `.pkg` (default) — change to `.dmg` in `build_app_mac.sh`:
  ```bash
  INSTALLER_TYPE=dmg
  ```
- **Icon**: `src/main/logo/macosx/duke.icns` — replace with your own `.icns` file
- **Package identifier**: `eus.ehu.tablefx`

### Linux

- **Installer type**: `.rpm` (default) — change to `deb` or `app-image` in `build_app_linux.sh`:
  ```bash
  INSTALLER_TYPE=deb
  ```
- **Icon**: `src/main/logo/linux/duke.png`

### Windows

- **Installer type**: `exe` is the simplest — no extra tools needed. Change in `build_app_windows.bat`:
  ```bat
  REM No WiX required — works out of the box:
  set INSTALLER_TYPE=exe

  REM Or, if WiX Toolset v3 is installed:
  set INSTALLER_TYPE=msi
  ```
- **`.msi` requires WiX**: Download **WiX Toolset v3** from [github.com/wixtoolset/wix3/releases](https://github.com/wixtoolset/wix3/releases). Install it and ensure `candle.exe` and `light.exe` are on your `PATH`.
- **App image** (no installer): `set INSTALLER_TYPE=app-image` produces a plain folder.
- **Icon**: `src/main/logo/windows/duke.ico`
- Adds Start Menu shortcut and desktop shortcut by default

## Customization

All branding lives in the build scripts (`build_app_mac.sh`, etc.):

| Setting | Variable / Flag |
|---------|-----------------|
| App name | `--name DemoFX` |
| App version | `APP_VERSION` env var (defaults to `client.version` from `pom.xml`) |
| Vendor | `--vendor "..."` |
| Copyright | `--copyright "..."` |
| macOS package ID | `--mac-package-identifier` |
| Max heap size | `--java-options -Xmx2048m` |
| Locales | `--include-locales=en,de` in `jlink` |
| Icon | `--icon src/main/logo/...` |

### Changing the Icon

Replace the placeholder files under `src/main/logo/`:

| Platform | Format | Path |
|----------|--------|------|
| macOS | `.icns` | `src/main/logo/macosx/` |
| Linux | `.png` | `src/main/logo/linux/` |
| Windows | `.ico` | `src/main/logo/windows/` |

Update the `--icon` path in the corresponding build script.

## Running Without Packaging

To run the app directly during development:

```bash
mvn clean javafx:run
```

## Troubleshooting

- **`JAVA_HOME` not set**: The build scripts require `JAVA_HOME` to point to a full JDK. Run `export JAVA_HOME=...` first.
- **`jdeps` fails**: Make sure the project compiles cleanly first (`mvn compile`).
- **`jpackage` fails on macOS**: Ensure Xcode command line tools are installed (`xcode-select --install`).
- **`jpackage` fails on Windows**: Install [WiX Toolset v3](https://github.com/wixtoolset/wix3/releases).
- **Missing JavaFX native jars**: The `maven-dependency-plugin` copies platform-specific jars automatically.
