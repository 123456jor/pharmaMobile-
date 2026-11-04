"""Entregable reproducible con capturas originales y estado verificado de la sesión 13."""
from pathlib import Path
import datetime, json, subprocess, hashlib, xml.etree.ElementTree as ET
from html import escape
from reportlab.pdfgen import canvas
from reportlab.lib.pagesizes import A4
from reportlab.lib.colors import HexColor, white
from reportlab.lib.styles import ParagraphStyle
from reportlab.platypus import Paragraph
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from PIL import Image
from pypdf import PdfReader

ROOT=Path(__file__).resolve().parents[2]
OUT=ROOT/'output/pdf/Entregable_Sesion13_Proyecto_Integrador_Jorge_Quispe.pdf'
EVI=ROOT/'evidencias/sesion13'
OUT.parent.mkdir(parents=True,exist_ok=True)
FONT='Helvetica'; BOLD='Helvetica-Bold'
if Path('C:/Windows/Fonts/arial.ttf').exists():
    pdfmetrics.registerFont(TTFont('ArialS13','C:/Windows/Fonts/arial.ttf'))
    pdfmetrics.registerFont(TTFont('ArialS13B','C:/Windows/Fonts/arialbd.ttf'))
    FONT='ArialS13';BOLD='ArialS13B'
styles={
 'body':ParagraphStyle('body',fontName=FONT,fontSize=10,leading=15,textColor=HexColor('#172033')),
 'small':ParagraphStyle('small',fontName=FONT,fontSize=8.3,leading=12,textColor=HexColor('#596174')),
 'h':ParagraphStyle('h',fontName=BOLD,fontSize=19,leading=25,textColor=HexColor('#8B1023')),
 'sub':ParagraphStyle('sub',fontName=BOLD,fontSize=12,leading=17,textColor=HexColor('#172033'))}
W,H=A4
C=canvas.Canvas(str(OUT),pagesize=A4)
C.setTitle('Proyecto integrador PharmaMobil - Sesión 13 - Jorge Quispe')
C.setAuthor('Jorge Quispe')
C.setSubject('Evidencias reales Android y Postman de refinamiento offline-first')
page=0; y=0; manifest=[]

def text(s,kind='body',x=46,width=None):
 global y
 p=Paragraph(s,styles[kind]);_,h=p.wrap(width or W-92,H)
 if y-h<58:newpage()
 p.drawOn(C,x,y-h);y-=h+10

def newpage(title=None):
 global page,y
 if page:C.showPage()
 page+=1
 C.setFillColor(HexColor('#C8102E'));C.rect(0,H-10,W,10,fill=1,stroke=0)
 C.setFillColor(HexColor('#596174'));C.setFont(FONT,8)
 C.drawString(46,H-32,'PharmaMobil  /  Proyecto integrador  /  Sesión 13')
 C.drawString(46,29,'Jorge Quispe  ·  Android  ·  Evidencias del 4 de noviembre de 2026')
 C.drawRightString(W-46,29,str(page))
 y=H-62
 if title:text(title,'h')

def evidence(name,title,caption,folder='capturas'):
 global y
 p=EVI/folder/name
 if not p.exists():return
 newpage(title)
 text(caption,'body')
 with Image.open(p) as im:iw,ih=im.size
 maxh=y-76;maxw=W-92;ratio=min(maxw/iw,maxh/ih)
 dw,dh=iw*ratio,ih*ratio
 C.drawImage(str(p),(W-dw)/2,y-dh,width=dw,height=dh,preserveAspectRatio=True,mask='auto')
 y-=dh+8
 fecha=''
 meta=p.with_suffix('.json')
 if meta.exists():
  obj=json.loads(meta.read_text(encoding='utf8'));fecha=obj.get('fecha_hora','')
 C.setFont(FONT,7);C.setFillColor(HexColor('#596174'))
 C.drawString(46,51,name[:98]);C.drawRightString(W-46,40,fecha)
 manifest.append({'archivo':str(p.relative_to(ROOT)).replace('\\','/'),'sha256':hashlib.sha256(p.read_bytes()).hexdigest(),'pagina_pdf':page})

