"""PDF de la actividad, construido a partir de evidencia real guardada en docs/s11."""
from pathlib import Path
from html import escape
import json
import re
import textwrap
from reportlab.platypus import (SimpleDocTemplate, Paragraph, Spacer, PageBreak,
    Image, Table, TableStyle, Preformatted, KeepTogether)
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.lib.utils import ImageReader

ROOT=Path(__file__).resolve().parents[2]
D=ROOT/'docs/s11'
OUT=ROOT/'output/pdf/S11_ActividadAutonoma_Quispe_Huaman.pdf'
URL='https://github.com/123456jor/pharmaMobile-/tree/feature/s11-autonoma-quispe'
pdfmetrics.registerFont(TTFont('Arial',r'C:\Windows\Fonts\arial.ttf'))
pdfmetrics.registerFont(TTFont('ArialBold',r'C:\Windows\Fonts\arialbd.ttf'))
pdfmetrics.registerFont(TTFont('Mono',r'C:\Windows\Fonts\consola.ttf'))
pdfmetrics.registerFontFamily('Arial',normal='Arial',bold='ArialBold',italic='Arial',boldItalic='ArialBold')
styles=getSampleStyleSheet()
styles.add(ParagraphStyle(name='Cuerpo',fontName='Arial',fontSize=9.5,leading=13,spaceAfter=8))
styles.add(ParagraphStyle(name='Sub',fontName='ArialBold',fontSize=12,leading=16,spaceBefore=10,spaceAfter=8,keepWithNext=True,textColor=colors.HexColor('#006b55')))
styles.add(ParagraphStyle(name='Titulo',fontName='ArialBold',fontSize=22,leading=28,spaceAfter=20,textColor=colors.HexColor('#006b55')))
styles.add(ParagraphStyle(name='Pequeno',fontName='Arial',fontSize=8.2,leading=11,spaceAfter=6))
styles.add(ParagraphStyle(name='Codigo',fontName='Mono',fontSize=6.7,leading=8.5,spaceAfter=7))
styles.add(ParagraphStyle(name='Portada',fontName='ArialBold',fontSize=26,leading=34,alignment=TA_CENTER,spaceAfter=20,textColor=colors.HexColor('#006b55')))
story=[]
def p(t,style='Cuerpo'):
    return Paragraph(escape(str(t)).replace('\n','<br/>'),styles[style])
def add(t,style='Cuerpo'):story.append(p(t,style))
def title(t):add(t,'Titulo')
def sub(t):add(t,'Sub')
def page():story.append(PageBreak())
def code(t):
    # Se conservan todos los caracteres; solo se ajustan los renglones al ancho del papel.
    wrapped=[]
    for line in t.expandtabs(4).splitlines():
        wrapped.extend(textwrap.wrap(line, width=121,replace_whitespace=False,
            drop_whitespace=False,break_long_words=True,break_on_hyphens=False) or [''])
    story.append(Preformatted('\n'.join(wrapped),styles['Codigo']))
def img(path,maxw=480,maxh=510):
    path=Path(path); w,h=ImageReader(str(path)).getSize(); scale=min(maxw/w,maxh/h)
    return Image(str(path),w*scale,h*scale)
def markdown(path):
    for block in Path(path).read_text(encoding='utf8').split('\n\n'):
        block=block.strip()
        if not block:continue
        if block.startswith('# '):sub(block[2:])
        elif block.startswith('## '):sub(block[3:])
        elif block.startswith('### '):sub(block[4:])
        else:
            block=re.sub(r'\*\*|`','',block)
            add(block)

