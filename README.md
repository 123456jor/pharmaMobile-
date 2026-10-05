This is a Kotlin Multiplatform project targeting Android, iOS.

* [/iosApp](./iosApp/iosApp) contains an iOS application. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* [/shared](./shared/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./shared/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./shared/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./shared/src/jvmMain/kotlin)
    folder is the appropriate location.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Android app: `./gradlew :androidApp:assembleDebug`
- iOS app: open the [/iosApp](./iosApp) directory in Xcode and run it from there.

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Android tests: `./gradlew :shared:testAndroidHostTest`
- iOS tests: `./gradlew :shared:iosSimulatorArm64Test`

## Manejo de errores

Las llamadas de productos se realizan mediante Ktor desde `commonMain`. El único lugar que convierte fallos HTTP y de transporte al dominio es `data/remote/EjecutarLlamada.kt`; las clases de `presentation` no dependen de Ktor.

| Respuesta o fallo | ErrorApi | Mensaje/resultado en la pantalla |
| --- | --- | --- |
| 400 | `Validacion(porCampo)` | Los mensajes recibidos se muestran debajo de nombre, precio, stock o categoría. El precio cero o negativo se envía para que lo valide PharmaSoft. |
| 404 | `NoEncontrado` | No se encontró el producto solicitado. |
| 409 | `Conflicto(mensaje)` | Se muestra el mensaje enviado por el servidor. |
| 5xx | `Servidor` | El servidor no pudo completar la operación. |
| Sin respuesta de red | `SinConexion` | No hay conexión con PharmaSoft. |
| Timeout de petición/conexión | `TiempoAgotado` | La solicitud tardó demasiado. |

`CancellationException` se relanza para que salir de la pantalla cancele la corrutina sin convertir la cancelación en un error visible. Los fallos de crear, actualizar o eliminar conservan la lista que ya estaba en pantalla; tras el éxito la lista se vuelve a consultar sin pasar por la fase `Cargando`.

El cliente instala `HttpTimeout` con 30 000 ms por defecto. Para probar el escenario 7, cambia temporalmente `MODO_TIMEOUT_PARA_PRUEBAS` a `true` en `shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobil/di/AppModule.kt` (configura 1 ms) y vuelve a `false` al terminar.

### Ejecutar las pruebas

- Android host/commonTest: `.\gradlew.bat :shared:testAndroidHostTest`
- Compilar shared Android: `.\gradlew.bat :shared:compileAndroidMain`
- iOS simulator (requiere macOS y Xcode): `./gradlew :shared:iosSimulatorArm64Test`

Los resultados de Android ejecutados por Codex están en `docs/evidencias/salida-pruebas.txt` (56 casos, 0 fallos). Las rutas locales del equipo se redactaron en las salidas guardadas. `:androidApp:compileDebugKotlin` compiló correctamente. El APK debug no pudo ensamblarse porque la política del entorno bloqueó `debug.keystore.lock`; se debe volver a ejecutar `:androidApp:assembleDebug` en Android Studio. La suite de iOS y las operaciones reales contra PharmaSoft siguen pendientes de ejecutarse desde los entornos correspondientes.

### Configuración de PharmaSoft

Android apunta al emulador en `10.0.2.2:8080`; iOS apunta a `localhost:8080`. El contrato se verificó contra el código de `H:\ProyectosUPEU\pharmaSoft`: productos están en `/api/v1/productos`, las categorías en `/api/v1/categorias`, y se requiere un `categoriaId` existente. PharmaSoft implementa DELETE como baja lógica (`estado=false`), responde 204 y mantiene filas en la lista. La UI muestra solo productos activos tras recargar. La base de datos/servidor no estuvo disponible para una ejecución integrada; completa capturas y HTTP reales en `docs/evidencias/` y `docs/bitacora.md` después de probar.

Repositorio destino: `https://github.com/123456jor/pharmaMobile-.git`; rama preparada: `feature/crud-productos-quispe`.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