estado={}
if (EVI/'estado_entregable.json').exists():estado=json.loads((EVI/'estado_entregable.json').read_text(encoding='utf8'))
final=estado.get('finalizado',False)
newpage('Proyecto integrador PharmaMobil')
text('Refinamiento offline-first, permisos nativos y Firebase','sub')
text('Guía práctica de laboratorio N.º 13<br/>Desarrollo de Aplicaciones Móviles<br/>Universidad Peruana Unión')
text('Estudiante: <b>Jorge Quispe</b><br/>Correo: joorge0148@gmail.com<br/>Fecha: 4 de noviembre de 2026')
text('Repositorio y rama de trabajo','sub')
link='https://github.com/123456jor/pharmaMobile-/tree/feature/integrador-13'
text('<link href="'+link+'" color="#C8102E">'+link+'</link>')
text('Alcance: Android y backend PharmaSoft. iOS se excluye por indicación del estudiante. Las solicitudes se comprueban en Postman.')
text('Estado del informe','sub')
text('Versión final verificada.' if final else '<b>Avance verificado.</b> Los pasos 1 a 4 están implementados y comprobados. Los permisos nativos, la integración del SDK de Firebase y la ampliación final de pruebas siguen en desarrollo; sus capturas se incorporarán después de ejecutarlos. Esta versión no acredita todavía el cumplimiento total de la guía.')
text('Las imágenes de este documento son capturas originales del emulador o de Postman. No se reconstruyeron pantallas ni respuestas. El inventario se guarda en SQLDelight y consume el backend real. Cada figura se relaciona con su archivo de evidencia.')
newpage('Resultados comprobados')
text('Se auditó el historial antes de elegir base. develop se creó desde feature/s11-autonoma-quispe (8184dc7), que incluía el trabajo previo. La implementación se realiza en feature/integrador-13; los commits nuevos usan Jorge Quispe &lt;joorge0148@gmail.com&gt;.')
for h,b in [
 ('Recorrido del flujo completo','Ocho momentos reales: primera apertura, login incorrecto, base vacía, creación sin red, reconexión, validación del servidor, expiración en formulario y cierre de sesión. Se documentaron los huecos y no se inventaron incidencias.'),
 ('Cierre de sesión','Se corrigió con autorización del estudiante el acceso invitado al catálogo después de logout. La bandera queda en preferencias cifradas; se conservan los pendientes. La expiración mantiene la consulta local permitida y no equivale a un cierre voluntario.'),
 ('Migración de la base','La migración 2.sqm lleva el esquema de versión 2 a 3 y añade intentos y ultimoError. Una actualización real sin desinstalar conservó doce filas, un pendiente, todos los campos y la fecha anterior. La tarea SQLDelight también pasó tras corregir el temporal de su trabajador en Windows.'),
 ('Sincronización','Un único flujo con Mutex, envío antes de descarga, máximo cinco fallos y espera creciente. Un rechazo permanente queda visible. Un POST con respuesta perdida no se reproduce automáticamente: requiere conciliación o revisión explícita.'),
 ('Interfaz y prueba integrada','Los contadores y la fecha salen de consultas observables. Se creó un producto en modo avión con ID -2, precio 6.75 y stock 12: al reconectar obtuvo ID 123 y el contador bajó a cero. Se corrigió otra fila sin duplicarla y se verificó persistencia al reabrir sin internet.')]:
 text(h,'sub');text(b)
