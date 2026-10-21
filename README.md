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

`CancellationException` se relanza para que salir de la pantalla cancele la corrutina sin convertir la cancelación en un error visible. Los fallos de crear, actualizar o eliminar conservan la lista que ya estaba en pantalla; desde la sesión 11 el éxito se refleja por el Flow de SQLDelight, sin recarga manual en el ViewModel.

El cliente instala `HttpTimeout` con 30 000 ms por defecto. Para probar el escenario 7, cambia temporalmente `MODO_TIMEOUT_PARA_PRUEBAS` a `true` en `shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobil/di/AppModule.kt` (configura 1 ms) y vuelve a `false` al terminar.

### Ejecutar las pruebas

- Android host/commonTest: `.\gradlew.bat :shared:testAndroidHostTest`
- Compilar shared Android: `.\gradlew.bat :shared:compileAndroidMain`
- iOS simulator (requiere macOS y Xcode): `./gradlew :shared:iosSimulatorArm64Test`

La ejecución actual de la sesión 11 está en `evidencias/pruebas/salida-pruebas.txt`: 76 casos aprobados y APK debug ensamblado correctamente. El reporte anterior de 56 casos se conserva como evidencia histórica en `docs/evidencias/`. iOS queda fuera del alcance por indicación del usuario.

### Configuración de PharmaSoft

Android apunta al emulador en `10.0.2.2:8080`; iOS apunta a `localhost:8080`. El contrato se verificó contra el código de `H:\ProyectosUPEU\pharmaSoft`: productos están en `/api/v1/productos`, las categorías en `/api/v1/categorias`, y se requiere un `categoriaId` existente. PharmaSoft implementa DELETE como baja lógica (`estado=false`), responde 204 y mantiene filas en la lista. La UI muestra solo productos activos observando la base local. En la sesión 11 se verificó la integración real con PharmaSoft/Oracle, la reapertura sin red y la sincronización del producto ID 63; las capturas actuales están en `evidencias/capturas/`.

Repositorio destino: `https://github.com/123456jor/pharmaMobile-.git`; rama histórica de sesión 8: `feature/crud-productos-quispe`. La rama actual de sesión 11 es `feature/offline-first-quispe`.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…

## Capacidades nativas (Sesión 9 · expect/actual)

| Capacidad | Mecanismo | commonMain | androidMain | iosMain |
| --- | --- | --- | --- | --- |
| Formato de moneda (S/) | `expect`/`actual` | `platform/Formato.kt` | `platform/Formato.android.kt` (`NumberFormat`, `es_PE`) | `platform/Formato.ios.kt` (`NSNumberFormatter`, `es_PE`) |
| Compartir producto | Interfaz + inyección con Koin | `domain/platform/Compartidor.kt`, `domain/usecase/TextoParaCompartir.kt` | `platform/CompartidorAndroid.kt` (`Intent.ACTION_SEND` + `FLAG_ACTIVITY_NEW_TASK`), `di/PlatformModule.android.kt` | `platform/CompartidorIos.kt` (`UIActivityViewController`), `di/PlatformModule.ios.kt` |

- El precio se formatea en `Producto.aUi()` (capa `presentation`); se eliminó el formateador manual que había en `ProductoUi.kt`.
- El texto a compartir se arma en código común (`Producto.comoTextoParaCompartir()`); `ProductoViewModel.compartir()` solo delega en `Compartidor`.
- El botón «Compartir» está en cada tarjeta del inventario (`ProductoScreen`), porque el proyecto aún no tiene pantalla de detalle.
- `presentation` no importa `android.*` ni `platform.UIKit`.
- En pruebas, `FakeCompartidor` sustituye la implementación nativa (`AppModuleTest`, `ProductoViewModelTest`).
- Evidencias: `docs/evidencias/android/`, `docs/evidencias/ios/` y `docs/evidencias/errores/` (error del punto de control 1).

## Autenticación (Sesión 10 · JWT)

