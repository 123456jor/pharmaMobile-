# Guion de sustentación - ocho minutos

## Preparación previa

Backend oficial de laboratorio en localhost:8080, Oracle en pharmasoft-oracle (puerto 1522), AVD Pixel_8_Pro abierto, modo avión desactivado, sesión Android cerrada antes de empezar y Postman abierto. Comprobar GET /api/health y categoría 1. Usuarios reales: admin con escritura y vendedor sin CATALOGO_ESCRIBIR; para el fallo usar admin con contraseña incorrecta. Ingresar claves únicamente durante la sesión; este documento no las contiene. Android usa 10.0.2.2:8080; Postman usa localhost:8080. El emulador no es necesario para consultar el servidor con Postman.

Postman sustituye Swagger por decisión expresa del usuario. iOS se declara sin evidencia. Preparar peticiones de login admin/vendedor, GET del producto y PUT restringido sin ejecutar altas o bajas ajenas. Usar un nombre nuevo en cada ensayo. Antes de empezar cerrar sesión desde la app para mostrar autenticación.

## 0-1 min. Autenticación JWT

**Qué digo:** «La app obtiene un JWT al iniciar sesión y lo guarda cifrado. Una contraseña incorrecta no concede acceso.»

**Qué hago:** Desde el login, demostrar primero el rechazo; después entrar correctamente. Alternativa de orden para no perder tiempo: login correcto, cerrar sesión, fallo y volver a entrar.

**Usuario:** admin; repetir admin con clave deliberadamente incorrecta.

**Riesgo y plan B:** El backend apagado produce error de conexión, no un rechazo de credenciales. Plan B: comprobar salud, recuperar servicio y repetir; no presentar el error de red como 401.

## 1-3 min. Consumo REST y CRUD

**Qué digo:** «La interfaz consulta SQLite; la sincronización envía primero los pendientes y después descarga todas las páginas del servidor.»

**Qué hago:** Abrir Productos, listar, crear un producto de demostración con nombre único, precio 5.50, stock 15, categoría 1. Mostrar ID positivo, editar precio y stock y verificar el cambio. Si el docente pide eliminar, dar de baja solo el producto de demostración; se trata de baja lógica.

**Usuario:** admin.

**Riesgo y plan B:** Categoría inexistente o nombre repetido impiden el alta. Plan B: categoría 1 comprobada y nombre distinto; no modificar productos del negocio.

## 3-5 min. Estados y errores

**Qué digo:** «Ocultar una acción en la app no basta: el backend también comprueba permisos. Los datos inválidos se señalan debajo de su campo.»

**Qué hago:** En Postman obtener JWT de vendedor y solicitar PUT al producto 83: debe responder 403 sin modificarlo. Volver a Android como admin, pulsar Registrar con campos vacíos y mostrar sus mensajes. La app oculta edición al vendedor; por eso el rechazo explícito se demuestra en Postman.

**Usuario:** vendedor para 403; admin para validación.

**Riesgo y plan B:** Sin token se obtiene 401, no 403. Plan B: rehacer login de vendedor y comprobar Authorization. No cambiar la aserción para aceptar otro estado.

## 5-7 min. Offline-first

**Qué digo:** «Estos datos vienen de la base del teléfono. El producto local recibe un ID negativo y se confirma con el ID del servidor cuando vuelve la conexión.»

**Qué hago:** Sincronizar, activar modo avión y abrir catálogo; registrar otro producto único y mostrar Pendiente. Desactivar modo avión, esperar conexión, pulsar Sincronizar y comprobar el ID en Postman. Usar producto 83 como evidencia ya documentada si hace falta.

**Usuario:** admin con sesión vigente.

**Riesgo y plan B:** El teclado puede ocultar la lista: cerrarlo y desplazar el inventario al inicio. Si vence la sesión, volver a iniciar sesión antes de enviar; la consulta local no permite escrituras.

## 7-8 min. Evidencia multiplataforma

**Qué digo:** «La lógica del repositorio y sus pruebas está en commonMain y commonTest. Aquí se ejecutó Android. No se adjunta una ejecución de iOS.»

**Qué hago:** Mostrar pruebas comunes, reporte de 94 casos y pantalla Android. El requisito original pide Android e iOS; este tramo es parcial porque se excluyó iOS y el equipo es Windows sin Xcode. No afirmar que pasó en iOS.

**Usuario:** admin en Android.

**Riesgo y plan B:** No hay plan B que equivalga a ejecutar iOS en este equipo. Para acreditar ese criterio completo se necesita un Mac y repetir la demostración. El estudiante decidió mantener el alcance Android.

## Comprobaciones y tareas personales

En comprobacion-defensa.json constan login correcto 200, incorrecto 401, PUT vendedor 403 y validación 400. La captura validacion_bajo_campos.png muestra errores reales en Android. Las acciones REST/Flow cuentan con pruebas del proyecto. El recorrido completo no se ha ensayado en voz alta ni se garantiza que la persona lo complete en ocho minutos: debe ensayarlo Jorge con cronómetro, estudiar cada línea, revisar conclusiones y preparar la demostración del 27 de octubre.
