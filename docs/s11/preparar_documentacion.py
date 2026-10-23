"""Organiza únicamente resultados y capturas ya obtenidos en la ejecución real."""
from pathlib import Path
import json
import subprocess

D = Path(__file__).parent
R = D.parent.parent
URL = 'https://github.com/123456jor/pharmaMobile-/tree/feature/s11-autonoma-quispe'
def leer(nombre):
    return json.loads((D/'consultas'/f'{nombre}.json').read_text(encoding='utf-8-sig'))

datos = [
 ('E1. Apertura sin red con datos previos','esc1',
  'Con red: iniciar sesión como admin y abrir Productos; sincronizar. Forzar cierre, activar modo avión y desactivar wifi y datos. Reabrir y entrar a Productos.',
  'Inventario visible, cinco productos activos y aviso «Sin conexión: mostrando datos guardados».',
  'esc1_listado_sin_red.png',
  'Cerrar el proceso no elimina la copia local: el catálogo sigue disponible sin volver a descargarlo.'),
 ('E2. Registro sin red','esc2_final',
  'Mantener modo avión. Registrar AA_Autonoma_Jorge_23Oct, precio 8.50, stock 17, categoría 1. Cerrar teclado y desplazar el inventario al inicio para ver el nuevo registro.',
  'Seis productos activos; el nuevo aparece con «Pendiente de sincronizar · ID -1». Se intentó reconectar antes de tomar la captura final, pero la consulta comprobó que no hubo envío; se volvió a modo avión para documentar el pendiente.',
  'esc2_pendiente_visible.png',
  'El registro se conserva con ID temporal negativo aunque el servidor no pueda recibirlo.'),
 ('E3. Reconexión y envío','esc3',
  'Desactivar modo avión, habilitar wifi y datos, esperar conexión y pulsar Sincronizar. En Postman: login de la colección S11 Autonoma Jorge, después GET /api/v1/productos/83.',
  'Desaparece la marca pendiente; el producto queda con ID 83. Postman responde 200 OK y devuelve nombre AA_Autonoma_Jorge_23Oct, precio 8.5 y stock 17.',
  'esc3_producto_sincronizado.png',
  'El GET independiente de Postman confirma que el producto llegó a PharmaSoft y recibió el ID 83.'),
 ('E4. Primera apertura sin red','esc4',
  'Confirmar cero pendientes y respaldar SQLite. Forzar cierre, activar modo avión, ejecutar pm clear pe.edu.upeu.pharmamobil y abrir la app.',
  '«Todavía no hay productos», explicación para conectarse e iniciar sesión y «Aún no se ha sincronizado». No se presenta una pantalla de error.',
  'esc4_primera_apertura_sin_red.png',
  'Una instalación sin caché informa que no tiene datos en vez de aparentar un fallo del inventario.'),
 ('E5. Sesión expirada sin red','esc5_final',
  'Arrancar temporalmente el backend con APP_JWT_EXPIRACION_MINUTOS=1. Iniciar sesión en Android, sincronizar y cerrar. Esperar el vencimiento emitido por el backend. Activar modo avión, forzar cierre y comprobar metadatos antes de reabrir. Abrir ya con jwt_exp=1792783267 y ahora=1792783361. Después reconectar y comprobar el login.',
  '«Sesión vencida. Sin conexión: solo consulta local», seis productos activos, sin formulario ni botones de edición/eliminación y Sincronizar deshabilitado. El token se borra. Al reconectar se solicita iniciar sesión nuevamente.',
  'esc5_consulta_local_verificada.png',
  'Vencer la autorización bloquea los cambios, pero no impide consultar el inventario ya guardado en este dispositivo.')
]
registros=[]
for nombre, consulta, pasos, pantalla, imagen, conclusion in datos:
    db=leer(consulta)
    fila=next((f for f in db['filas'] if f['nombre']=='AA_Autonoma_Jorge_23Oct'),None)
    resumen=f"{len(db['filas'])} filas totales, {sum(f['estado'] for f in db['filas'])} activas, {sum(f['pendiente'] for f in db['filas'])} pendientes."
    if fila: resumen+=f" Producto observado: id={fila['id']}, pendiente={fila['pendiente']}, precio={fila['precio']}, stock={fila['stock']}."
    resumen+=f" Marca de sincronización: {db['sincronizacion']}. Consulta: {db['consulta']}. Archivo: consultas/{consulta}.json."
    registros.append(dict(zip(['Identificador y nombre','Fecha y hora real','Plataforma','Pasos exactos seguidos','Qué mostró la aplicación','Qué había en la base local','Captura','Conclusión en una línea'],
        [nombre,db['fecha_hora_lima'],'Android - Pixel 8 Pro, API 37.1',pasos,pantalla,resumen,'capturas/'+imagen,conclusion])))
