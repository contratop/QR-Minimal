# QR Minimal 📱✨

**QR Minimal** es un lector de códigos QR ultrarrápido, moderno y ligero para Android. Está diseñado para ofrecer la mejor experiencia de usuario sin distracciones, utilizando el último lenguaje de diseño de Google, **Material Design 3**, y la máxima velocidad de lectura que aporta **Google ML Kit**.

## 🚀 Características Principales

- 🎨 **Diseño Moderno:** Interfaz creada 100% con Jetpack Compose y Material 3. Fluida, reactiva y adaptable al modo oscuro de tu sistema.
- 📷 **Escaneo Instantáneo:** Motor impulsado por *CameraX* y *ML Kit Barcode Scanning*. Funciona totalmente **offline** y en tiempo real.
- 🧠 **Acciones Inteligentes:** No solo lee texto, entiende lo que escaneas:
  - **Wi-Fi**: Copia la contraseña y abre tus ajustes de red con un solo toque.
  - **VCard (Contactos)**: Extrae el nombre, teléfono, email y empresa, y abre directamente tu agenda para guardar el contacto.
  - **Enlaces (URLs)**: Abre tu navegador preferido.
  - **Teléfono, SMS y Correo**: Ejecuta las aplicaciones nativas pertinentes ya rellenadas con los datos escaneados.
- 🔦 **Linterna Integrada**: Control de flash integrado directamente en la pantalla de escaneo para condiciones de poca luz.
- 🥚 **Easter Eggs**: Escanea algo *peligroso* y averigua qué pasa. (Nunca te abandonará).

## 🛠️ Tecnologías Utilizadas

- **Lenguaje:** [Kotlin](https://kotlinlang.org/)
- **UI Toolkit:** [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Diseño:** [Material Design 3](https://m3.material.io/)
- **Cámara:** [AndroidX CameraX](https://developer.android.com/training/camerax)
- **Procesamiento de Visión:** [Google ML Kit](https://developers.google.com/ml-kit)
- **CI/CD:** GitHub Actions automatizado para la creación de *Releases* con APKs instalables.

## 📦 Descarga e Instalación

Puedes descargar el último archivo `.apk` instalable directamente desde la pestaña de [Releases](../../releases/latest) de este repositorio. Las compilaciones se generan automáticamente cada vez que se actualiza el código.

## 🏗️ Cómo compilarlo localmente

Si quieres hacer un fork, probarlo localmente o modificarlo, simplemente sigue estos pasos:

1. Clona el repositorio:
   ```bash
   git clone https://github.com/tu-usuario/qrminimal.git
   cd qrminimal
   ```
2. Compila la aplicación en modo Debug:
   ```bash
   ./gradlew assembleDebug
   ```
3. Instálalo en tu dispositivo conectado:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

## 📄 Licencia

Este proyecto es de código abierto. ¡Siéntete libre de utilizarlo, aprender de él o mejorarlo!
