@echo off
echo Wraith Mod Build
echo ================

where gradle >nul 2>&1
if %ERRORLEVEL% == 0 (
    gradle build
    goto done
)

where gradlew >nul 2>&1
if %ERRORLEVEL% == 0 (
    call gradlew.bat build
    goto done
)

echo.
echo ERROR: Gradle not found.
echo.
echo Option 1: Install Gradle via winget:
echo   winget install Gradle.Gradle
echo.
echo Option 2: Download from https://gradle.org/install/
echo.
pause
exit /b 1

:done
if %ERRORLEVEL% == 0 (
    echo.
    echo Build successful!
    echo WraithMod.jar copied to ..\publish\WraithMod.jar
    echo Place it next to WraithClient.exe and launch the game.
) else (
    echo.
    echo Build FAILED. Check output above.
)
pause
