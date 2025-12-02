@echo off
setlocal enabledelayedexpansion

echo ========================================
echo   Defenders of Solara - Game Launcher
echo ========================================
echo.

REM Check if Java is installed
where java >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java is not installed or not in PATH.
    echo Please install Java JDK 8 or higher and try again.
    echo.
    pause
    exit /b 1
)

REM Check if Gradle wrapper exists
if not exist "gradlew.bat" (
    echo ERROR: gradlew.bat not found!
    echo Make sure you're running this from the project root directory.
    echo.
    pause
    exit /b 1
)

REM Check for force rebuild argument
set FORCE_REBUILD=0
if "%1"=="--rebuild" (
    set FORCE_REBUILD=1
    echo Force rebuild requested...
    echo.
)

REM Check if JAR file exists or if rebuild is requested
if not exist "core\build\libs\defenders-of-solara-1.0.0-all.jar" (
    set FORCE_REBUILD=1
)

if %FORCE_REBUILD%==1 (
    echo Building the game (this includes all dependencies including music library)...
    echo This may take a minute on first run...
    echo.
    
    REM Clean old build if it exists
    if exist "core\build\libs\defenders-of-solara-1.0.0-all.jar" (
        echo Removing old JAR file...
        del /q "core\build\libs\defenders-of-solara-1.0.0-all.jar" >nul 2>&1
    )
    
    REM Build the fat JAR with all dependencies
    call gradlew.bat core:fatJar --no-daemon
    if errorlevel 1 (
        echo.
        echo ERROR: Failed to build the game.
        echo.
        echo Possible issues:
        echo   - Java JDK 8+ is not installed
        echo   - Gradle wrapper is corrupted
        echo   - Network issues (downloading dependencies)
        echo   - Missing dependencies (including music library)
        echo.
        echo Try running: gradlew.bat core:fatJar --no-daemon
        echo.
        pause
        exit /b 1
    )
    
    REM Verify JAR was created
    if not exist "core\build\libs\defenders-of-solara-1.0.0-all.jar" (
        echo.
        echo ERROR: JAR file was not created after build.
        echo Check the build output above for errors.
        echo.
        pause
        exit /b 1
    )
    
    echo.
    echo Build complete! Starting game...
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

java -jar "core\build\libs\defenders-of-solara-1.0.0-all.jar"
set GAME_EXIT_CODE=%errorlevel%

if %GAME_EXIT_CODE% neq 0 (
    echo.
    echo ========================================
    echo   Game exited with error code: %GAME_EXIT_CODE%
    echo ========================================
    echo.
    echo Check the messages above for error details.
    echo.
    echo Common issues:
    echo   - Missing Java runtime
    echo   - Corrupted game files
    echo   - Missing music files in resources
    echo.
    pause
    exit /b %GAME_EXIT_CODE%
)

echo.
echo Game closed normally.
timeout /t 2 >nul

