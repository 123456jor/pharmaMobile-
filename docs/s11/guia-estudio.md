# Guía de estudio: cómo explicar lo agregado

## Producto 1: seis pruebas

Archivo: `shared/src/commonTest/kotlin/pe/edu/upeu/pharmamobil/data/repository/ProductoRepositorioOfflineAutonomaTest.kt`.

### Preparación, línea por línea útil

- `nuevo`: construye el producto de entrada con ID 0. El repositorio asignará otro ID; la pantalla no lo decide.
- `cabeceras`: indica que las respuestas simuladas contienen JSON.
- `Producto.dto()`: transforma el producto de dominio al contrato real que devuelve PharmaSoft.
- `pagina(...)`: serializa una página completa con la lista, cantidad y bandera `ultima=true`. No es una petición real.
- `conBaseNueva(...)`: recibe un motor HTTP y el bloque que comprobará un caso.
- `crearDriverEnMemoria()`: crea SQLite temporal mediante expect/actual; JDBC solo aparece en androidHostTest.
- `baseEnMemoria(driver)`: crea el esquema y entrega PharmaDb; cada llamada tiene una base independiente.
- `ProductoLocal(...)`: permite consultar y observar esa base.
- `crearHttpClient(... engine=motor)`: sustituye el transporte con MockEngine sin cambiar el código del repositorio.
- `try { comprobar(...) }`: ejecuta las aserciones con repositorio y base del mismo caso.
- `finally`: cierra cliente y driver incluso cuando una aserción falla.
- `@Test`: hace que Gradle descubra el caso. `runTest` controla corrutinas con un planificador de pruebas.
- `first()`: recoge la primera lista emitida por el Flow; no espera con sleep.
- `assertEquals`: compara lo esperado y lo obtenido. `assertTrue`: exige una condición, no la imprime solamente.

### Prueba 2: registro sin red

- `MockEngine { throw IOException(...) }`: simula que el transporte no puede alcanzar el servidor.
- `repo.registrar(nuevo)`: debe guardar primero en SQLite aunque el envío falle.
- `repo.observar().first().single()`: obtiene el único producto del listado local.
- `guardado.id < 0`: demuestra el identificador temporal.
- `emitido.pendiente`: exige que no se confunda con un envío confirmado.
- `assertEquals(guardado, emitido)`: compara todos sus campos.
- `local.pendientes()`: comprueba directamente la fila que deberá enviarse al reconectar.

### Prueba 4: la bajada conserva pendientes

- Se registra sin red y se prepara otro producto con ID 71.
- `local.reemplazarSincronizados(listOf(remoto))`: aísla la operación de descarga. Un POST fallido detiene la sincronización completa antes de descargar; por eso este caso comprueba directamente el reemplazo local.
- Cantidad 2: deben permanecer el pendiente y el remoto.
- Conjunto de IDs: exige ambos identificadores sin depender del orden alfabético.
- Comparar el producto pendiente completo: detecta si se perdió su marca o cambió algún dato.
- `local.pendientes()`: debe seguir conteniendo exactamente la fila original.

### Prueba 1: sincronización con red

- Se preparan dos productos del servidor con IDs 41 y 42.
- El motor exige método GET y devuelve una página JSON completa.
- `repo.sincronizar()`: descarga y confirma la lista en SQLite.
- Comparación de conjuntos: los datos emitidos deben coincidir con todos los productos del servidor.
- Cantidad 2, igualdad con `local.listar()` y pendientes vacíos: contrastan Flow, persistencia y estado de envío.

### Prueba 3: envío al reconectar

- `conRed=false`: la misma base inicia desconectada.
- `postAceptados=0`: cuenta únicamente POST aceptados por el motor.
- El motor rechaza sin red; con red devuelve el producto confirmado con ID 72 para POST y la página para GET.
- Se registra un pendiente y se comprueba su ID negativo.
- `conRed=true` y `repo.sincronizar()`: cambian el transporte sin reemplazar la base.
- POST igual a 1, ID 72 y marca pendiente falsa: acreditan el envío y su confirmación.
- Pendientes vacíos: ya no debe enviarse nuevamente ese registro.

### Prueba 5: identificadores sin colisión

- El producto existente del servidor usa ID 41; el local recibe ID negativo.
- Tras el POST, el nuevo obtiene ID 72; la descarga contiene los dos.
- Se exigen dos filas y ambos nombres y valores: contar filas sin comprobar contenido podría ocultar una sobrescritura.

### Prueba 6: segunda sincronización

- Se registra sin red y luego se habilita el motor.
- Se sincroniza dos veces sobre la misma base.
- Se comparan las dos listas completas y sus cantidades.
- Se exige un único POST en total y ningún pendiente. Una segunda subida o duplicación hace fallar el caso.

## Producto 3: antigüedad, línea por línea