La app envía las credenciales a `POST /api/v1/auth/login`, guarda el JWT en `EncryptedSharedPreferences` respaldadas por Android Keystore y deja que el plugin Bearer de Ktor lo adjunte a las rutas protegidas. Al abrir la app, consulta `GET /api/v1/auth/me` para reconstruir el usuario y sus permisos. Un `401` en una ruta protegida borra la sesión y devuelve al login; un `403` conserva la sesión y muestra el aviso de permisos.

El cliente registra cabeceras solo en compilaciones de depuración y sanea `Authorization`; no registra cuerpos de login. El tráfico HTTP sin cifrar está permitido únicamente para `10.0.2.2`, que es el host de Android Emulator para el backend local. `INTERNET` está habilitado y las copias de seguridad Android están desactivadas para no exportar las preferencias cifradas.

| Usuario de prueba | Permisos relevantes |
| --- | --- |
| `admin` | Administración completa, catálogo, clientes, ventas y reportes |
| `vendedor` | Catálogo de lectura, clientes y ventas |
| `almacen` | Catálogo de lectura y escritura |

Para ejecutar el flujo Android, inicia PharmaSoft con el perfil `dev` en el puerto 8080, verifica `http://localhost:8080/api/health` y abre PharmaMobil en un emulador. La app usa `http://10.0.2.2:8080/api/v1/`. Las credenciales de prueba se mantienen en los datos de desarrollo del backend y no se guardan en el código de la app.

Desde la sesión 11, si `/auth/me` falla por falta de red se restaura la sesión previamente autenticada y cifrada únicamente mientras siga vigente; un `401` elimina el token y los datos de sesión. El cierre de sesión elimina el token persistido y la caché del proveedor Bearer.

## Persistencia offline-first

Sesión 11, Android. SQLDelight 2.1.0 es la única fuente de la lista de productos de la interfaz. Docker/Oracle siguen siendo la base del servidor PharmaSoft; `pharma.db` es SQLite dentro del teléfono y permite consultar y registrar sin conexión.

### Esquema y arquitectura

- Tabla `producto`: `id`, `nombre`, `precio`, `stock`, `pendiente` (default 0), `estado`, `categoriaId`, `categoriaNombre`. Los tres últimos conservan los datos del CRUD existente.
- Consultas: `listarTodos`, `listarPendientes`, `insertar`, `borrarSincronizados` (solo pendiente=0), `siguienteIdLocal` y `borrarPorId`.
- `CreadorDeDriverAndroid` abre `pharma.db`; Koin registra el driver, `PharmaDb`, `ProductoLocal` y `ProductoRepositorioOffline`.
- `ProductoLocal` observa SQLDelight con `asFlow().mapToList(Dispatchers.Default)` y convierte filas al dominio. No depende de Ktor ni DTO. IDs y reemplazo se escriben en transacciones.
- Un alta offline usa `minOf(menor, 0L) - 1L`: los IDs negativos no chocan con los positivos del servidor y permiten distinguir pendientes.
- `ProductoViewModel` recoge `repositorio.observar()` desde init. Registrar, editar y eliminar no vuelven a pedir la lista en la presentación.

### Sincronización

1. Guardar primero el alta local con pendiente=1.
2. `sincronizar()` usa un Mutex para serializar subidas/descargas. Envía primero los pendientes.
3. Solo tras confirmar cada POST, una transacción sustituye el ID negativo por el producto recibido con ID positivo y pendiente=0.
4. Después descarga todas las páginas y reemplaza exclusivamente las filas sincronizadas, conservando los pendientes. El Flow actualiza la pantalla.

El botón **Sincronizar** inicia el proceso. Sin red se conserva el listado y aparece **Sin conexión: mostrando datos guardados**. Cada alta pendiente muestra su marca e ID. Editar y eliminar requieren conexión y un producto sincronizado; se mantienen las acciones autorizadas del CRUD, con baja lógica. Los errores 400/401/403/5xx no se silencian como si fueran desconexión. Timeout/conexión durante un alta conserva el pendiente.

