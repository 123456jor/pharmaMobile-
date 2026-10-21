from pathlib import Path
from xml.sax.saxutils import escape
import json
import subprocess
from reportlab.pdfgen import canvas
from reportlab.lib.pagesizes import A4, A3, landscape
from reportlab.lib.colors import HexColor, white
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import Paragraph, Table, TableStyle
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.utils import ImageReader
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'output/pdf/Entregable_Sesion11_OfflineFirst_Quispe.pdf'
OUT.parent.mkdir(parents=True, exist_ok=True)
CAP = ROOT / 'evidencias/capturas'
pdfmetrics.registerFont(TTFont('Arial', r'C:\Windows\Fonts\arial.ttf'))
pdfmetrics.registerFont(TTFont('Arial-Bold', r'C:\Windows\Fonts\arialbd.ttf'))
pdfmetrics.registerFontFamily('Arial', normal='Arial', bold='Arial-Bold')
INK = HexColor('#173D39')
TEAL = HexColor('#007B68')
MUTED = HexColor('#526862')
LIGHT = HexColor('#EAF5F0')
BODY = ParagraphStyle('body', fontName='Arial', fontSize=11, leading=16, textColor=INK)
SMALL = ParagraphStyle('small', parent=BODY, fontSize=9, leading=13)
CELL = ParagraphStyle('cell', parent=BODY, fontSize=10, leading=14)
c = canvas.Canvas(str(OUT), pagesize=A4)
c.setTitle('Sesión 11 - Persistencia local y estrategia offline-first')
c.setAuthor('Jorge Armando Quispe Huaman')
number = 0
TOTAL = 15


def para(text, x, y, width, style=BODY):
    p = Paragraph(text, style)
    _, height = p.wrap(width, 1000)
    p.drawOn(c, x, y - height)
    return y - height - 12


def page(title, section, wide=False):
    global number
    number += 1
    w, h = landscape(A3) if wide else A4
    c.setPageSize((w, h))
    c.setFillColor(TEAL)
    c.rect(0, h - 8, w, 8, fill=1, stroke=0)
    c.setFillColor(MUTED)
    c.setFont('Arial', 9)
    c.drawString(40, h - 35, 'PHARMAMOBIL  /  GUÍA PRÁCTICA 11')
    c.drawRightString(w - 40, h - 35, section)
    c.setFillColor(INK)
    c.setFont('Arial-Bold', 23 if not wide else 22)
    c.drawString(40, h - 72, title)
    c.setStrokeColor(HexColor('#D8E6E0'))
    c.line(40, 40, w - 40, 40)
    c.setFont('Arial', 8)
    c.setFillColor(MUTED)
    c.drawString(40, 25, 'Jorge Armando Quispe Huaman  |  21 de octubre de 2026')
    c.drawRightString(w - 40, 25, f'{number} / {TOTAL}')
    return w, h, h - 100


def finish():
    c.showPage()


def figure(title, section, filename, description, wide=False, note=None):
    w, h, y = page(title, section, wide)
    y = para(description, 40, y, w - 80)
    if note:
        y = para(note, 40, y, w - 80, SMALL)
    with Image.open(CAP / filename) as im:
        iw, ih = im.size
    available_h = y - 80
    scale = min((w - 80) / iw, available_h / ih)
    dw, dh = iw * scale, ih * scale
    c.drawImage(ImageReader(str(CAP / filename)), (w - dw) / 2, y - dh,
                width=dw, height=dh, preserveAspectRatio=True)
    c.setFont('Arial', 8)
    c.setFillColor(MUTED)
    c.drawString(40, 57, 'Original: evidencias/capturas/' + filename)
    finish()


