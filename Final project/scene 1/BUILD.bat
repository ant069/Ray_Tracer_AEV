@echo off
REM ═══════════════════════════════════════════════════════════════════
REM SCENE 1 - CRYSTAL GEOMETRY - Build & Render Script
REM ═══════════════════════════════════════════════════════════════════

setlocal enabledelayedexpansion

cd /d "c:\Users\aenri\Downloads\Ray_Tracer_AEV\Final project\scene 1\src"

echo.
echo ╔═══════════════════════════════════════════════════════════════╗
echo ║         SCENE 1: CRYSTAL GEOMETRY - Ray Tracer               ║
echo ║                  Build & Render Script                        ║
echo ╚═══════════════════════════════════════════════════════════════╝
echo.

REM Preguntar al usuario qué quiere hacer
echo [1] Preview (1024x576 - Rápido ~1 segundo)
echo [2] Final Render (4096x2160 - 8K ~10 minutos)
echo.
set /p choice=Selecciona una opción [1 o 2]: 

if "%choice%"=="1" (
    echo.
    echo [Info] Compilando para PREVIEW...
    javac -cp . *.java
    if errorlevel 1 (
        echo [Error] Fallo en compilación
        pause
        exit /b 1
    )
    echo [Info] Ejecutando PREVIEW (1024x576)...
    java -cp . App_Scene1
    echo.
    echo [OK] Imagen guardada en: output\Scene_1_Preview_1024x576.png
) else if "%choice%"=="2" (
    echo.
    echo [Info] Modificando código para final render (4096x2160)...
    REM Reemplazar isFinalRender = false por isFinalRender = true
    powershell -Command "(Get-Content App_Scene1.java) -replace 'isFinalRender = false', 'isFinalRender = true' | Set-Content App_Scene1.java"
    
    echo [Info] Compilando para FINAL RENDER...
    javac -cp . *.java
    if errorlevel 1 (
        echo [Error] Fallo en compilación
        REM Revertir los cambios
        powershell -Command "(Get-Content App_Scene1.java) -replace 'isFinalRender = true', 'isFinalRender = false' | Set-Content App_Scene1.java"
        pause
        exit /b 1
    )
    
    echo [Info] Ejecutando FINAL RENDER (4096x2160)...
    echo [Adviso] Esto puede tomar 5-15 MINUTOS. Por favor, espera...
    echo.
    java -cp . App_Scene1
    
    REM Revertir los cambios para la próxima vez
    powershell -Command "(Get-Content App_Scene1.java) -replace 'isFinalRender = true', 'isFinalRender = false' | Set-Content App_Scene1.java"
    
    echo.
    echo [OK] Imagen final guardada en: output\Scene_1_FINAL_4096x2160.png
) else (
    echo [Error] Opción inválida
    pause
    exit /b 1
)

echo.
echo ╔═══════════════════════════════════════════════════════════════╗
echo ║                   ✓ Proceso completado                       ║
echo ╚═══════════════════════════════════════════════════════════════╝
echo.
pause
