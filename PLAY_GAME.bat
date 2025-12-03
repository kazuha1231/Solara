@echo off
cd /d "%~dp0"

echo ========================================
echo   Defenders of Solara - Game Launcher
echo ========================================
echo.

REM Check if Java is installed
echo Checking for Java...
java -version >nul 2>&1
if errorlevel 1 (
    echo.
    echo ========================================
    echo   ERROR: Java is not installed or not in PATH.
    echo ========================================
    echo.
    echo Please install Java JDK 8 or higher and try again.
    echo You can download Java from: https://adoptium.net/
    echo.
    pause
    exit /b 1
)
echo Java found and ready.
echo.

REM Check if Gradle wrapper exists
if not exist gradlew.bat (
    echo.
    echo ========================================
    echo   ERROR: gradlew.bat not found!
    echo ========================================
    echo.
    echo Make sure you are running this from the project root directory.
    echo Current directory: %CD%
    echo.
    pause
    exit /b 1
)

REM Check for force rebuild argument and JAR file
set JAR_PATH=core\build\libs\defenders-of-solara-1.0.0-all.jar
set NEED_BUILD=0

if "%1"=="--rebuild" (
    set NEED_BUILD=1
    echo Force rebuild requested...
    echo.
)

if not exist "%JAR_PATH%" (
    set NEED_BUILD=1
)

REM Build if needed
if "%NEED_BUILD%"=="1" (
    if "%1" neq "--rebuild" (
        echo JAR file not found. Building game...
        echo.
    )
    echo Building the game (this includes all dependencies including music library)...
    echo This may take a minute on first run...
    echo.
    
    REM Clean old build if it exists
    if exist "%JAR_PATH%" (
        echo Removing old JAR file...
        del /q "%JAR_PATH%" 2>nul
    )
    
    REM Build the fat JAR with all dependencies
    echo Running Gradle build...
    call gradlew.bat core:fatJar --no-daemon
    if errorlevel 1 (
        echo.
        echo ========================================
        echo   ERROR: Failed to build the game.
        echo ========================================
        echo.
        echo Possible issues:
        echo   - Java JDK 8+ is not installed or not in PATH
        echo   - Gradle wrapper is corrupted
        echo   - Network issues (downloading dependencies)
        echo   - Missing dependencies (including music library)
        echo   - Insufficient disk space
        echo.
        echo Try running manually: gradlew.bat core:fatJar --no-daemon
        echo.
        pause
        exit /b 1
    )
    
    REM Verify JAR was created
    if not exist "%JAR_PATH%" (
        echo.
        echo ========================================
        echo   ERROR: JAR file was not created after build.
        echo ========================================
        echo.
        echo Check the build output above for errors.
        echo Expected location: %JAR_PATH%
        echo.
        pause
        exit /b 1
    )
    
    echo.
    echo ========================================
    echo   Build complete! Starting game...
    echo ========================================
    echo.
) else (
    echo JAR file found. Launching game...
    echo (Use --rebuild argument to force a rebuild)
    echo.
)

REM Launch the game
echo ========================================
echo   Starting Defenders of Solara...
echo ========================================
echo.

REM Verify JAR exists before attempting to run
if not exist "%JAR_PATH%" (
    echo.
    echo ========================================
    echo   ERROR: JAR file not found.
    echo ========================================
    echo.
    echo Expected location: %JAR_PATH%
    echo Current directory: %CD%
    echo.
    echo Please run this script from the project root directory.
    echo Or use --rebuild to build the game.
    echo.
    pause
    exit /b 1
)

REM Run the game
echo Launching game...
echo.
java -jar "%JAR_PATH%"
set GAME_EXIT_CODE=%ERRORLEVEL%

if not "%GAME_EXIT_CODE%"=="0" (
    echo.
    echo ========================================
    echo   Game exited with an error (code: %GAME_EXIT_CODE%)
    echo ========================================
    echo.
    echo Check the messages above for error details.
    echo.
    echo Common issues:
    echo   - Missing Java runtime or wrong version
    echo   - Corrupted game files
    echo   - Missing music files in resources
    echo   - Display/graphics driver issues
    echo   - Settings file corruption (delete settings.properties to reset)
    echo.
    echo If settings are corrupted, delete: settings.properties
    echo.
    pause
    exit /b %GAME_EXIT_CODE%
)

echo.
echo ========================================
echo   Game closed normally.
echo ========================================
echo.
pause
