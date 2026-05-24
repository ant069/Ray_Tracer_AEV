# ═══════════════════════════════════════════════════════════════════
# SCENE 1 - KOENIGSEGG SHOWROOM - PowerShell Build & Render Script
# ═══════════════════════════════════════════════════════════════════

$SceneDir = "c:\Users\aenri\Downloads\Ray_Tracer_AEV\Final project\scene 1\src"
Set-Location $SceneDir

Write-Host "`n╔═══════════════════════════════════════════════════════════════╗"
Write-Host "║         SCENE 1: KOENIGSEGG SHOWROOM - Ray Tracer               ║"
Write-Host "║                  Build & Render Script                        ║"
Write-Host "╚═══════════════════════════════════════════════════════════════╝`n"

Write-Host "[1] Preview (1024x576 - Rápido ~1 segundo)" -ForegroundColor Cyan
Write-Host "[2] Final Render (4096x2160 - 8K ~10 minutos)" -ForegroundColor Cyan
Write-Host ""

$choice = Read-Host "Selecciona una opción [1 o 2]"

if ($choice -eq "1") {
    Write-Host "`n[Info] Compilando para PREVIEW..." -ForegroundColor Yellow
    & javac -cp . *.java
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[Error] Fallo en compilación" -ForegroundColor Red
        exit 1
    }
    
    Write-Host "[Info] Ejecutando PREVIEW (1024x576)..." -ForegroundColor Yellow
    & java -cp . App_Scene1
    
    Write-Host "`n[OK] Imagen guardada en: output\Scene_1_Preview_1024x576.png" -ForegroundColor Green
    
} elseif ($choice -eq "2") {
    Write-Host "`n[Info] Modificando código para final render (4096x2160)..." -ForegroundColor Yellow
    
    # Reemplazar isFinalRender = false por isFinalRender = true
    $content = Get-Content App_Scene1.java
    $content = $content -replace 'isFinalRender = false', 'isFinalRender = true'
    $content | Set-Content App_Scene1.java
    
    Write-Host "[Info] Compilando para FINAL RENDER..." -ForegroundColor Yellow
    & javac -cp . *.java
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[Error] Fallo en compilación" -ForegroundColor Red
        # Revertir cambios
        $content = Get-Content App_Scene1.java
        $content = $content -replace 'isFinalRender = true', 'isFinalRender = false'
        $content | Set-Content App_Scene1.java
        exit 1
    }
    
    Write-Host "[Info] Ejecutando FINAL RENDER (4096x2160)..." -ForegroundColor Yellow
    Write-Host "[Adviso] Esto puede tomar 5-15 MINUTOS. Por favor, espera..." -ForegroundColor Magenta
    Write-Host ""
    
    $startTime = Get-Date
    & java -cp . App_Scene1
    $endTime = Get-Date
    $totalTime = ($endTime - $startTime).TotalSeconds
    
    # Revertir cambios
    $content = Get-Content App_Scene1.java
    $content = $content -replace 'isFinalRender = true', 'isFinalRender = false'
    $content | Set-Content App_Scene1.java
    
    Write-Host "`n[OK] Imagen final guardada en: output\Scene_1_FINAL_4096x2160.png" -ForegroundColor Green
    Write-Host "[Info] Tiempo total: $([Math]::Round($totalTime, 2)) segundos" -ForegroundColor Gray
    
} else {
    Write-Host "[Error] Opción inválida" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "╔═══════════════════════════════════════════════════════════════╗"
Write-Host "║                   ✓ Proceso completado                       ║"
Write-Host "╚═══════════════════════════════════════════════════════════════╝`n"

# Abrir la carpeta output automáticamente
Start-Process explorer.exe -ArgumentList "output"