(D/'bitacora.json').write_text(json.dumps(registros,ensure_ascii=False,indent=2),encoding='utf8')
md='# Bitácora de cinco escenarios\n\nFechas en America/Lima. El emulador está en GMT y su barra usa reloj de 12 horas; no se alteró el reloj. Las consultas contienen filas inactivas que la pantalla filtra.\n\n'
for r in registros:
    md+='## '+r['Identificador y nombre']+'\n\n'+'\n\n'.join(f'**{i}. {k}:** {v}' for i,(k,v) in enumerate(r.items(),1))+'\n\n'
md+='## Argumento del escenario 5\n\nLa versión anterior abría el login sin conexión aunque SQLite conservaba nueve filas. La captura anterior ilustra ese estado, pero no mide la expiración de su token. La prueba final sí usa un JWT genuino emitido con duración de un minuto, registra su exp y comprueba el vencimiento antes de reabrir. No se modificó su firma ni se falsificó un token. En el primer intento se abrió antes de vencer y se observó Inicio; se repitió después de verificar exp < ahora. Solo la repetición correcta se usa como evidencia final.\n\nMostrar datos locales en consulta es útil porque sin internet tampoco es posible autenticarse. No equivale a renovar la sesión: se borra el JWT vencido, no se conceden permisos, no se admiten mutaciones ni sincronización y al reconectar se exige login. Los datos quedan limitados al dispositivo de laboratorio; el usuario debe entender que son una copia con la antigüedad indicada. Cambios en de29d57; comprobaciones adicionales y revisión de expiración activa en c052b69.\n\nLa duración normal del backend se restauró a 3600 segundos (comprobacion-defensa.json). Se dejaron wifi y datos habilitados, modo avión desactivado, diez filas locales y cero pendientes. Las conclusiones quedan para revisión personal del estudiante.\n'
(D/'bitacora.md').write_text(md,encoding='utf8')