story.append(Spacer(1,55))
add('UNIVERSIDAD PERUANA UNIÓN','Portada')
add('Desarrollo de Aplicaciones Móviles\nActividad Autónoma N.º 11','Portada')
add('Pruebas unitarias y evidencia de escenarios offline','Sub')
add('Estudiante: Jorge Armando Quispe Huaman')
add('Unidad 2 - sesión 5 (sesión 11 del curso)')
add('Fecha de ejecución y documento: 23 de octubre de 2026, America/Lima')
add('Proyecto: PharmaMobil sobre PharmaSoft. Plataforma ejecutada: Android.')
add('Rama: feature/s11-autonoma-quispe')
story.append(Paragraph(f'<link href="{URL}" color="#006b55">{URL}</link>',styles['Cuerpo']))
story.append(Spacer(1,18))
add('Versión de entrega: rama publicada en GitHub con autorización del estudiante. Enlace comprobado: HTTP 200 y commit publicado visible.','Sub')
add('Alcance acordado: Android y Postman. No se ejecutó iOS ni Swagger. Se declaran esas diferencias frente a la guía en la lista de cotejo.')
page()
title('Resumen verificable')
add('Seis pruebas obligatorias aprobadas en commonTest. Suite completa: 94 pruebas, cero fallos, cero errores y cero omitidas. Compilación del APK debug exitosa. No se necesita red ni emulador para ejecutar los seis casos con MockEngine y SQLite en memoria.')
add('Cinco escenarios ejecutados en Pixel 8 Pro. El producto AA_Autonoma_Jorge_23Oct pasó de ID local -1 y pendiente=1 a ID 83 y pendiente=0; Postman confirmó el recurso con HTTP 200. La base mantiene diez filas, seis activas y cero pendientes al terminar.')
add('Antigüedad: marca persistida tras sincronización completa, texto natural, caso nunca sincronizado y migración real 1 a 2 que conservó los nueve productos anteriores. La evidencia de un día utiliza una edición de depuración declarada de 26 horas; no transcurrió ese tiempo real.')
add('Expiración: JWT genuino del backend con duración temporal de un minuto. Se comprobó exp < ahora antes de abrir. La consulta local conserva datos, borra credenciales vencidas y bloquea cambios; al volver la conexión se pide login. La duración normal de 3600 segundos fue restaurada.')
sub('Límites y acciones personales')
add('Postman reemplaza Swagger por instrucción del usuario, por lo que el punto literal de Swagger se marca No. No hay ejecución iOS; el tramo multiplataforma del guion queda parcial. El ensayo en voz alta, comprensión del código, revisión de conclusiones, entrega al aula y sustentación corresponden al estudiante. La publicación fue autorizada y el enlace de GitHub fue verificado.')
sub('Entorno')
add('Windows 11; Microsoft JDK 21.0.12.1; Gradle 9.1.0; Kotlin 2.4.10; AGP 9.0.1; SQLDelight 2.1.0; Ktor 3.6.0; coroutines 1.10.2. Emulador API 37.1. Android llama a 10.0.2.2:8080; Postman a localhost:8080. Oracle corre en el contenedor pharmasoft-oracle.')
add('Detalles reproducibles: docs/s11/entorno.md, mapa-codigo.md, matriz.md, consultar_base.py y consultas/. Las horas de bitácora son Lima; el emulador está en GMT y muestra la hora en formato de 12 horas.')
page()
title('Producto 1. Seis pruebas comunes')
descs=[
('2. Registro sin red','El motor lanza IOException. Se exige ID negativo, fila pendiente, emisión del Flow y coincidencia con la consulta de pendientes.'),
('4. La bajada conserva pendientes','Se aísla reemplazarSincronizados porque un POST fallido detiene la sincronización completa antes del GET. La lista remota omite el pendiente: deben existir ambas filas y conservarse intacta la local.'),
('1. Sincronización con red','Dos productos remotos, base nueva: el Flow y SQLite deben contener la lista completa, sin pendientes.'),
('3. Envío al reconectar','La misma base crea sin red y sincroniza con red. Se cuenta exactamente un POST aceptado; el ID confirmado es positivo y desaparece la marca pendiente.'),
('5. Identificadores sin colisión','Se comprueban cantidad, nombres y datos de un producto existente del servidor y uno creado localmente tras recibir su ID definitivo.'),
('6. Segunda sincronización','Se comparan listas completas tras dos sincronizaciones y se exige un único POST en total. El segundo ciclo no duplica el registro.')]
for h,t in descs:sub(h);add(t)
add('Orden real de escritura: primero 2 y 4 en f49b9cd; después 1, 3, 5 y 6 en d33a5b1. Cada prueba llama conBaseNueva, crea su driver y cierra recursos. La lógica está en commonTest y el actual JDBC en androidHostTest; iOS no fue objeto de esta ejecución.')
page();title('Sensibilidad y correcciones')
add('Se cambió temporalmente guardarPendiente a pendiente=false. Las pruebas 2 y 4 fallaron con aserciones; se restauró el archivo automáticamente en finally y se verificó que no quedara diferencia. Nunca se confirmó ese defecto. La suite correcta volvió a pasar con 94 casos.')
add('La primera prueba nueva de fecha falló por una respuesta simulada que escribía tamano en lugar del campo real tamanio. Se corrigió el JSON del test sin debilitar aserciones; se conservan salida inicial y salida corregida. No fue necesario alterar el contrato de producción.')
sub('Commits de la rama publicada')
code((D/'commits.txt').read_text(encoding='utf8'))
add('fc3067e agrega antigüedad y migración; de29d57 corrige consulta con sesión vencida y primera apertura sin red; c052b69 incorpora comprobaciones adicionales, reporte y reevaluación de sesión cuando vence con la app abierta. Los commits de implementación y documentación están disponibles en el enlace de la rama. f9fdf1d refuerza el bloqueo inmediato al vencer la sesión y elimina la sesión anterior ante un 401.')
sub('Resultado de sensibilidad (fragmento, salida completa en repositorio)')
sens=(D/'sensibilidad-fallo-esperado.txt').read_text(encoding='utf-8-sig')
code('\n'.join(l for l in sens.splitlines() if 'FAILED' in l or 'AssertionError' in l or 'tests completed' in l))
page();title('Código de las seis pruebas')
add('Código real de ProductoRepositorioOfflineAutonomaTest.kt. La explicación para estudiar cada caso está al final de este documento y en docs/s11/guia-estudio.md.')
source=ROOT/'shared/src/commonTest/kotlin/pe/edu/upeu/pharmamobil/data/repository/ProductoRepositorioOfflineAutonomaTest.kt'
code('\n'.join(f'{i:3} {l}' for i,l in enumerate(source.read_text(encoding='utf-8-sig').splitlines(),1)))
page();title('Salida completa de las pruebas')
add('Transcripción íntegra de docs/s11/salida-pruebas.txt. Comando: gradlew.bat :shared:testAndroidHostTest :androidApp:assembleDebug --console=plain. La advertencia de iOS corresponde a Windows; no se afirma una ejecución iOS. Los renglones largos se ajustan al papel sin omitir contenido.')
code((D/'salida-pruebas.txt').read_text(encoding='utf-8-sig'))
page();title('Producto 2. Bitácora offline')
add('Cada registro contiene los ocho campos solicitados. Las consultas son copias reales del SQLite del APK debug, leídas con run-as y sqlite3. Las filas inactivas se conservan en la base, mientras la pantalla muestra las activas. Las conclusiones se redactan a partir de lo observado y quedan para revisión personal del estudiante.')
for numero,r in enumerate(json.loads((D/'bitacora.json').read_text(encoding='utf8'))):
    if numero: page()
    title(r['Identificador y nombre'])
    textos=[p(f'{i}. {k}: {v}','Pequeno') for i,(k,v) in enumerate(r.items(),1)]
    tabla=Table([[textos,img(D/r['Captura'],maxw=205,maxh=470)]],colWidths=[290,210])
    tabla.setStyle(TableStyle([('VALIGN',(0,0),(-1,-1),'TOP'),('LEFTPADDING',(0,0),(-1,-1),0),('RIGHTPADDING',(0,0),(-1,-1),7)]))
    story.append(tabla)
