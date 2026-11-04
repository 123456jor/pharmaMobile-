# Auditoría inicial - sesión 13

## Identidad y alcance

- Fecha de auditoría: 4 de noviembre de 2026, America/Lima.
- Proyecto existente: `H:/ProyectosUPEU/pharmaMobile-`.
- Autor de nuevos commits: Jorge Quispe <joorge0148@gmail.com>, configuración local. No se modificaron autores anteriores ni la configuración global.
- Remoto verificado: `https://github.com/123456jor/pharmaMobile-.git`.
- Alcance acordado: Android y Postman. iOS continúa excluido por indicación del estudiante; no se declara compilado ni ejecutado.
- Referencias: guía práctica de 14 páginas y diapositivas de 19 páginas de la sesión 13, leídas completas. El prompt del estudiante complementa los ejemplos incompletos de la guía.

## Ramas: análisis realizado antes de crear las nuevas

El árbol estaba limpio. Se ejecutó fetch de todos los remotos y se inspeccionaron referencias e historial. La rama predeterminada remota era feature/crud-productos-quispe, una versión anterior al trabajo de autenticación y persistencia. Todas las ramas existentes forman una línea de descendencia; no hay cambios exclusivos en las ramas anteriores respecto a feature/s11-autonoma-quispe.

| Rama comparada | Commits adicionales de sesión 11 | Commits exclusivos de la rama anterior |
|---|---:|---:|
| feature/crud-productos-quispe | 35 | 0 |
| feature/expect-actual-quispe | 32 | 0 |
| feature/auth-jwt-quispe | 17 | 0 |
| feature/offline-first-quispe | 8 | 0 |

Base elegida: `feature/s11-autonoma-quispe`, commit `8184dc7`. No existían main ni develop. Tras presentar el análisis al estudiante se crearon y publicaron develop desde ese commit y feature/integrador-13 desde develop. No se reescribió historial y no se modificó una rama main.

## Línea base ejecutada hoy

