# SCENE 1 - KOENIGSEGG SHOWROOM
## Ray Tracer Final Project

### ðŸ“– NARRATIVA
**"Koenigsegg Showroom"** - Una escena que pone en primer plano un superdeportivo Koenigsegg OBJ dentro de un showroom sobrio y nocturno. La composiciÃ³n busca transmitir la calma previas a la carrera, resaltando la curva del auto y el brillo de su carrocerÃ­a.

**Historia que cuenta:** Un momento de expectaciÃ³n y lujo antes de la aceleraciÃ³n, donde el auto es la pieza central y la iluminaciÃ³n resalta su presencia y su potencial.

---

## 🛠️ COMPILACIÓN Y EJECUCIÓN

### Opción 1: PREVIEW (Rápido - 1024x576)
Perfecto para ver resultados rápidamente durante desarrollo.

```bash
# Navegar a la carpeta de Scene 1
cd "c:\Users\aenri\Downloads\Ray_Tracer_AEV\Final project\scene 1\src"

# Compilar todos los archivos Java
javac -cp . *.java

# Ejecutar la escena de PREVIEW
java -cp . App_Scene1

# La imagen se guardará en: output/Scene_1_Koenigsegg_Preview_1024x576.png
```

**Tiempo esperado:** 5-15 segundos

---

### Opción 2: FINAL RENDER (4096x2160 - 8K)
Requiere cambiar una línea en App_Scene1.java

```bash
# 1. Editar App_Scene1.java
#    Cambiar la línea:
#    boolean isFinalRender = false;
#    Por:
#    boolean isFinalRender = true;

# 2. Compilar
javac -cp . *.java

# 3. Ejecutar (esto tomará 5-15 MINUTOS)
java -cp . App_Scene1

# La imagen final se guardará en: output/Scene_1_Koenigsegg_FINAL_4096x2160.png
```

**Tiempo esperado:** 5-15 minutos (según CPU)

---

## 📝 EDITAR LA RESOLUCIÓN EN VS CODE

**Método 1: VS Code GUI**
1. Abre `src/App_Scene1.java`
2. Busca la línea: `boolean isFinalRender = false;`
3. Cambia `false` a `true`
4. Guarda el archivo (Ctrl+S)
5. Ejecuta: `javac -cp . *.java && java -cp . App_Scene1`

**Método 2: Terminal PowerShell (Reemplazar directamente)**
```powershell
cd "c:\Users\aenri\Downloads\Ray_Tracer_AEV\Final project\scene 1\src"

# Reemplazar false por true
(Get-Content App_Scene1.java) -replace 'isFinalRender = false', 'isFinalRender = true' | Set-Content App_Scene1.java

# Compilar y ejecutar
javac -cp . *.java
java -cp . App_Scene1
```

---

## 📊 CONTENIDO DE LA ESCENA

### Materiales Utilizados:
- ✅ **Vidrio Refractivo** (Pirámide) - IOR=1.5, transparencia 92%
- ✅ **Espejos Metálicos** - 5 esferas con reflectividad 80-90%
- ✅ **Piso Espejo** - Reflexión perfecta 95%
- ✅ **Bloque Reflectivo** - Espejo oscuro para profundidad

### Iluminación:
- ✅ **Luz Clave:** Azul fría (simula luna) - desde arriba-izquierda
- ✅ **Luz Relleno:** Dorada cálida - desde la derecha
- ✅ **Luz Contorno:** Azul profundo - desde atrás (rim light)

### Características de Ray Tracing:
- ✅ Refracción con Ley de Snell (pirámide de vidrio)
- ✅ Reflexiones especulares (esferas metálicas)
- ✅ Múltiples bounces (5 por defecto)
- ✅ Sombras duras correctas
- ✅ Iluminación Blinn-Phong con especular

---

## 🎯 CRITERIOS DE EVALUACIÓN CUBIERTOS

| Criterio | Estado | Detalles |
|----------|--------|----------|
| **Cuenta una historia** | ✅ | Exploración de luz a través de materiales |
| **Visualmente estética** | ✅ | Composición artística con colores complementarios |
| **Elementos complejos** | ✅ | Pirámide + 5 esferas + espejo + bloque |
| **NO teteras/esferas básicas** | ✅ | Pirámide es el objeto principal |
| **Resolución 4096x2160** | ✅ | Disponible en modo final render |
| **Formato PNG** | ✅ | Guardado automáticamente en PNG |

---

## 💾 ARCHIVOS GENERADOS

Después de ejecutar, encontrarás:

```
Final project/
└── scene 1/
    ├── src/
    │   ├── App_Scene1.java          ← Punto de entrada
    │   ├── Raytracer.java
    │   ├── Material.java
    │   ├── Camera.java
    │   ├── Scene.java
    │   └── ... (otros archivos)
    │
    └── output/
        └── Scene_1_Koenigsegg_Preview_1024x576.png  (después de ejecución)
        └── Scene_1_Koenigsegg_FINAL_4096x2160.png   (si isFinalRender=true)
```

---

## 🐛 SOLUCIÓN DE PROBLEMAS

**Problema:** `'javac' no se reconoce como comando`
```
Solución: Instalar Java Development Kit (JDK)
o agregar la ruta de Java a la variable PATH
```

**Problema:** `FileNotFoundException` al guardar imagen
```
Solución: La carpeta "output/" se crea automáticamente
Asegúrate de tener permisos de escritura en la carpeta scene 1/
```

**Problema:** El render es muy lento
```
Solución: Usar preview (1024x576) durante desarrollo
Usar final (4096x2160) solo para entrega
```

---

## ⏱️ TIEMPOS DE RENDERIZADO ESTIMADOS

| Resolución | CPU | Tiempo |
|------------|-----|--------|
| 1024x576   | Modern | 5-15s |
| 4096x2160  | Modern | 5-15 min |

*Nota: Los tiempos dependen de tu procesador. CPUs con más cores renderizarán más rápido.*

---

**Creado para:** Multimedia & Computer Graphics - Universidad Panamericana, 2026
**Profesor:** Ing. Bernardo Moya de la Mora, M.C. Guillermo González Mena
