@ECHO OFF

rem ------ ENVIRONMENT --------------------------------------------------------
rem The script depends on various environment variables to exist in order to
rem run properly. The java version we want to use, the location of the java
rem binaries (java home), and the project version as defined inside the pom.xml
rem file, e.g. 1.0-SNAPSHOT.
rem
rem PROJECT_VERSION: version used in pom.xml, e.g. 1.0-SNAPSHOT
rem APP_VERSION: the application version, e.g. 1.0.0, shown in "about" dialog

set JAVA_VERSION=25
set MAIN_JAR=tablefx-%PROJECT_VERSION%.jar

rem Set desired installer type: "exe" (requires WiX Toolset v3 on PATH), "msi" (requires WiX Toolset v3), "app-image".
set INSTALLER_TYPE=exe

rem ------ SETUP DIRECTORIES AND FILES ----------------------------------------
rem Remove previously generated java runtime and installers. Copy all required
rem jar files into the input/libs folder.

IF EXIST target\java-runtime rmdir /S /Q  .\target\java-runtime
IF EXIST target\installer rmdir /S /Q target\installer

xcopy /S /Q target\libs\* target\installer\input\libs\
copy target\%MAIN_JAR% target\installer\input\libs\

rem ------ REQUIRED MODULES ---------------------------------------------------
rem Use jlink to detect all modules that are required to run the application.
rem Starting point for the jdep analysis is the set of jars being used by the
rem application.

echo detecting required modules

"%JAVA_HOME%\bin\jdeps" ^
  -q ^
  --multi-release %JAVA_VERSION% ^
  --ignore-missing-deps ^
  --class-path "target\installer\input\libs\*" ^
  --print-module-deps target\classes\eus\ehu\TableUI.class > temp.txt

set /p detected_modules=<temp.txt

echo detected modules: %detected_modules%

rem ------ MANUAL MODULES -----------------------------------------------------
rem jdk.crypto.ec has to be added manually bound via --bind-services or
rem otherwise HTTPS does not work.
rem
rem See: https://bugs.openjdk.java.net/browse/JDK-8221674
rem
rem In addition we need jdk.localedata if the application is localized.
rem This can be reduced to the actually needed locales via a jlink parameter,
rem e.g., --include-locales=en,de.
rem
rem Don't forget the leading ','!

set manual_modules=,jdk.crypto.ec,jdk.localedata
echo manual modules: %manual_modules%

rem ------ JAVAFX MODULES -----------------------------------------------------
rem JavaFX modules are not JDK modules and must be explicitly added to the
rem runtime image via --module-path pointing to the JavaFX JAR files.

set JAVAFX_MODULE_PATH=target/installer/input/libs/javafx-controls-21.0.2-win.jar;target/installer/input/libs/javafx-fxml-21.0.2-win.jar;target/installer/input/libs/javafx-graphics-21.0.2-win.jar;target/installer/input/libs/javafx-base-21.0.2-win.jar
set JAVAFX_MODULES=,javafx.base,javafx.controls,javafx.fxml,javafx.graphics
echo javafx modules: %JAVAFX_MODULES%

rem ------ RUNTIME IMAGE ------------------------------------------------------
rem Use the jlink tool to create a runtime image for our application. We are
rem doing this in a separate step instead of letting jlink do the work as part
rem of the jpackage tool. This approach allows for finer configuration and also
rem works with dependencies that are not fully modularized, yet.

echo creating java runtime image

call "%JAVA_HOME%\bin\jlink" ^
  --strip-native-commands ^
  --no-header-files ^
  --no-man-pages ^
  --strip-debug ^
  --module-path "%JAVAFX_MODULE_PATH%" ^
  --add-modules %detected_modules%%manual_modules%%JAVAFX_MODULES% ^
  --include-locales=en,de ^
  --output target/java-runtime


rem ------ PACKAGING ----------------------------------------------------------
rem In the end we will find the package inside the target/installer directory.

call "%JAVA_HOME%\bin\jpackage" ^
  --type %INSTALLER_TYPE% ^
  --dest target/installer ^
  --input target/installer/input/libs ^
  --name DemoFX ^
  --main-class eus.ehu.TableUI ^
  --main-jar %MAIN_JAR% ^
  --java-options -Xmx2048m ^
  --runtime-image target/java-runtime ^
  --icon src/main/logo/windows/duke.ico ^
  --app-version %APP_VERSION% ^
  --vendor "Euskal Herriko Unibertsitatea" ^
  --copyright "Copyright © 2026 Euskal Herriko Unibertsitatea" ^
  --win-dir-chooser ^
  --win-shortcut ^
  --win-per-user-install ^
  --win-menu