w, h, y = page('Persistencia offline-first', 'ENTREGABLE')
y = para('Desarrollo de Aplicaciones Móviles<br/><b>Guía Práctica de Laboratorio N.º 11</b>', 40, y, w - 80)
y -= 12
y = para('<b>Universidad Peruana Unión</b><br/>EP Ingeniería de Sistemas · Ciclo VI · Semestre 2026-2', 40, y, w - 80)
y = para('<b>Estudiante:</b> Jorge Armando Quispe Huaman<br/><b>Fecha:</b> 21 de octubre de 2026<br/><b>Proyecto:</b> PharmaMobil (Android/KMP) + PharmaSoft (API REST)', 40, y, w - 80)
y -= 12
c.setFillColor(LIGHT)
c.roundRect(40, y - 118, w - 80, 118, 10, fill=1, stroke=0)
para('<b>Resultado demostrado</b><br/>Producto creado en modo avión: <b>A_Offline_S11_1531</b>.<br/>ID local <b>-1</b>, pendiente <b>1</b> → ID del servidor <b>63</b>, pendiente <b>0</b>.<br/>Postman: <b>200 OK</b> y una sola coincidencia del producto.', 56, y - 14, w - 112)
y -= 145
y = para('<b>Validación:</b> 76 pruebas aprobadas, 0 fallos; APK Android compilado. La app reabrió y listó con modo avión y con PharmaSoft detenido.', 40, y, w - 80)
y = para('<b>Alcance acordado:</b> Android. iOS excluido por indicación del usuario. Postman sustituye Swagger para las evidencias del servidor.', 40, y, w - 80)
y = para('<b>Repositorio:</b> <link href="https://github.com/123456jor/pharmaMobile-">github.com/123456jor/pharmaMobile-</link><br/><b>Rama local:</b> feature/offline-first-quispe<br/>Los commits de esta práctica están guardados localmente; su publicación requiere la confirmación final solicitada en el encargo.', 40, y, w - 80, SMALL)
finish()

w, h, y = page('Implementación realizada', 'ARQUITECTURA')
sections = [
    ('01 · Base local', 'SQLDelight 2.1.0 genera PharmaDb. Android usa AndroidSqliteDriver y crea pharma.db. Koin registra el driver, la base, ProductoLocal y ProductoRepositorioOffline.'),
    ('02 · Esquema conservando el CRUD', 'producto contiene id, nombre, precio, stock y pendiente. Se conservan estado, categoriaId y categoriaNombre para mantener categorías y bajas lógicas del proyecto existente.'),
    ('03 · Una fuente para la pantalla', 'ProductoLocal observa consultas SQLDelight mediante Flow. ProductoViewModel recoge ese Flow; no consulta manualmente la lista después de registrar, editar o eliminar.'),
    ('04 · Subir y después bajar', 'Se guardan primero las altas pendientes con ID negativo. La sincronización serializada envía POST, confirma la sustitución local por ID positivo y luego descarga todas las páginas del servidor. El reemplazo transaccional conserva los pendientes.'),
    ('05 · Sesión al reabrir sin red', 'Se recupera únicamente una sesión previamente autenticada, cifrada y vigente. Un 401 o el vencimiento impiden usar esa sesión. El primer login y la renovación necesitan conexión.'),
    ('06 · Acciones conservadas', 'Registrar y consultar funcionan sin red. Editar y eliminar se mantienen con conexión y solo para productos sincronizados, según la decisión expresa del usuario.'),
]
for title, text in sections:
    y = para('<b>' + title + '</b><br/>' + text, 40, y, w - 80)
y = para('Código, instrucciones de ejecución y adaptaciones: README.md, sección <b>Persistencia offline-first</b>. Verificación detallada de cada requisito: evidencias/checklist.md.', 40, y, w - 80, SMALL)
finish()

figure('Pruebas automatizadas y compilación', '76 APROBADAS', '09-pruebas-build-successful.jpg',
       '<b>76 pruebas, 0 fallos, 0 errores, 0 omitidas.</b> Ocho pruebas del repositorio offline y tres de sesión offline se suman a la suite previa. La misma ejecución ensambló el APK Android.', True,
       'Vista real del final de salida-pruebas.txt en Bloc de notas. Las respuestas 400/404 visibles corresponden a casos controlados de manejo de errores. El total se verificó con los resultados XML; se conserva también el reporte HTML de las 17 suites.')

figure('01 · Inventario con conexión', 'INICIO DEL CICLO', '01-con-red-sincronizado.png',
       'La app sincronizó y mostró cuatro productos activos. Se verificó el alta con red A_Prueba_con_red_S11_1431 con ID 43 y después se cerró la app por completo.')