tramos=[
 ('0-1 min. Autenticación JWT','admin; repetir admin con clave deliberadamente incorrecta',
  '«La app obtiene un JWT al iniciar sesión y lo guarda cifrado. Una contraseña incorrecta no concede acceso.»',
  'Desde el login, demostrar primero el rechazo; después entrar correctamente. Alternativa de orden para no perder tiempo: login correcto, cerrar sesión, fallo y volver a entrar.',
  'El backend apagado produce error de conexión, no un rechazo de credenciales. Plan B: comprobar salud, recuperar servicio y repetir; no presentar el error de red como 401.'),
 ('1-3 min. Consumo REST y CRUD','admin',
  '«La interfaz consulta SQLite; la sincronización envía primero los pendientes y después descarga todas las páginas del servidor.»',
  'Abrir Productos, listar, crear un producto de demostración con nombre único, precio 5.50, stock 15, categoría 1. Mostrar ID positivo, editar precio y stock y verificar el cambio. Si el docente pide eliminar, dar de baja solo el producto de demostración; se trata de baja lógica.',
  'Categoría inexistente o nombre repetido impiden el alta. Plan B: categoría 1 comprobada y nombre distinto; no modificar productos del negocio.'),
 ('3-5 min. Estados y errores','vendedor para 403; admin para validación',
  '«Ocultar una acción en la app no basta: el backend también comprueba permisos. Los datos inválidos se señalan debajo de su campo.»',
  'En Postman obtener JWT de vendedor y solicitar PUT al producto 83: debe responder 403 sin modificarlo. Volver a Android como admin, pulsar Registrar con campos vacíos y mostrar sus mensajes. La app oculta edición al vendedor; por eso el rechazo explícito se demuestra en Postman.',
  'Sin token se obtiene 401, no 403. Plan B: rehacer login de vendedor y comprobar Authorization. No cambiar la aserción para aceptar otro estado.'),
 ('5-7 min. Offline-first','admin con sesión vigente',
  '«Estos datos vienen de la base del teléfono. El producto local recibe un ID negativo y se confirma con el ID del servidor cuando vuelve la conexión.»',
  'Sincronizar, activar modo avión y abrir catálogo; registrar otro producto único y mostrar Pendiente. Desactivar modo avión, esperar conexión, pulsar Sincronizar y comprobar el ID en Postman. Usar producto 83 como evidencia ya documentada si hace falta.',
  'El teclado puede ocultar la lista: cerrarlo y desplazar el inventario al inicio. Si vence la sesión, volver a iniciar sesión antes de enviar; la consulta local no permite escrituras.'),
 ('7-8 min. Evidencia multiplataforma','admin en Android',
  '«La lógica del repositorio y sus pruebas está en commonMain y commonTest. Aquí se ejecutó Android. No se adjunta una ejecución de iOS.»',
  'Mostrar pruebas comunes, reporte de 94 casos y pantalla Android. El requisito original pide Android e iOS; este tramo es parcial porque se excluyó iOS y el equipo es Windows sin Xcode. No afirmar que pasó en iOS.',
  'No hay plan B que equivalga a ejecutar iOS en este equipo. Para acreditar ese criterio completo se necesita un Mac y repetir la demostración. El estudiante decidió mantener el alcance Android.')
]
guion='# Guion de sustentación - ocho minutos\n\n## Preparación previa\n\nBackend oficial de laboratorio en localhost:8080, Oracle en pharmasoft-oracle (puerto 1522), AVD Pixel_8_Pro abierto, modo avión desactivado, sesión Android cerrada antes de empezar y Postman abierto. Comprobar GET /api/health y categoría 1. Usuarios reales: admin con escritura y vendedor sin CATALOGO_ESCRIBIR; para el fallo usar admin con contraseña incorrecta. Ingresar claves únicamente durante la sesión; este documento no las contiene. Android usa 10.0.2.2:8080; Postman usa localhost:8080. El emulador no es necesario para consultar el servidor con Postman.\n\nPostman sustituye Swagger por decisión expresa del usuario. iOS se declara sin evidencia. Preparar peticiones de login admin/vendedor, GET del producto y PUT restringido sin ejecutar altas o bajas ajenas. Usar un nombre nuevo en cada ensayo. Antes de empezar cerrar sesión desde la app para mostrar autenticación.\n\n'
for tiempo,usuario,decir,accion,riesgo in tramos:
    guion+=f'## {tiempo}\n\n**Qué digo:** {decir}\n\n**Qué hago:** {accion}\n\n**Usuario:** {usuario}.\n\n**Riesgo y plan B:** {riesgo}\n\n'
guion+='## Comprobaciones y tareas personales\n\nEn comprobacion-defensa.json constan login correcto 200, incorrecto 401, PUT vendedor 403 y validación 400. La captura validacion_bajo_campos.png muestra errores reales en Android. Las acciones REST/Flow cuentan con pruebas del proyecto. El recorrido completo no se ha ensayado en voz alta ni se garantiza que la persona lo complete en ocho minutos: debe ensayarlo Jorge con cronómetro, estudiar cada línea, revisar conclusiones y preparar la demostración del 27 de octubre.\n'
(D/'guion-sustentacion.md').write_text(guion,encoding='utf8')

