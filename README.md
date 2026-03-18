# AppSpace Movil 🧹

Aplicación Android de limpieza y organización de archivos con agente IA integrado.

## Características ✨

- **🔍 Escaneo completo**: Analiza todos los archivos del dispositivo
- **👯 Detección de duplicados**: Encuentra archivos repetidos usando hash MD5
- **🗑️ Limpieza de caché**: Elimina archivos temporales de forma segura
- **📊 Informes detallados**: Recibe un informe después de cada tarea
- **🤖 Agente IA**: Asistente inteligente que recomienda acciones de limpieza
- **✅ Aprobación del usuario**: Todas las acciones requieren tu confirmación
- **📁 Organización por categorías**: Fotos, Videos, Documentos, Descargas, etc.

## Tecnologías 🛠️

- **Lenguaje**: Kotlin
- **UI**: Jetpack Compose + Material Design 3
- **Base de datos**: Room
- **Arquitectura**: MVVM (Model-View-ViewModel)
- **Corrutinas**: Para operaciones asíncronas
- **Navigation Compose**: Navegación entre pantallas

## Estructura del Proyecto 📂

```
app/
├── src/main/
│   ├── java/com/appspace/movil/
│   │   ├── data/
│   │   │   ├── local/          # Room Database, DAOs, Entities
│   │   │   └── repository/     # Repositorios (FileRepository, DuplicateDetector, CacheCleaner)
│   │   ├── domain/
│   │   │   ├── agent/          # Agente IA (CleanAgent)
│   │   │   └── report/         # Generador de informes (ReportGenerator)
│   │   ├── model/              # Modelos de datos
│   │   ├── ui/
│   │   │   ├── screens/        # Pantallas Compose
│   │   │   ├── theme/          # Tema y tipografía
│   │   │   ├── navigation/     # Navegación
│   │   │   └── viewmodel/      # ViewModels
│   │   └── MainActivity.kt
│   ├── res/
│   │   ├── values/             # Strings, colors, themes
│   │   ├── drawable/           # Iconos
│   │   └── xml/                # File paths provider
│   └── AndroidManifest.xml
```

## Funcionalidades 🚀

### 1. Escaneo de Archivos
- Escanea fotos, videos, audio, documentos, descargas y caché
- Muestra progreso en tiempo real
- Calcula estadísticas por categoría

### 2. Detección de Duplicados
- Usa hash MD5 para identificar archivos idénticos
- Agrupa duplicados y muestra el espacio desperdiciado
- Recomienda cuáles eliminar (mantiene el original)

### 3. Limpieza de Caché
- Limpia caché de aplicación
- Elimina archivos temporales (.tmp, .temp)
- Limpia caché de thumbnails

### 4. Agente IA
- Analiza el dispositivo y genera recomendaciones
- Clasifica acciones por nivel de riesgo (Low, Medium, High)
- Responde preguntas del usuario en lenguaje natural
- Requiere aprobación antes de ejecutar cualquier acción

### 5. Informes
- Genera informe después de cada limpieza
- Muestra espacio liberado y archivos eliminados
- Historial de informes anteriores

## Permisos Requeridos 📋

```xml
<!-- Almacenamiento -->
READ_EXTERNAL_STORAGE (Android < 13)
READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, READ_MEDIA_AUDIO (Android 13+)
MANAGE_EXTERNAL_STORAGE

<!-- Sistema -->
REQUEST_DELETE_PACKAGES
PACKAGE_USAGE_STATS

<!-- Otros -->
INTERNET, POST_NOTIFICATIONS, FOREGROUND_SERVICE
```

## Instalación y Build ⚙️

### Requisitos
- Android Studio Hedgehog o superior
- JDK 17
- Android SDK 34

### Pasos
1. Clonar el repositorio
2. Abrir en Android Studio
3. Sincronizar Gradle
4. Ejecutar en emulador o dispositivo físico

```bash
# Build debug
./gradlew assembleDebug

# Instalar en dispositivo
./gradlew installDebug
```

## Capturas de Pantalla 📱

### Dashboard
- Gráfico circular de uso de almacenamiento
- Botón de escaneo
- Recomendaciones del agente IA
- Grid de categorías
- Último informe

### Chat con Agente IA
- Interfaz conversacional
- Sugerencias de acciones
- Informes integrados

## Próximas Características 🔮

- [ ] Integración con APIs de IA (ChatGPT, Gemini)
- [ ] Compresión de archivos
- [ ] Subida a la nube
- [ ] Programación de limpiezas automáticas
- [ ] Widget de escritorio
- [ ] Modo de limpieza profunda (root)

## Licencia 📄

Este proyecto está bajo la licencia MIT.

---

**Desarrollado con ❤️ usando Kotlin y Jetpack Compose**