Para reabrir sin red se guardan usuario, permisos y vencimiento de la sesión ya autenticada en las mismas preferencias cifradas que el JWT. No se guarda la contraseña en la app. Una sesión vencida o rechazada con 401 no se restaura. El primer login y la renovación al vencer requieren conexión.

### Pruebas

```powershell
.\gradlew.bat :shared:testAndroidHostTest :androidApp:assembleDebug --console=plain
```

Resultado ejecutado el 21/10/2026: **76 pruebas, 0 fallos, 0 errores, 0 omitidas**, y APK ensamblado. Ocho pruebas nuevas del repositorio cubren red, ausencia de red, IDs negativos distintos, orden POST antes de GET, preservación de pendientes, duplicados, errores 503 y paginación. Tres pruebas de autenticación cubren apertura offline con sesión vigente, vencimiento y rechazo 401. Cada prueba de base de datos crea su propio SQLite en memoria.

- [Salida completa](evidencias/pruebas/salida-pruebas.txt) y [resumen](evidencias/pruebas/resumen.json).
- [Reporte HTML de todas las pruebas](evidencias/pruebas/reporte/index.html).
- [Bitácora](evidencias/bitacora.md), [capturas originales](evidencias/capturas/) y [lista de verificación](evidencias/checklist.md).

### Reproducir el ciclo

1. Iniciar Oracle/Docker y PharmaSoft perfil dev en 8080. Iniciar sesión Android y entrar en Productos.
2. Dejar sincronizar y cerrar completamente la app.
3. Activar modo avión y desactivar también Wi-Fi si Android lo mantiene activo. Reabrir y comprobar listado y aviso.
4. Registrar un producto con categoría existente: aparece pendiente y con ID negativo.
5. Recuperar la red y pulsar Sincronizar: desaparece la marca de pendiente y queda un único producto con ID positivo.
6. En Postman importar [la colección S11](postman/S11-Offline-first.postman_collection.json). Ejecutar 01 Login, 02 Listar y 03 Buscar. La segunda petición comprueba que existe una sola coincidencia y guarda su ID para la tercera.
7. Con red activa, detener PharmaSoft, cerrar y reabrir la app: debe seguir mostrando lo guardado. Volver a iniciar PharmaSoft al acabar.

### Adaptaciones y alcance

- El usuario autorizó la rama `feature/offline-first-quispe` desde `feature/auth-jwt-quispe`, pues `develop` no existe.
- iOS está expresamente excluido por el usuario. No se implementó ni verificó en esta práctica.
- D-1: JDBC es de JVM; la prueba está en commonTest, con fábrica expect y actual JDBC en androidHostTest. Una base nueva por prueba.
- D-2: el algoritmo literal produjo dos filas (prueba fallida conservada en `04-duplicado-antes.*`). Se añadió `borrarPorId` y confirmación transaccional tras POST. Esto evita volver a enviar pendientes ya confirmados.
- D-3: la clase generada se importa con alias `ProductoFila`, distinguiéndola del dominio.
- D-4: editar/eliminar se conservan online, según decisión expresa.
- El proyecto usa `ErrorApiException(ErrorApi.SinConexion)`; se captura el envoltorio real, pues `ErrorApi` no es Throwable.
- **Postman sustituye Swagger por instrucción expresa del usuario.** Se consulta la misma API protegida y se captura nombre e ID positivo reales.
- Esta práctica no incorpora sincronización periódica, cola offline para editar/eliminar ni resolución de conflictos. Un POST cuyo resultado se pierda después de guardarse en el servidor necesitaría idempotencia del backend para garantizar exactamente una creación ante ese corte.

### Entregable de la sesión 11

- [PDF con capturas reales y lista de cotejo](output/pdf/Entregable_Sesion11_OfflineFirst_Quispe.pdf).
- Capturas originales y verificaciones: [evidencias/](evidencias/).
- El PDF tiene 15 páginas; las vistas de escritorio usan páginas apaisadas más grandes para conservar la legibilidad, sin alterar las capturas.
