@echo off

rem Setup directories
if not exist build\classes mkdir build\classes
if not exist build\sketches\src mkdir build\sketches\src
if not exist build\sketches\bin mkdir build\sketches\bin

rem Compile everything
echo Compiling Jrocessing...
javac -d build\classes -sourcepath src src\jrocessing\app\JrocessingIDE.java src\jrocessing\core\JApplet.java src\jrocessing\core\JVector.java src\jrocessing\core\JImage.java src\jrocessing\preproc\Preprocessor.java src\jrocessing\app\Runner.java
if %errorlevel% neq 0 exit /b %errorlevel%

rem Run IDE if requested
if "%1"=="run" (
    echo Launching Jrocessing IDE...
    java -cp build\classes jrocessing.app.JrocessingIDE
)