figure('02 · Modo avión activado', 'SIN RED', '02-modo-avion-activo.png',
       'El emulador permite activar modo avión. Durante el ciclo también se desactivaron Wi-Fi y datos para asegurar la ausencia de conexión.',
       note='Esta captura de Ajustes se repitió después del ciclo porque la inicial quedó durante una animación. El modo avión del ciclo original está documentado también en modo-avion.txt y en la barra de las capturas 03 y 04.')

figure('03 · Reapertura sin conexión', 'LECTURA LOCAL', '03-listado-sin-red-android.png',
       'Tras cerrar y reabrir con modo avión, aparecieron los mismos cuatro productos. El aviso visible indica: Sin conexión: mostrando datos guardados.')

figure('04 · Registro sin conexión', 'PENDIENTE LOCAL', '04-registro-sin-red.png',
       'A_Offline_S11_1531 apareció en el inventario sin red. La tarjeta muestra Pendiente de sincronizar e ID -1. El listado pasó de cuatro a cinco productos.')

figure('05 · Fila pendiente en SQLite', 'ID -1 / PENDIENTE 1', '05-fila-pendiente.jpg',
       'Consulta real sobre una copia de pharma.db extraída de la app: SELECT * FROM producto WHERE pendiente=1. Se verificó precio 6.75, stock 12, categoría 1 e ID -1.', True,
       'La captura muestra el JSON de esa consulta en Bloc de notas. Es un visor del resultado SQLite guardado; no se presenta como Database Inspector ni como respuesta HTTP. Original verificable: evidencias/pruebas/fila-pendiente.json.')

figure('06 · Reconexión y sincronización', 'ID REAL 63', '06-reconexion-sincronizar.png',
       'Se desactivó modo avión, se recuperó la red y se pulsó Sincronizar. El producto conservó sus datos y quedó una sola vez con ID 63. SQLite confirmó pendiente=0 y cero pendientes totales.')

figure('07 · Producto confirmado en Postman', 'API REAL / 200 OK', '07-postman-producto-id-real.jpg',
       'GET /api/v1/productos/{{productoId}} devolvió 200 OK. El cuerpo del servidor muestra ID 63, nombre A_Offline_S11_1531, precio 6.75, stock 12 y categoría Medicamentos.', True,
       'Se utiliza Postman en lugar de Swagger por instrucción expresa del usuario. La variable productoId se obtuvo de la consulta del listado; el registro se creó desde Android en modo avión.')

figure('08 · Comprobaciones de Postman', 'TRES PASSED', '07b-postman-listado-pruebas.jpg',
       'El GET del listado aprobó tres comprobaciones: HTTP 200, Producto offline existe una sola vez e ID real positivo. Esto complementa la captura del cuerpo del producto.', True,
       'Colección reproducible: postman/S11-Offline-first.postman_collection.json. Orden: 01 Login, 02 Listar, 03 Buscar. El archivo exportado no contiene JWT.')

figure('09 · PharmaSoft apagado', 'REAPERTURA LOCAL', '08-backend-apagado-listado-android.png',
       'Con la red activa y PharmaSoft detenido, se cerró y reabrió la app. Mostró cinco productos guardados y el aviso de falta de conexión.',
       note='Se comprobó que el puerto 8080 no tenía un proceso escuchando. Después de la captura se reinició PharmaSoft: health respondió 200 a las 15:38, hora de Lima. Oracle/Docker se mantuvo activo.')

figure('10 · Archivo pharma.db creado', 'CONTROL DEL DRIVER', '10-archivo-pharma-db.jpg',
       'El control del driver confirmó que existe pharma.db dentro de databases de la aplicación Android. Se conserva la salida original de adb run-as ls -l databases.', True,
       'Captura auténtica del archivo de salida 02-pharma-db.txt abierto en Bloc de notas. Es una vista del resultado de ADB, no una terminal simulada ni Device Explorer.')