- JDK Microsoft 21.0.12.1, Gradle 9.1.0, Kotlin 2.4.10, AGP 9.0.1.
- SQLDelight 2.1.0, Ktor 3.6.0, Koin 4.2.2, coroutines 1.10.2.
- Compose Multiplatform 1.11.1; Material3 1.11.0-alpha07.
- Android compile/target SDK 36, mínimo 24. AVD disponible: Pixel_8_Pro, imagen API 37.1 con Google Play.
- Se listaron las tareas reales de shared y se ejecutó `:shared:testAndroidHostTest :androidApp:assembleDebug --rerun-tasks --console=plain`.
- Resultado: BUILD SUCCESSFUL en 56 s, 73 tareas ejecutadas. XML generado hoy: **94 pruebas, cero fallos, cero errores y cero omitidas**. No se usó el resultado antiguo de la sesión 11.
- APK: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`, 24 164 519 bytes, generado hoy a las 14:07 aproximadamente.
- Advertencias: los targets de simulador iOS no se ejecutan en Windows; advertencias de APIs deprecadas ya presentes. No se alteraron versiones para eliminarlas.
- Salida íntegra: `evidencias/sesion13/pruebas/linea-base.txt`; tareas: `tareas-gradle.txt` en la misma carpeta.

## Arquitectura encontrada

| Requisito previo | Implementación real | Verificación inicial |
|---|---|---|
| REST y DTO separados | crearHttpClient, ProductoApi, ProductoRequestDto/ProductoResponseDto/PaginaResponseDto; Producto en dominio | Inspección y pruebas actuales; recorrido real pendiente del paso 1 |
| CRUD y errores tipados | ProductoRepositorioOffline; ErrorApi sellado; 401/403 se modelan con ErrorSesion sellado | Compilación y pruebas existentes aprobadas |
| JWT seguro y cabecera central | AlmacenSeguroAndroid con EncryptedSharedPreferences; plugin Bearer en crearHttpClient; Authorization sanitizado en logs | Código y regresión de autenticación aprobados |
| Fuente local observable | ProductoLocal.observar con SQLDelight asFlow; ProductoViewModel recoge Flow | Código y pruebas offline aprobados; modo avión real se repetirá hoy |
| Subir antes de bajar | sincronizar con Mutex llama subirPendientes antes de GET de todas las páginas | Seis pruebas previas y código actual |
| Koin por capas/plataforma | dataModule, domainModule, presentationModule y platformModule Android | Tests de resolución de dependencias aprobados |
| Última sincronización | Sincronizacion.sq, una fila id=1; ProductoLocal.confirmarSincronizacion confirma lista y fecha en transacción | Prueba de éxito/fallo y formateador aprobados |

El módulo de clientes conserva una implementación en memoria de sesiones anteriores. No se usa como sustituto del catálogo real de productos; cualquier ampliación de ese módulo queda fuera del refinamiento solicitado salvo decisión expresa.

## Diferencias frente a los ejemplos de la guía

1. La tabla real es `producto`, no labor. Conservaremos IDs, categorías, precio, stock, estado y pendiente.
2. Existe `1.sqm`, que agrega Sincronizacion. La siguiente migración será `2.sqm`; no se sobrescribirá la anterior.
3. La sincronización actual vive en `ProductoRepositorioOffline.sincronizar`; no existe una clase SincronizarUseCase. Se refinará esa única ruta y se expondrá el caso de uso sin crear un segundo flujo de envío independiente.
4. SinPrivilegio/NoAutorizado se corresponden con ErrorSesion.SinPermiso/SesionExpirada. La traducción debe respetar esa separación y mantener la política de sesión.
5. No hay monitor de conectividad nativo, gestor de permisos ni integración Firebase. Solo está declarado INTERNET en el manifiesto.
6. La tabla aún no conserva intentos ni último error, y la UI no presenta contadores de pendientes/fallidas. Son trabajo pendiente, no requisitos ya cumplidos.

## Backend y entorno

- La carpeta original `H:/ProyectosUPEU/pharmaSoft` existe; la copia usada para el laboratorio con JWT está en `tmp/pharmaSoft-s11`, commit `fff44f9` del repositorio de referencia. No se modifica el código del profesor.
- Contrato real: `/api/v1/productos` y `/{id}`, GET/POST/PUT/DELETE. Categoría obligatoria, nombre de 3 a 150 caracteres, precio desde 0.01, stock no negativo y estado obligatorio.
- El servicio comprueba nombres duplicados y la base tiene un índice único de producto por nombre. No se encontró soporte de una clave de idempotencia: esa restricción de nombre no se presentará como un protocolo de reintento idempotente.
- Un POST cuyo resultado sea incierto no se reenviará automáticamente sin conciliación o decisión del usuario. Se documentará la política exacta en el paso 3.
- Al empezar estaban apagados Docker, Oracle, backend y AVD. El AVD ya arrancó; Docker terminó de iniciar mediante su CLI y se conservó el contenedor pharmasoft-oracle. Oracle y la copia del backend están arrancando. El recorrido contra servidor se documentará cuando se compruebe salud y disponibilidad.

## Preparación de Firebase

El estudiante creó personalmente el proyecto `pharmamobil-jorgequispe`, nombre PharmaMobil-JorgeQuispe, plan Spark. Después se registró la app Android con el applicationId verificado `pe.edu.upeu.pharmamobil`, alias PharmaMobil Android - Jorge Quispe. Se observa el paso Registrar app como completado. El archivo google-services.json aún no se ha confirmado en el sistema de archivos: el botón de descarga se accionó, pero el navegador no devolvió un archivo verificable; se pidió al estudiante guardarlo o adjuntarlo. No se declara instalado el SDK ni recibido un evento en consola.

Ningún archivo de configuración de Firebase se publicará sin la decisión del estudiante. No se solicitaron contraseñas ni se automatizó el inicio de sesión de Google ni la aceptación de condiciones.

## Pendientes inmediatos

Recorrer los ocho momentos del paso 1 con capturas reales, tabla de huecos y decisión por momento. No se inventarán problemas solo por parecer posibles al leer el código. Las pruebas reales, la migración y el resto de pasos se implementarán sobre la rama integrador-13 preservando la base comprobada.

## Actualización posterior a la auditoría

El estudiante descargó google-services.json y autorizó colocarlo en el proyecto. Se verificó proyecto pharmamobil-jorgequispe, paquete pe.edu.upeu.pharmamobil y coincidencia SHA-256 con el archivo descargado. Está en androidApp/google-services.json. El estudiante decidió mantenerlo fuera de GitHub; se agregó .gitignore y una exclusión local preventiva. No se incluyeron claves del archivo en la evidencia. Docker, Oracle y el backend ya están disponibles; GET /api/health respondió correctamente. La integración del SDK corresponde al paso 6 y todavía no se declara realizada.