newpage('Pruebas y trazabilidad')
rs=[ET.parse(p).getroot() for p in (ROOT/'shared/build/test-results/testAndroidHostTest').glob('TEST-*.xml')]
counts={k:sum(int(r.attrib.get(k,0)) for r in rs) for k in ('tests','failures','errors','skipped')}
text('Último resultado XML disponible: <b>{tests} pruebas; {failures} fallos, {errors} errores y {skipped} omitidas</b>.'.format(**counts))
text('Compilación comprobada: :shared:testAndroidHostTest y :androidApp:assembleDebug. APK: androidApp/build/outputs/apk/debug/androidApp-debug.apk. Los logs completos se conservan en evidencias/sesion13/pruebas/.')
text('Decisiones que explican el comportamiento','sub')
text('La pantalla observa la base local para funcionar sin internet. Primero se envían las creaciones y después se descargan todas las páginas. La bajada conserva las filas pendientes y fallidas. Solo se actualiza la fecha cuando termina una sincronización completa sin filas sin resolver.')
text('El índice previo de fallos produce esperas de 1, 2, 4, 8 y 16 segundos. Como se detiene al quinto fallo, no espera 16 segundos después del último: las esperas efectivas máximas son 1+2+4+8 segundos. Las pruebas usan tiempo virtual.')
text('PharmaSoft no ofrece claves de idempotencia. Antes de transmitir se persiste un bloqueo de resultado incierto. Si se pierde la respuesta o muere el proceso, se conserva para revisar. El usuario puede conciliar un ID existente o confirmar que revisó el servidor antes de habilitar otro envío.')
text('Commits publicados hasta esta generación','sub')
log=subprocess.check_output(['git','log','--reverse','--format=%h %s','develop..HEAD'],cwd=ROOT,text=True,encoding='utf8')
for l in log.splitlines():text(escape(l),'small')
text('Documentación técnica: docs/sesion13/. Configuración Firebase: google-services.json se mantiene local y fuera de GitHub por decisión del estudiante. No se incluyen JWT ni contraseñas en el informe.')
figuras=[
('01_primera_apertura.png','01 Primera apertura sin sesión','La instalación limpia abre el login. Con conexión no se muestra el catálogo sin autenticar.'),
('02_login_incorrecto.png','02 Credenciales incorrectas','Mensaje real de credenciales incorrectas; no se presenta como un error de conexión.'),
('03_inventario_vacio.png','03 Base vacía sin conexión','Estado vacío propio y ausencia explícita de una sincronización anterior.'),
('04_producto_pendiente.png','04 Creación en modo avión','Primer recorrido: el producto se guardó con ID provisional -1 y marca pendiente.'),
('05_producto_sincronizado.png','05 Registro confirmado','Al recuperar señal recibió el ID 103 y el inventario cambió desde la base observable.'),
('05b_postman_producto_103.jpg','06 Confirmación real en Postman','GET del producto creado desde Android. HTTP 200 e ID 103, con precio y stock conservados.'),
('06_precio_rechazado_servidor.png','07 Validación del servidor','Precio cero rechazado por PharmaSoft. El mensaje se muestra debajo de Precio.'),
('07a_borrador_antes_de_expirar.png','08 Formulario antes de vencer la sesión','JWT legítimo emitido con duración de un minuto mediante configuración temporal del backend. No se alteró su firma ni se registró el borrador.'),
('07b_sesion_expirada.png','09 Sesión vencida','Después de vencer el token aparece el aviso de expiración y se solicita iniciar sesión nuevamente.'),
('07c_formulario_tras_reautenticar.png','10 Borrador conservado','Los cuatro campos se conservaron al reautenticar la misma cuenta dentro de la misma Activity. No acredita recuperación tras muerte del proceso.'),
('08_cierre_sesion.png','11 Cierre de sesión','La app vuelve al login y el receptor debug confirmó token ausente. No se publica el contenido del token.'),
('08b_cache_sin_sesion_tras_logout.png','12 Hueco de acceso detectado','Antes de corregir: al reabrir sin internet tras logout todavía se veía el catálogo. Es evidencia del problema, no del resultado final.'),
('08c_logout_offline_bloqueado.png','13 Acceso corregido tras logout','Después de la corrección autorizada: al reabrir en modo avión se exige login. La base mantuvo doce filas y un pendiente.'),
('10_estado_sincronizacion_con_fallida.png','14 Estado global de sincronización','Interfaz roja con conexión, antigüedad y contadores. El rechazo de precio ya está separado como una fila que requiere atención.'),
('10b_contador_pendiente_sin_red.png','15 Pendiente y contador reales','Modo avión, Wi-Fi y datos desactivados. A00_S13_Offline_151900 queda con ID -2, precio 6.75, stock 12 e intento 1 sin transmitir POST.'),
('10c_contador_tras_sincronizar.png','16 Contador actualizado al reconectar','El producto obtiene ID 123 y desaparece de pendientes. La fallida previa permanece y la fecha todavía no avanza por ser sincronización parcial.'),
('10d_fila_fallida_visible.png','17 Fila fallida visible','El producto rechazado se conserva y muestra que requiere atención, con acceso a Ver qué pasó.'),
('10e_motivo_real_y_accion_corregir.png','18 Motivo persistido y acción','Detalle real: el precio debe ser mayor que cero. Ofrece revisar y corregir la misma fila.'),
('10f_correccion_guardada.png','19 Corrección sin duplicar','Se cambió el precio a 4.75 sobre el ID local -1. Se mantuvieron trece filas y se habilitó su envío.'),
('10g_sincronizacion_completa.png','20 Sincronización completa','Después de enviar la corrección quedaron cero pendientes y cero fallidas. La fecha se actualizó.'),
('10h_reapertura_sin_red_fecha_persistida.png','21 Persistencia al reabrir sin red','Reapertura real en modo avión: inventario y última fecha iguales a los guardados antes de cerrar. Captura con el ajuste gramatical final.')]
for f,t,c in figuras:evidence(f,t,c)
if (EVI/'figuras_adicionales.json').exists():
 for f in json.loads((EVI/'figuras_adicionales.json').read_text(encoding='utf8')):evidence(f['archivo'],f['titulo'],f['descripcion'],f.get('carpeta','capturas'))
