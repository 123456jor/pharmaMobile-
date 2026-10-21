# Práctica 11 - Bitácora real

## Reconocimiento y línea base - 21/10/2026

- Windows 11, JDK Microsoft 21.0.12.1, Gradle 9.1.0, AGP 9.0.1, Kotlin del proyecto 2.4.10, Compose 1.11.1.
- Proyecto limpio al iniciar en `feature/auth-jwt-quispe`, commit `ef38718`.
- No existe `develop`. El usuario autorizó crear `feature/offline-first-quispe` desde la rama de sesión 10.
- El usuario autorizó conservar editar/eliminar con conexión. iOS está expresamente fuera del alcance.
- Docker/Oracle, PharmaSoft perfil dev en 8080, AVD Pixel_8_Pro API 37.1.
- Primer intento de API/instalación se adelantó al arranque: conexión rechazada y emulador iniciando. Se esperó al arranque y se repitió.
- Compilación base `:androidApp:assembleDebug`: BUILD SUCCESSFUL.
- API real: login 200, GET productos 200, 7 productos, JWT 3600 segundos. Ver `linea-base-api.json`.
- Login Android correcto. Captura del listado: `capturas/00-linea-base-android.png`.
- Modo avión disponible por `adb shell cmd connectivity airplane-mode`: disabled.
- Swagger configurado en el backend: `/swagger-ui.html`.

## Adaptación del esquema

Se mantienen los campos de la guía y se añaden estado/categoriaId/categoriaNombre porque el dominio y el backend actuales los necesitan. El POST no debe perder la categoría al guardar offline. No se almacena ningún DTO en la capa local.

## Implementación y controles

- SQLDelight 2.1.0 generó PharmaDb y compiló. Salida: pruebas/01-esquema.txt.
- Android crea pharma.db. El driver abre el archivo de forma diferida: se añadió la primera consulta al inicializar para comprobar la creación real. Salidas: pruebas/02-driver.txt y 02-pharma-db.txt.
- ProductoLocal compila, usa transacciones, Flow y dominio; no importa Ktor ni DTO.
- La prueba del algoritmo literal falló: se esperó 1 fila y aparecieron 2. Se conservó la salida y XML en pruebas/04-duplicado-antes.*. Corrección D-2: borrarPorId y confirmarSubida transaccional después del POST confirmado.
- Ocho pruebas del repositorio aprobadas; después suite completa: 76 pruebas, 0 fallos, 0 errores, 0 omitidas. Log completo salida-pruebas.txt y reporte HTML conservados.
- Se retiraron cargarProductos y recargarTrasMutacion del ViewModel de productos. La fase solo se actualiza desde observar(). Editar/eliminar siguen online y escriben la confirmación en la base local.
- Para abrir sin red se guarda la sesión ya autenticada, junto con su vencimiento, en las mismas preferencias cifradas de Android. Solo se restaura si sigue vigente y la red falla; 401 y sesión vencida no usan la caché. Tres pruebas adicionales cubren esos casos.

## Ciclo real Android - 21/10/2026, hora de Lima

- Se renovó el login admin tras la pausa de la conversación, antes del ciclo offline.
- Con red: 4 productos activos, incluido A_Prueba_con_red_S11_1431, ID 43. Se cerró la app por completo.
- Modo avión activado; también se deshabilitaron Wi-Fi y datos para evitar que Android mantuviera Wi-Fi encendido. El registro modo-avion.txt muestra enabled y ausencia de ruta.
- Reapertura sin red correcta: listado guardado y aviso de conexión visible.
- Registro sin red A_Offline_S11_1531: precio 6.75, stock 12, categoría 1, ID -1, pendiente 1. Consulta de copia SQLite real: fila-pendiente.json.
- Reconexión y botón Sincronizar: misma fila pasó a ID 63 y pendiente 0; cero pendientes y una sola coincidencia. Comprobación en sincronizacion-local.json.
- El usuario indicó explícitamente usar Postman en lugar de Swagger para el entregable. Se importó S11 Offline-first - Verificacion real. Login 200; listado 200 con tres pruebas PASSED: HTTP 200, producto existe una sola vez, ID real positivo. Captura 07b-postman-listado-pruebas.jpg.

- Búsqueda Postman GET /productos/{{productoId}}: 200 OK y cuerpo real con ID 63, nombre A_Offline_S11_1531, precio 6.75, stock 12, categoría Medicamentos. Captura 07-postman-producto-id-real.jpg. No se incluyeron JWT en las capturas del entregable ni en la colección exportada.
- PharmaSoft detenido a las 15:36 (PID 31796 confirmado por clase principal). Health no respondió y se comprobó que no había proceso escuchando en 8080. Android con modo avión desactivado y rutas activas se cerró y reabrió: mostró 5 productos y aviso de conexión. Captura 08-backend-apagado-listado-android.png.
- PharmaSoft reiniciado: health 200 a las 15:38. Se restableció la sincronización Android. Oracle/Docker siguió funcionando.
- Captura 05-fila-pendiente.jpg: visor Bloc de notas del JSON obtenido por consulta SQLite sobre una copia auténtica de pharma.db; no es Database Inspector ni una respuesta HTTP simulada. Los originales de Android provienen de screencap/adb pull y los de Postman/visor del capturador de ventanas.
La captura 02 se repitió en Ajustes después del ciclo para evitar una imagen tomada durante la animación; el modo avión volvió a desactivarse al terminar.

## Entregable y revisión final

- PDF de 15 páginas creado con capturas originales sin modificación. Las páginas de escritorio se ampliaron para leer la respuesta y los IDs.
- Se renderizaron las 15 páginas y se revisó su distribución; comprobación de texto fuera de página: cero casos. Se verificó la apertura de las 13 capturas originales.
- Se conservó la salida completa, el reporte HTML de pruebas, el checklist individual y los hashes de las evidencias.
- No se incluyeron JWT en las evidencias del entregable ni en la colección exportada.
- Publicación de la rama pendiente de la confirmación final solicitada por el prompt; no se ejecutó git push.