page();title('E3. Confirmación desde Postman')
add('GET http://localhost:8080/api/v1/productos/83. Respuesta real 200 OK. Este cliente consulta el backend; ver el producto solo en Android no bastaría para comprobar el envío. Postman se emplea por solicitud expresa del usuario, en sustitución de Swagger.')
story.append(img(D/'capturas/esc3_postman_producto_83.jpg',maxw=500,maxh=360))
sub('Datos devueltos por el servidor')
real=json.loads((D/'comprobacion-defensa.json').read_text(encoding='utf8'))
code(json.dumps(real['producto'],ensure_ascii=False,indent=2))
add('La colección entregada no incluye token ni contraseña: se configura claveAdmin durante la sesión y se ejecuta el login para obtener JWT. La captura muestra el producto, no las credenciales.')
page();title('E5. Argumento y reconexión')
add('El login sin internet impedía consultar datos ya guardados. La versión anterior se documentó con captura, sin atribuir a ese token una expiración no medida. En la prueba controlada final se emitió un JWT real con duración de un minuto y se comprobó su vencimiento antes de reabrir; no se alteró la firma.')
code((D/'expiracion-emision.txt').read_text(encoding='utf-8-sig')+'\n'+(D/'expiracion-antes-reabrir.txt').read_text(encoding='utf-8-sig')+'\n'+(D/'expiracion-despues-reabrir.txt').read_text(encoding='utf-8-sig'))
add('El primer intento abrió antes del vencimiento y no se usa como evidencia final. Se repitió después de verificar exp < ahora. La consulta conserva diez filas, seis activas, sin conceder sesión ni escritura. Al reconectar la app exige iniciar sesión. Consultar una copia local con su antigüedad visible es más útil que exigir un login imposible sin red; no equivale a renovar la autorización.')
story.append(img(D/'capturas/esc5_login_al_reconectar_verificado.png',maxw=180,maxh=360))
page();title('Producto 3. Antigüedad')
add('Sincronizacion contiene una sola fila, id=1 y ultimaMillis. ProductoRepositorioOffline guarda la fecha únicamente después de subir todos los pendientes y descargar todas las páginas. La lista y la marca se confirman en una transacción. Un fallo conserva la fecha anterior. El reloj se puede inyectar para probar sin esperas.')
add('La migración 1.sqm pasó la instalación existente de esquema 1 a 2 sin alterar ninguno de sus nueve productos. Ver consultas/migracion-verificada.json. No se borró la base para comprobar la migración.')
sub('Función pura de presentación')
code((ROOT/'shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobil/domain/model/Antiguedad.kt').read_text(encoding='utf8'))
add('Se probaron nulo, segundos, reloj atrasado, minutos, horas y días. La marca se observa por Flow y la interfaz recalcula el texto cada minuto, sin peticiones HTTP adicionales. Sin marca aparece «Aún no se ha sincronizado»; véase E4.')
page();title('Antigüedad reciente y controlada')
add('Izquierda: sincronización real reciente. Derecha: marca modificada SOLO en el APK debug a hace 26 horas mediante DEBUG_ANTIGUEDAD_26_HORAS. No se esperó un día real, no se insertaron filas de productos y no se cambió el reloj del emulador. Después se restauró la marca mediante una sincronización exitosa; consta en consultas/antes_borrar_datos.json y final_restaurado.json.')
t=Table([[img(D/'capturas/antiguedad_reciente.png',maxw=235,maxh=520),img(D/'capturas/antiguedad_26_horas_controlada.png',maxw=235,maxh=520)]],colWidths=[250,250]);t.setStyle(TableStyle([('VALIGN',(0,0),(-1,-1),'TOP')]))
story.append(t)
page();title('Producto 4. Guion de ocho minutos')
markdown(D/'guion-sustentacion.md')
page();title('Comprobaciones para la sustentación')
add('Ejecución real del 23/10/2026: login admin 200; login vendedor 200; clave incorrecta 401; PUT de vendedor al producto 83, 403; POST inválido de admin, 400. No se aceptó un 401 como prueba de permisos ni se modificó el producto al recibir 403. El backend se restauró a JWT de 3600 segundos. Archivo: docs/s11/comprobacion-defensa.json.')
add('En Android se pulsó Registrar con el formulario vacío. Los mensajes se muestran bajo sus campos y no se insertó ningún registro inválido. Esta captura complementa el tramo de manejo de estados; no sustituye el ensayo personal con cronómetro.')
story.append(img(D/'capturas/validacion_bajo_campos.png',maxw=235,maxh=510))
page();title('Lista de cotejo y alcance real')
for n,c,estado in json.loads((D/'lista-cotejo.json').read_text(encoding='utf8')):
    add(f'{n}. {c}: {estado}.')
