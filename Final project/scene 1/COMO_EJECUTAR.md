# 📋 GUÍA: Cómo Ejecutar Scene 1 desde Terminal

## Opción A: Script Automático (RECOMENDADO)

### En Windows PowerShell:
```powershell
# 1. Abre PowerShell
# 2. Ejecuta este comando:
cd "c:\Users\aenri\Downloads\Ray_Tracer_AEV\Final project\scene 1"
powershell -ExecutionPolicy Bypass -File build.ps1

# 3. Selecciona la opción (1 para preview, 2 para final)
# 4. ¡Espera a que termmine!
```

### En Command Prompt (CMD):
```batch
# 1. Abre Command Prompt (cmd.exe)
# 2. Ejecuta:
cd "c:\Users\aenri\Downloads\Ray_Tracer_AEV\Final project\scene 1"
BUILD.bat

# 3. Selecciona la opción (1 para preview, 2 para final)
# 4. ¡Espera a que termine!
```

---

## Opción B: Comandos Manuales

### PREVIEW (Rápido - 1024x576):

**Paso 1: Navegar a la carpeta**
```powershell
cd "c:\Users\aenri\Downloads\Ray_Tracer_AEV\Final project\scene 1\src"
```

**Paso 2: Compilar**
```powershell
javac -cp . *.java
```

**Paso 3: Ejecutar**
```powershell
java -cp . App_Scene1
```

**Resultado esperado:**
```
╔════════════════════════════════════════════════════════════╗
║          SCENE 1: CRYSTAL GEOMETRY - Ray Tracer            ║
║                  Resolución: 1024x576
╚════════════════════════════════════════════════════════════╝

[Info] Iniciando renderizado con hasta 5 bounces...
[Raytracer] Renderizando 1024x576 con hasta 5 bounces...
  Línea 0 / 576
  ...
[Raytracer] Render completo en 0.95 s

╔════════════════════════════════════════════════════════════╗
║                    RENDERIZADO COMPLETADO                 ║
╠════════════════════════════════════════════════════════════╣
║ Tiempo total: 0.99 segundos
║ Resolución: 1024x576
║ Archivo: Scene_1_Preview_1024x576.png
║ Ruta: C:\Users\aenri\Downloads\Ray_Tracer_AEV\Final project\scene 1\src\output
╚════════════════════════════════════════════════════════════╝
```

**Tiempo esperado:** ~1 segundo

---

### FINAL RENDER (4096x2160 - 8K):

**Paso 1: Abrir App_Scene1.java en VS Code**
```
File → Open File → C:\Users\aenri\Downloads\Ray_Tracer_AEV\Final project\scene 1\src\App_Scene1.java
```

**Paso 2: Buscar esta línea**
```java
boolean isFinalRender = false;  // ← Cambiar esto
```

**Paso 3: Reemplazar por**
```java
boolean isFinalRender = true;   // ← A esto
```

**Paso 4: Guardar (Ctrl+S)**

**Paso 5: En terminal, compilar**
```powershell
cd "c:\Users\aenri\Downloads\Ray_Tracer_AEV\Final project\scene 1\src"
javac -cp . *.java
```

**Paso 6: Ejecutar**
```powershell
java -cp . App_Scene1
```

**Tiempo esperado:** 5-15 minutos

**⚠️ IMPORTANTE:** No cierres la terminal durante el renderizado. Verás actualización cada 50 líneas.

---

## Opción C: One-Liner (Una línea para todo)

### Para PREVIEW:
```powershell
cd "c:\Users\aenri\Downloads\Ray_Tracer_AEV\Final project\scene 1\src"; javac -cp . *.java; java -cp . App_Scene1
```

### Para FINAL RENDER:
```powershell
cd "c:\Users\aenri\Downloads\Ray_Tracer_AEV\Final project\scene 1\src"; (Get-Content App_Scene1.java) -replace 'isFinalRender = false', 'isFinalRender = true' | Set-Content App_Scene1.java; javac -cp . *.java; java -cp . App_Scene1; (Get-Content App_Scene1.java) -replace 'isFinalRender = true', 'isFinalRender = false' | Set-Content App_Scene1.java
```

---

## 📁 Dónde encontrar la imagen generada

**Después de ejecutar, la imagen se guardará en:**

```
C:\Users\aenri\Downloads\Ray_Tracer_AEV\Final project\scene 1\src\output\
```

**Dependiendo de lo que haya ejecutado:**
- PREVIEW: `Scene_1_Preview_1024x576.png`
- FINAL: `Scene_1_FINAL_4096x2160.png`

Puedes abrir esta carpeta directamente desde PowerShell:
```powershell
explorer "c:\Users\aenri\Downloads\Ray_Tracer_AEV\Final project\scene 1\src\output"
```

---

## 🐛 Solución de Problemas

### Error: `'javac' no se reconoce como comando`
**Solución:** Java no está instalado o no está en el PATH.
```powershell
# Verifica si tienes Java:
java -version
javac -version

# Si no funciona, instala Java Development Kit (JDK) desde:
# https://www.oracle.com/java/technologies/downloads/
```

### Error: `FileNotFoundException`
**Solución:** Verifica que exista la carpeta `output/`:
```powershell
cd "c:\Users\aenri\Downloads\Ray_Tracer_AEV\Final project\scene 1\src"
New-Item -ItemType Directory -Name output -Force
```

### El render es MUY lento
**Solución:** Asegúrate de usar PREVIEW (1024x576) para desarrollo:
```java
boolean isFinalRender = false;  // ← Esto está bien para desarrollo
```

---

## 📊 Monitor de Progreso

Mientras renderiza, verás algo como:

```
[Raytracer] Renderizando 4096x2160 con hasta 5 bounces...
  Línea 0 / 2160
  Línea 50 / 2160
  Línea 100 / 2160
  ...
  Línea 2100 / 2160
  Línea 2150 / 2160
[Raytracer] Render completo en 543.45 s
```

El progreso se actualiza cada 50 líneas. ¡Paciencia es virtud! ☕

---

## ✅ Checklist Antes de Ejecutar

- [ ] Tengo Java instalado (`java -version` funciona)
- [ ] Estoy en la carpeta correcta (`cd "...\Final project\scene 1\src"`)
- [ ] Tengo permisos de escritura en la carpeta
- [ ] Si es FINAL RENDER, cambié `isFinalRender = false` a `true`
- [ ] Guardé los cambios si edité App_Scene1.java (Ctrl+S)

---

## 🎯 Próximos Pasos

1. ✅ **Scene 1 completada:** CRYSTAL GEOMETRY
2. 🔄 **Próximo:** Scene 2 (Koenigsegg en showroom)
3. 🔄 **Próximo:** Scene 3 (Naturaleza con refracción)
4. 📝 **Próximo:** Generar reporte LaTeX

**¡Buena suerte con tu proyecto! 🚀**