filas=[]
def fila(a,r,accion,res,evi,ver,estado='Verificado'): filas.append([a,r,accion,res,evi,ver,estado])
requisitos=['Tras sincronizar, la base contiene los productos del servidor y el Flow emite la lista completa.',
'Al fallar el envío, el producto queda guardado con la marca de pendiente y el Flow lo emite igualmente.',
'Sincronizar con el servidor disponible envía lo pendiente y la fila deja de estar marcada.',
'Traer la lista del servidor conserva las filas que el usuario creó sin conexión.',
'Un producto local y uno del servidor con numeración distinta conviven sin sobrescribirse.',
'Ejecutar la sincronización dos veces seguidas deja la misma cantidad de filas.']
for i,r in enumerate(requisitos,1):fila(f'P1-{i}',r,f'prueba{i:02} en commonTest','PASSED','salida-pruebas.txt; reporte-pruebas/','XML y consola: seis obligatorias, cero omitidas')
fila('P1-salida completa','Adjunta la salida de la ejecución completa, no una captura de un solo método en verde.','Guardar consola íntegra','94/94','salida-pruebas.txt','BUILD SUCCESSFUL')
fila('P1-sin esperas/emulador/red','Ninguna debe necesitar emulador, red ni esperas.','runTest + MockEngine','Sin peticiones reales ni sleeps','ProductoRepositorioOfflineAutonomaTest.kt','Inspección de las seis pruebas')
fila('P1-base propia por prueba','Cada prueba crea su propia base en memoria','Crear y cerrar driver por caso','Bases independientes','mapa-codigo.md','conBaseNueva llama crearDriverEnMemoria')
fila('P1-pruebas sin comentar','Una prueba comentada no acredita nada.','Revisar @Test y @Ignore','Seis activas','resumen-pruebas.json','0 omitidas; búsqueda sin @Ignore')
for i,r in enumerate(registros,1):fila(f'P2-E{i}','Provoca los cinco escenarios siguientes en el emulador y documenta cada uno con captura y resultado observado.','Ejecutar pasos de bitácora','Ocho campos y SQL real',r['Captura']+'; bitacora.json','Captura inspeccionada y consulta guardada')
fila('P2-Swagger en E3','Comprueba el escenario 3 en Swagger, no solo en la aplicación','Usuario ordenó usar Postman','GET 83 = 200; requisito literal Swagger no ejecutado','capturas/esc3_postman_producto_83.jpg','Respuesta real de PharmaSoft','Pendiente')
fila('P2-argumento E5','documenta qué hace hoy tu aplicación y argumenta si es el comportamiento correcto.','Comparar antes y después','Consulta sin permisos y reautenticación','bitacora.md; expiracion-antes-reabrir.txt','JWT emitido y vencido, token borrado')
for a,r,accion,evi in [
 ('P3-registro','Guardar la marca de tiempo de cada sincronización correcta.','Transacción de lista y fecha tras sync completa','antiguedad-y-migracion.md'),
 ('P3-lenguaje natural','Mostrarla en la pantalla de productos en lenguaje natural','Formateador puro y actualización visual por minuto','capturas/antiguedad_reciente.png'),
 ('P3-nunca sincronizado','Si nunca se sincronizó, indicarlo explícitamente','Caso nulo y prueba commonTest','capturas/esc4_primera_apertura_sin_red.png'),
 ('P3-captura reciente','Captura con datos recientes y otra con datos de más de un día de antigüedad.','Sincronizar y capturar','capturas/antiguedad_reciente.png'),
 ('P3-captura >1 día','Captura con datos recientes y otra con datos de más de un día de antigüedad.','Editar solo marca debug a hace 26 h; restaurar mediante sync','capturas/antiguedad_26_horas_controlada.png')]:fila(a,r,accion,'Verificado en Android',evi,'Pruebas y consulta real')