- `Sincronizacion.sq`: declara tabla adicional; `id=1` restringe a una fila. `ultimaMillis` almacena epoch en milisegundos, no un texto ya formateado.
- `leerUltima`: lee solo esa marca; si no existe devuelve null mediante `executeAsOneOrNull`.
- `guardarUltima`: inserta o reemplaza la única fila.
- `1.sqm`: crea la tabla cuando una instalación pasa de esquema 1 a 2; no borra productos.
- `observarUltimaSincronizacion`: convierte la consulta SQL en Flow. La interfaz recibe cambios automáticamente.
- `confirmarSincronizacion`: engloba reemplazo y fecha en una transacción para no actualizar solo una mitad.
- `ahora: () -> Long`: inyecta un reloj. En producción consulta Clock.System; el test entrega un número fijo.
- El repositorio llama esa transacción después de enviar pendientes y terminar todas las páginas. Si algo falla antes, la marca no cambia.
- `formatearAntiguedad(ahora, ultima)`: función pura; la misma entrada siempre da el mismo texto.
- `ultima == null`: devuelve «Aún no se ha sincronizado».
- `coerceAtLeast(0)`: un reloj atrasado no muestra antigüedad negativa.
- Dividir entre 60000 convierte milisegundos en minutos enteros.
- Cero minutos: segundos; menos de 60: minutos; menos de 1440: horas; el resto: días.
- Las condiciones de singular evitan «1 minutos» o «1 días».
- ProductoViewModel recoge el Flow y copia la fecha al estado de pantalla.
- ProductoScreen recuerda el instante actual. Su LaunchedEffect actualiza el texto cada minuto; ese delay pertenece a la interfaz de producción, no a las seis pruebas ni a HTTP.
- Las cinco pruebas del formateador cubren nulo, segundos, minutos, horas, días y reloj atrasado. La prueba del repositorio exige conservar la marca tras un fallo.

## Producto 2: sesión vencida y evidencia

- `ConsultaLocalException`: señala que solo puede consultarse SQLite. No es un objeto Sesion ni concede permisos.
- `sesionGuardada`: intenta comprobar auth/me también cuando no hay token. Con red, el 401 conduce al login; sin red, el fallo de transporte permite consulta local.
- `guardada`: solo acepta la sesión cifrada que corresponde al token almacenado.
- `sesionSinRed`: si la sesión sigue vigente conserva el comportamiento previo. Si venció o no existe, borra credenciales y solicita consulta local.
- `consultaLocal` y `sesionLocalExpirada`: son estado de interfaz, no permisos del servidor.
- SesionViewModel recibe la excepción y deja `sesion=null`; mantiene la bandera de consulta.
- El ViewModel de productos de consulta usa otra clave de Koin. No inicia sincronización.
- `iniciarOperacion` y `sincronizar`: retornan inmediatamente en modo consulta; no basta con esconder un botón.
- ProductoScreen sin escritura omite el formulario y los controles de editar/eliminar; el botón de sincronizar queda deshabilitado.
- App comprueba conexión cada cinco segundos mientras está en consulta. Cuando auth/me vuelve a responder pide login. La comprobación de la fecha también se programa si la sesión vence con la app abierta.
- `DEBUG_METADATA_SESION`: solo en APK debug; lee exp del JWT y registra fechas, nunca el token.
- `DEBUG_ANTIGUEDAD_26_HORAS`: modifica únicamente la marca de sincronización en depuración; no inserta productos ni cambia el reloj. La captura declara el método.
- `consultar_base.py`: copia el archivo real del emulador con run-as, lo abre con SQLite y guarda el resultado de SELECT en JSON. No agrega filas.
- Las capturas son screencap de Android y captura nativa de Postman. Los JSON describen lo consultado en el instante de cada escenario.

## Producto 4 y revisión personal

El guion indica lo que diré y haré, usuario, tiempo y plan B por tramo. Debo practicarlo realmente con cronómetro y entender por qué un 401 es distinto de un 403. Postman es otro cliente del backend: no necesita que el emulador esté corriendo. Docker mantiene Oracle y el backend atiende HTTP; SQLDelight conserva los datos del teléfono cuando esos servicios no se pueden alcanzar.

Debo revisar las conclusiones de los escenarios antes de asumirlas como propias. No se ejecutó iOS ni se simuló el ensayo. Los cambios están en una rama local; su publicación requiere mi confirmación final. La entrega al aula y la sustentación las realizo personalmente.

- Refuerzo final: SesionViewModel retira inmediatamente una sesión ya vencida antes de esperar HTTP. Un rechazo 401 no conserva el objeto Sesion anterior. AuthRepositorioRest borra el token vencido antes de auth/me; el nuevo test exige ausencia de Authorization. Las dos pruebas del ViewModel controlan la respuesta con CompletableDeferred, sin dormir ni usar red.