w, h, y = page('Lista de cotejo', 'RESULTADOS VERIFICADOS')
items = [
    ('1', 'Esquema SQLDelight compila', 'Generación de PharmaDb y compilación correctas.'),
    ('2', 'Driver registrado', 'Android y JDBC de pruebas verificados. iOS excluido.'),
    ('3', 'Capa local sin Ktor ni DTO', 'ProductoLocal trabaja con SQL y dominio.'),
    ('4', 'Contrato observar / sincronizar', 'Implementado, inyectado y probado.'),
    ('5', 'ViewModel con Flow', 'Sin recarga manual después de mutaciones.'),
    ('6', 'Lista con backend apagado', 'Reapertura y captura real con cinco productos.'),
    ('7', 'Pendiente marcado', 'ID -1 y pendiente=1 en UI y consulta SQLite.'),
    ('8', 'Sube antes de bajar', 'Prueba de orden POST antes de GET aprobada.'),
    ('9', 'Producto existe en PharmaSoft', 'Postman 200, ID 63, una sola coincidencia.'),
    ('10', 'Al menos tres pruebas', 'Ocho del repositorio; 76 totales aprobadas.'),
]
data = [[Paragraph('<b>N.º</b>', CELL), Paragraph('<b>Criterio</b>', CELL), Paragraph('<b>Resultado</b>', CELL)]]
data += [[Paragraph(escape(v), CELL) for v in row] for row in items]
t = Table(data, colWidths=[33, 180, w - 293], repeatRows=1)
t.setStyle(TableStyle([('BACKGROUND', (0, 0), (-1, 0), LIGHT), ('VALIGN', (0, 0), (-1, -1), 'TOP'),
                       ('LEFTPADDING', (0, 0), (-1, -1), 8), ('RIGHTPADDING', (0, 0), (-1, -1), 8),
                       ('TOPPADDING', (0, 0), (-1, -1), 9), ('BOTTOMPADDING', (0, 0), (-1, -1), 9),
                       ('LINEBELOW', (0, 0), (-1, -1), .4, HexColor('#D8E6E0'))]))
_, th = t.wrap(w - 80, h)
t.drawOn(c, 40, y - th)
y -= th + 22
y = para('El detalle de E1-E3, P1-P6, T1-T4 y D1-D6, con archivos, verificaciones y evidencias, está en <b>evidencias/checklist.md</b>. Los puntos de iOS no se reclaman completados.', 40, y, w - 80, SMALL)
finish()

w, h, y = page('Adaptaciones y trazabilidad', 'CIERRE')
adaptations = [
    '<b>Rama:</b> develop no existe. Se creó feature/offline-first-quispe desde la sesión 10 con autorización del usuario.',
    '<b>D-1:</b> JDBC se ubica en androidHostTest mediante una fábrica expect/actual. La lógica de las pruebas está en commonTest, con una base nueva por caso.',
    '<b>D-2:</b> el algoritmo literal dejó dos filas. Se conservó la prueba fallida y se corrigió con borrarPorId y confirmación transaccional solo después del POST exitoso.',
    '<b>D-3 y D-4:</b> alias ProductoFila para el modelo generado; edición y eliminación online conservadas por decisión expresa.',
    '<b>Alcance:</b> Android, sin implementación ni pruebas iOS. Postman sustituye Swagger por indicación expresa.',
]
for text in adaptations:
    y = para(text, 40, y, w - 80, SMALL)
y = para('<b>Commits realizados durante la implementación</b>', 40, y - 5, w - 80)
commits = subprocess.check_output(['git', 'log', '--reverse', '--format=%h %s', 'ef38718..HEAD'], cwd=ROOT, text=True, encoding='utf-8').splitlines()
for line in commits:
    y = para(escape(line), 40, y, w - 80, SMALL)
y = para('La documentación, las capturas originales, el reporte de pruebas y este PDF se guardan en el proyecto. El README incluye cómo reproducir el ciclo y la limitación de idempotencia ante un POST confirmado en el servidor cuya respuesta se pierda.', 40, y - 5, w - 80, SMALL)
finish()

assert number == TOTAL, (number, TOTAL)
c.save()
print(OUT)