fila('P4-5 tramos con tiempos','Prepara un guion de demostración de ocho minutos que los recorra en orden.','Cinco tramos, acciones, riesgos y plan B','Guion escrito; iOS sin ejecución','guion-sustentacion.md','Revisión contra guía','Completado')
fila('P4-usuarios y preparación','Incluye además qué usuario usarás en cada tramo','Usuarios admin/vendedor, servicios y fallos','Preparación sin contraseñas','guion-sustentacion.md','Estados 200/401/403/400 comprobados')
fila('P4-iOS','La misma funcionalidad ejecutándose en Android y en iOS.','Mantener alcance Android del usuario','Sin Mac ni Xcode; no se simula','entorno.md','Windows; exclusión expresa','Bloqueado')
fila('P4-ensayo','Ensaya el guion de sustentación en voz alta con el cronómetro.','Corresponde al estudiante','Por realizar','guion-sustentacion.md','No se simula','Pendiente')
fila('PDF-nombre','S11_ActividadAutonoma_Apellidos.pdf','Usar Quispe_Huaman','PDF preparado','../../output/pdf/S11_ActividadAutonoma_Quispe_Huaman.pdf','Revisión visual de páginas','Completado')
fila('PDF-portada','Incluye portada con nombre completo, sesión, fecha y enlace a la rama del repositorio.','Portada con identidad del estudiante','Incluida; publicación pendiente','../../output/pdf/S11_ActividadAutonoma_Quispe_Huaman.pdf','Campos contrastados','Completado')
fila('PDF-enlace a rama','Los commits de esta actividad están en mi rama y son accesibles desde el enlace entregado.','Preparar URL de rama nueva','Se publicará solo con confirmación',URL,'Commits locales; remoto aún pendiente','Pendiente')
fila('commits en la rama','actualiza tu rama del repositorio con los commits correspondientes.','Commits pequeños con autoría Jorge','Confirmados localmente','commits.txt','git log')
fila('commits de correcciones citados','corrige el código y registra el commit de la corrección en el documento.','Citar fc3067e/de29d57/c052b69','Referencias incluidas','bitacora.md; commits.txt','git show')
fila('lista de cotejo 1-11','Verifica cada punto antes de subir el archivo al aula virtual.','Responder según evidencia','No se marca 100 %','lista-cotejo.json','Swagger/iOS/publicación declarados','Completado')
matrix='# Matriz de cumplimiento\n\nLas citas se transcriben del PDF de actividad; los saltos de línea de tablas se normalizan. Postman e iOS reflejan las decisiones expresas del usuario.\n\n'
matrix+='| Actividad | Requisito (cita exacta de la guía) | Acción | Resultado | Evidencia | Verificación | Estado |\n|---|---|---|---|---|---|---|\n'
matrix+='\n'.join('| '+' | '.join(str(c).replace('|','/') for c in f)+' |' for f in filas)+'\n'
(D/'matriz.md').write_text(matrix,encoding='utf8')
check=[
 (1,'PDF con nombre S11_ActividadAutonoma_Quispe_Huaman.pdf','Sí'),
 (2,'Portada: nombre completo, sesión, fecha y enlace a rama','Sí; enlace pendiente de publicación'),
 (3,'Seis pruebas en commonTest y todas pasan','Sí'),
 (4,'Salida completa de la ejecución adjunta','Sí'),
 (5,'Cinco escenarios con capturas respectivas','Sí'),
 (6,'Escenario 3 con evidencia desde Swagger','No: Postman por instrucción del usuario'),
 (7,'Escenario 5 argumenta el comportamiento observado','Sí; revisión personal pendiente'),
 (8,'Pantalla indica última sincronización correcta','Sí'),
 (9,'Caso nunca sincronizado resuelto','Sí'),
 (10,'Guion cubre los cinco criterios de Producto U2','No completamente: iOS excluido; Android documentado'),
 (11,'Commits accesibles desde el enlace entregado','No todavía: falta confirmar publicación')]
(D/'lista-cotejo.json').write_text(json.dumps(check,ensure_ascii=False,indent=2),encoding='utf8')
commits=subprocess.check_output(['git','log','--reverse','--format=%h %an <%ae> %s','7adc1ad..HEAD'],cwd=R).decode('utf8')
(D/'commits.txt').write_text(commits,encoding='utf8')
print('Bitácora, guion, matriz, cotejo y registro de commits preparados.')