newpage('Estado de cumplimiento y referencias')
text('Alcance verificado a la fecha de esta generación','sub')
for title,status in [
 ('1 Recorrido y tabla de huecos','Verificado, ocho momentos con evidencia real.'),
 ('2 Intentos y último error','Verificado en SQLDelight, migración instalada y tarea Gradle.'),
 ('3 Clasificación de errores','Verificado con pruebas de 400, 401, 403, 404, 409, 503 y transporte.'),
 ('4 Espera y máximo','Verificado con tiempo virtual y máximo cinco fallos persistidos.'),
 ('5 Fila agotada visible','Verificado en pruebas y UI de fila fallida con motivo y acciones.'),
 ('6 Subida antes de bajada','Verificado por orden de peticiones y conservación de filas.'),
 ('7 Contadores y antigüedad','Verificado en emulador, Flow y reapertura sin conexión.'),
 ('8 a 11 Permisos nativos','Verificado: '+estado.get('permisos_detalle','') if estado.get('permisos') else 'Pendiente de implementación y de las tres capturas reales del sistema.'),
 ('12 Firebase en capa de datos','Verificado: '+estado.get('firebase_detalle','') if estado.get('firebase') else 'Proyecto y app registrados, JSON local; SDK, no fatal y logcat todavía pendientes.'),
 ('13 Pruebas comunes','Última regresión: {tests} pruebas, {failures} fallos, {errors} errores, {skipped} omitidas.'.format(**counts)),
 ('14 Commits por paso','Pasos 1 a 4 y correcciones publicados; pasos restantes pendientes.' if not final else estado.get('git_detalle','Véase historial y auditoría final.'))]:
 text('<b>'+title+'</b> · '+status,'small')
text('Fuentes y archivos de soporte','sub')
text('Guía práctica de laboratorio N.º 13, 14 páginas; diapositivas de la sesión 13, 19 páginas; prompt complementario del estudiante. El detalle de una futura actividad autónoma no se inventa a partir de esta práctica.')
text('docs/sesion13/01_Tabla_Huecos_JorgeQuispe.md<br/>docs/sesion13/02_Esquema_Migracion.md<br/>docs/sesion13/03_Sincronizacion_Refinada.md<br/>docs/sesion13/04_Estados_Interfaz.md<br/>docs/sesion13/Matriz_Cumplimiento.md')
text('Limitaciones: iOS excluido y sin evidencia de dispositivo físico. No se afirma que Firebase haya recibido un evento en consola hasta observarlo realmente. El estudiante debe revisar el informe y explicar sus decisiones antes de entregarlo.')
C.save()
(EVI/'indice_capturas_pdf.json').write_text(json.dumps({'pdf':str(OUT.relative_to(ROOT)).replace('\\','/'),'paginas':len(PdfReader(OUT).pages),'generado':datetime.datetime.now().astimezone().isoformat(),'finalizado':final,'figuras':manifest},ensure_ascii=False,indent=2),encoding='utf8')
print(json.dumps({'pdf':str(OUT),'paginas':len(PdfReader(OUT).pages),'figuras':len(manifest),'bytes':OUT.stat().st_size,'finalizado':final},ensure_ascii=False))