sub('Revisión de la rúbrica')
add('Suite (7 pts): nivel logrado por las seis pruebas y salida completa. Bitácora (6 pts): cinco escenarios con datos reales; la sustitución de Swagger por Postman puede afectar el requisito literal. Antigüedad (3 pts): implementada y comprobada. Guion (2 pts): cinco tramos preparados; evidencia iOS pendiente. Análisis y repositorio (2 pts): conclusiones específicas y commits publicados; acceso remoto verificado. La nota definitiva corresponde al docente; no se afirma 20/20 ni 100 % de cumplimiento.')
add('Pendiente de Jorge: revisar conclusiones, estudiar el código, ensayar con cronómetro, subir PDF y enlace al aula antes del lunes 26/10/2026 a las 23:59 y sustentar el martes 27/10. El ensayo y la entrega no se simulan.')
page();title('Explicación para estudiar')
markdown(D/'guia-estudio.md')

def pie(c,doc):
    c.setStrokeColor(colors.HexColor('#b6d9cf'));c.line(48,39,547,39)
    c.setFont('Arial',7);c.setFillColor(colors.HexColor('#45645c'))
    c.drawString(48,27,'Jorge Armando Quispe Huaman | Actividad Autónoma 11 | 23/10/2026')
    c.drawRightString(547,27,str(doc.page))
OUT.parent.mkdir(parents=True,exist_ok=True)
doc=SimpleDocTemplate(str(OUT),pagesize=(595.276,841.89),rightMargin=47.6,leftMargin=47.6,
    topMargin=45,bottomMargin=52,title='Actividad Autónoma 11 - Pruebas offline',
    author='Jorge Armando Quispe Huaman',subject='PharmaMobil: pruebas, bitácora, antigüedad y sustentación')
doc.build(story,onFirstPage=pie,onLaterPages=pie)
print(OUT)
