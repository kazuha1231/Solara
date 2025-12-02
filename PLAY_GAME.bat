@echo off
echo ========================================
echo   Defenders of Solara - Game Launcher
echo ========================================
echo.

REM Check if JAR file exists
if not exist "core\build\libs\defenders-of-solara-1.0.0-all.jar" (
    echo JAR file not found. Building the game first...
    echo This may take a minute on first run...
    echo.
    call gradlew.bat core:fatJar
    if errorlevel 1 (
        echo.
        echo Error: Failed to build the game.
        echo Make sure Java and Gradle are properly installed.
        echo.
        pause
        exit /b 1
    )
    echo.
    echo Build complete! Starting game...
    echo.
)

REM Launch the game (keep console window open to see any error messages)
java -jar core\build\libs\defenders-of-solara-1.0.0-all.jar
if errorlevel 1 (
    echo.
    echo Game exited with an error. Check the messages above for details.
    echo.
    pause
)
if errorlevel 1 (
    echo.
    echo Error: Could not start the game.
    echo Make sure Java is installed and accessible.
    echo.
    pause
)

