# 🏗️ ESTRUCTURA DEL PROYECTO - FINAL PROJECT

```
Ray_Tracer_AEV/
│
├── v0.1 → v0.8/        ← Versiones anteriores (desarrollo)
│
└── Final project/       ← ENTREGAS FINALES (3 ESCENAS)
    │
    ├── scene 1/         ✅ COMPLETADA
    │   ├── src/
    │   │   ├── App_Scene1.java       ← PUNTO DE ENTRADA PRINCIPAL
    │   │   ├── Raytracer.java        ← Motor de ray tracing
    │   │   ├── Material.java         ← Sistema de materiales
    │   │   ├── Camera.java           ← Sistema de cámara
    │   │   ├── Scene.java            ← Gestión de escena
    │   │   ├── Sphere.java           ← Geometría: esfera
    │   │   ├── Triangle.java         ← Geometría: triángulo
    │   │   ├── Ray.java              ← Rayo 3D
    │   │   ├── Vector3D.java         ← Matemática 3D
    │   │   ├── Intersection.java     ← Datos de intersección
    │   │   ├── Object3D.java         ← Clase base objetos
    │   │   ├── Light.java            ← Sistema de iluminación
    │   │   ├── DirectionalLight.java ← Luz direccional (sol)
    │   │   ├── PointLight.java       ← Luz puntual
    │   │   ├── PhongShader.java      ← Iluminación Blinn-Phong
    │   │   ├── ObjModel.java         ← Carga de modelos OBJ
    │   │   └── ObjReader.java        ← Parser de archivos OBJ
    │   │
    │   ├── output/                   ← Imágenes generadas
    │   │   ├── Scene_1_Preview_1024x576.png
    │   │   └── Scene_1_FINAL_4096x2160.png (después de render final)
    │   │
    │   ├── README.md                 ← Documentación de Scene 1
    │   ├── COMO_EJECUTAR.md          ← Instrucciones de terminal
    │   ├── BUILD.bat                 ← Script para Windows CMD
    │   └── build.ps1                 ← Script para PowerShell
    │
    ├── scene 2/         🔄 EN DESARROLLO
    │   └── [Estructura igual a scene 1]
    │
    └── scene 3/         🔄 EN DESARROLLO
        └── [Estructura igual a scene 1]
```

---

## 📌 SCENE 1 - INFORMACIÓN CLAVE

### 🎨 COMPOSICIÓN
- **Nombre:** CRYSTAL GEOMETRY
- **Tema:** La transformación de la luz a través de diferentes medios
- **Resolución:** 
  - Preview: 1024x576 (desarrollo, ~1 segundo)
  - Final: 4096x2160 (entrega, ~10 minutos)
- **Formato:** PNG

### 🏆 ELEMENTOS VISUALES
| Elemento | Tipo | Material | Cantidad |
|----------|------|----------|----------|
| Pirámide | Geometría 3D | Vidrio (IOR=1.5) | 1 (principal) |
| Esferas | Geometría 3D | Metales diversos | 5 (satélites) |
| Piso | Triángulos | Espejo (reflectividad 95%) | 1 (2 triángulos) |
| Bloque | Triángulos | Espejo oscuro (70%) | 1 (2 triángulos) |

### 💡 ILUMINACIÓN
| Luz | Tipo | Color | Posición | Intensidad |
|-----|------|-------|----------|-----------|
| Clave | Direccional | Azul frío | Arriba-izquierda | 1.4 |
| Relleno | Puntual | Dorado cálido | Derecha | 25.0 |
| Contorno | Puntual | Azul profundo | Atrás | 18.0 |

### ✅ CRITERIOS CUMPLIDOS
- [x] Cuenta una historia clara (exploración de luz)
- [x] Visualmente estética (composición artística)
- [x] Elementos complejos (pirámide + esferas + espejo)
- [x] NO usa teteras ni esferas simples
- [x] Refracción funcional (vidrio)
- [x] Reflexiones funcionales (metales)
- [x] Múltiples bounces (5)
- [x] Resolución 4096x2160 disponible
- [x] Formato PNG

---

## 🚀 PRÓXIMAS ESCENAS (PLANIFICADAS)

### Scene 2: "LUXURY SHOWROOM"
- Koenigsegg.obj como objeto principal
- Ambiente de showroom (suelo reflectivo, luces de galerìa)
- Composición: Presentación de vehículo de lujo
- Materiales: Pintura metálica del auto, vidrio refractivo, acero

### Scene 3: "NATURE'S PRISM"
- Composición natural: agua, cristal, gemas
- Tema: Belleza natural refractada
- Materiales: Agua (refractiva), cristal, arena (difusa)
- Narrativa: Luz natural en medio natural

---

## 📊 ESTADÍSTICAS

### Scene 1
- **Líneas de código:** ~500+ (App_Scene1.java + librerías)
- **Objetos en escena:** 9 (1 pirámide + 5 esferas + 2 bloques espejo + piso)
- **Triángulos:** 16+ (2 para piso, 6 para pirámide, 2 para bloque, más en esferas)
- **Fuentes de luz:** 3
- **Bounces:** 5 máximo
- **Tiempo preview:** ~1 segundo
- **Tiempo final (4K):** ~10-15 minutos

---

## 🔧 COMPILACIÓN RÁPIDA

### Desde terminal (PowerShell):
```powershell
cd "c:\Users\aenri\Downloads\Ray_Tracer_AEV\Final project\scene 1\src"
javac -cp . *.java && java -cp . App_Scene1
```

### Desde terminal (CMD):
```batch
cd "c:\Users\aenri\Downloads\Ray_Tracer_AEV\Final project\scene 1\src"
javac -cp . *.java && java -cp . App_Scene1
```

---

## 📝 LISTA DE CONTROL - ENTREGA

- [x] Scene 1 completada y funcionando
- [x] README.md con documentación
- [x] COMO_EJECUTAR.md con instrucciones
- [x] Scripts de compilación (BUILD.bat, build.ps1)
- [x] Imagen preview generada
- [ ] Imagen final (4096x2160) generada
- [ ] Scene 2 completada
- [ ] Scene 3 completada
- [ ] Reporte LaTeX generado
- [ ] Presentación preparada

**Progreso:** 6/10 completado (60%)

---

**Última actualización:** 21 de Mayo, 2026
**Desarrollador:** Estudiante de Multimedia & Computer Graphics
**Institución:** Universidad Panamericana
