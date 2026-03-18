# 📱 Guía para obtener el APK

## Pasos para compilar el APK en GitHub

### 1. Subir el código a GitHub

Abre una terminal en la carpeta del proyecto y ejecuta:

```bash
# Inicializar repositorio git
git init

# Agregar todos los archivos
git add .

# Crear el primer commit
git commit -m "Initial commit - AppSpace Movil"

# Conectar con tu repositorio de GitHub (reemplaza TU_USUARIO)
git remote add origin https://github.com/TU_USUARIO/AppSpaceMovil.git

# Subir el código
git push -u origin main
```

### 2. Activar GitHub Actions

1. Ve a tu repositorio en GitHub
2. Haz clic en la pestaña **Actions**
3. Si ves un mensaje de bienvenida, haz clic en **"I understand my workflows, go ahead and enable them"**

### 3. Esperar a que se compile el APK

GitHub Actions automáticamente:
- Detectará el código
- Ejecutará el build
- Generará el APK

Esto toma aproximadamente **5-10 minutos** la primera vez.

### 4. Descargar el APK

1. Ve a la pestaña **Actions** en tu repositorio
2. Haz clic en el workflow **"Build Android APK"** (el más reciente)
3. En la sección **"Artifacts"**, haz clic en **app-debug**
4. Se descargará un archivo ZIP
5. Extrae el ZIP y obtendrás `app-debug.apk`

### 5. Instalar en tu celular

1. Transfiere el `app-debug.apk` a tu celular
2. En tu celular, activa **"Orígenes desconocidos"** en Ajustes > Seguridad
3. Abre el APK e instálalo
4. ¡Listo! La app estará en tu launcher

---

## Solución de problemas

### El build falla
- Revisa los logs en GitHub Actions (pestaña Actions)
- Verifica que tengas conexión a internet

### No aparece el Artifact
- Espera a que el workflow termine completamente (debe decir en verde "✓")
- Los artifacts están disponibles por 90 días

### No puedo instalar el APK
- Asegúrate de permitir instalación de orígenes desconocidos
- El APK es de tipo "debug", puede que necesites habilitar "Depuración USB" en algunos dispositivos

---

## Actualizar la app

Cuando hagas cambios en el código:

```bash
git add .
git commit -m "Descripción de los cambios"
git push
```

GitHub automáticamente generará un nuevo APK!
