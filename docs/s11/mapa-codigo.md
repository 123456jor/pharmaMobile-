# Mapa del código existente

| Responsabilidad | Archivo / símbolo real | Funcionamiento |
| --- | --- | --- |
| Tabla | commonMain/sqldelight/pe/edu/upeu/pharmamobil/db/Producto.sq | producto: id, nombre, precio, stock, pendiente, estado, categoriaId, categoriaNombre |
| Capa local | data/local/ProductoLocal.kt | observar Flow, pendientes, guardarPendiente, reemplazarSincronizados, confirmarSubida. IDs negativos y transacciones. |
| Repositorio | data/repository/ProductoRepositorioOffline.kt | Constructor recibe ProductoLocal, ProductoApi, SesionManager. sincronizar usa Mutex: POST pendientes antes de GET paginado. |
| Red | data/remote/ProductoApi.kt y HttpClientFactory.kt | HttpClient inyectado; crearHttpClient admite MockEngine. ejecutarLlamada convierte IOException a ErrorApiException(SinConexion). |
| Driver Android | androidMain/data/local/CreadorDeDriverAndroid.kt | AndroidSqliteDriver, PharmaDb.Schema, pharma.db |
| Pruebas | commonTest/data/local/BaseEnMemoria.kt y androidHostTest/.../BaseEnMemoria.android.kt | expect/actual con JDBC; esquema nuevo por prueba. |
| Inyección | di/AppModule.kt y PlatformModule.android.kt | Koin registra driver, PharmaDb, ProductoLocal, API, repositorio y ViewModels. No hace falta refactor para testear. |
| UI | presentation/producto/ProductoViewModel.kt y ProductoScreen.kt | Colección Flow en init; sincronizar separado; aviso de conexión y pendientes. Falta fecha de última sincronización. |
| Sesión | data/repository/AuthRepositorioRest.kt | Sesión cifrada vigente permite apertura offline; sesión vencida se elimina antes de consultar la red. Hallazgo para E5. |
| Acceso inicial | App.kt | sesion==null abre LoginScreen. Falta el estado informativo para primera apertura offline E4. |

Las rutas Kotlin indicadas pertenecen a shared/src salvo cuando se especifica androidMain. La UI no importa Ktor. Se conservarán las pruebas anteriores y se añadirán los seis casos específicos del autónomo.
