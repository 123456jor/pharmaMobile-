import json
import argparse
from pathlib import Path

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_LEFT
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import cm
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import (
    BaseDocTemplate,
    Frame,
    Image,
    KeepTogether,
    PageBreak,
    PageTemplate,
    Paragraph,
    Spacer,
    Table,
    TableStyle,
)

ROOT = Path(__file__).resolve().parent
parser = argparse.ArgumentParser(description='Genera el entregable de la actividad autónoma S10 a partir de evidencias reales.')
parser.add_argument('--evid', type=Path, default=ROOT / 'evidencias_autonoma')
args = parser.parse_args()
EVID = args.evid.resolve()
OUT = ROOT / "output" / "pdf" / "S10_ActividadAutonoma_Quispe.pdf"
OUT.parent.mkdir(parents=True, exist_ok=True)

datos = json.loads((EVID / "datos.json").read_text(encoding="utf-8"))
resultados = json.loads((EVID / "resultados.json").read_text(encoding="utf-8"))

font_dir = Path(r"C:\Windows\Fonts")
pdfmetrics.registerFont(TTFont("Arial", str(font_dir / "arial.ttf")))
pdfmetrics.registerFont(TTFont("Arial-Bold", str(font_dir / "arialbd.ttf")))

GREEN = colors.HexColor("#007F5F")
MINT = colors.HexColor("#D9F5EA")
DARK = colors.HexColor("#17221F")
GRAY = colors.HexColor("#F1F4F2")
MID = colors.HexColor("#65726D")
AMBER = colors.HexColor("#FFF1C7")
RED = colors.HexColor("#B42318")
WHITE = colors.white

styles = getSampleStyleSheet()
styles.add(ParagraphStyle(name="BodyA", fontName="Arial", fontSize=9.2, leading=12.2, textColor=DARK, spaceAfter=5))
styles.add(ParagraphStyle(name="SmallA", fontName="Arial", fontSize=7.6, leading=9.5, textColor=MID))
styles.add(ParagraphStyle(name="TitleA", fontName="Arial-Bold", fontSize=25, leading=29, textColor=GREEN, spaceAfter=12))
styles.add(ParagraphStyle(name="H1A", fontName="Arial-Bold", fontSize=16, leading=20, textColor=GREEN, spaceAfter=9))
styles.add(ParagraphStyle(name="H2A", fontName="Arial-Bold", fontSize=11.5, leading=14, textColor=DARK, spaceBefore=4, spaceAfter=5))
styles.add(ParagraphStyle(name="CenterA", fontName="Arial", fontSize=9, leading=12, alignment=TA_CENTER, textColor=DARK))
styles.add(ParagraphStyle(name="CaptionA", fontName="Arial", fontSize=7.4, leading=9, alignment=TA_CENTER, textColor=MID))
styles.add(ParagraphStyle(name="WhiteA", fontName="Arial-Bold", fontSize=9, leading=11, textColor=WHITE))
styles.add(ParagraphStyle(name="CodeA", fontName="Courier", fontSize=7.4, leading=9.2, textColor=DARK))


def p(text, style="BodyA"):
    safe = str(text).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    return Paragraph(safe, styles[style])


def header_footer(canvas, doc):
    canvas.saveState()
    canvas.setFillColor(GREEN)
    canvas.rect(0, A4[1] - 0.55 * cm, A4[0], 0.55 * cm, fill=1, stroke=0)
    canvas.setFont("Arial", 7.5)
    canvas.setFillColor(MID)
    canvas.drawString(1.4 * cm, 0.65 * cm, "PharmaMobil · Sesión 10 · Actividad autónoma")
    canvas.drawRightString(A4[0] - 1.4 * cm, 0.65 * cm, f"Página {doc.page}")
    canvas.restoreState()


doc = BaseDocTemplate(
    str(OUT),
    pagesize=A4,
    rightMargin=1.4 * cm,
    leftMargin=1.4 * cm,
    topMargin=1.35 * cm,
    bottomMargin=1.25 * cm,
    title="Sesión 10 - Actividad Autónoma - Quispe",
    author="Jorge Armando Quispe Huamán",
)
frame = Frame(doc.leftMargin, doc.bottomMargin, doc.width, doc.height, id="normal")
doc.addPageTemplates(PageTemplate(id="main", frames=frame, onPage=header_footer))


def image_fit(path, max_w, max_h):
    path = Path(path)
    if not path.exists():
        return Table([[p("[Captura visual pendiente]", "CenterA")]], colWidths=[max_w], rowHeights=[max_h], style=TableStyle([
            ("BACKGROUND", (0, 0), (-1, -1), AMBER),
            ("BOX", (0, 0), (-1, -1), 0.7, colors.HexColor("#D6A300")),
            ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
        ]))
    img = Image(str(path))
    scale = min(max_w / img.imageWidth, max_h / img.imageHeight)
    img.drawWidth = img.imageWidth * scale
    img.drawHeight = img.imageHeight * scale
    return img


def callout(title, body, color=GRAY, width=None):
    return Table(
        [[p(title, "H2A")], [p(body, "BodyA")]],
        colWidths=[width or doc.width],
        style=TableStyle([
            ("BACKGROUND", (0, 0), (-1, -1), color),
            ("BOX", (0, 0), (-1, -1), 0.5, colors.HexColor("#CBD5D1")),
            ("LEFTPADDING", (0, 0), (-1, -1), 10),
            ("RIGHTPADDING", (0, 0), (-1, -1), 10),
            ("TOPPADDING", (0, 0), (-1, -1), 7),
            ("BOTTOMPADDING", (0, 0), (-1, -1), 7),
        ]),
    )


def server_record(path):
    lines = Path(path).read_text(encoding="utf-8").splitlines()
    rows = [[p(line, "CodeA")] for line in lines]
    return Table(rows, colWidths=[10.2 * cm], style=TableStyle([
        ("BACKGROUND", (0, 0), (-1, -1), colors.HexColor("#F6F8F7")),
        ("BOX", (0, 0), (-1, -1), 0.6, colors.HexColor("#C7D2CE")),
        ("LEFTPADDING", (0, 0), (-1, -1), 8),
        ("RIGHTPADDING", (0, 0), (-1, -1), 8),
        ("TOPPADDING", (0, 0), (-1, -1), 3),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 3),
    ]))


story = []

# Portada
story += [Spacer(1, 2.1 * cm), p("UNIVERSIDAD PERUANA UNIÓN", "H2A"), Spacer(1, 0.25 * cm)]
story.append(p("Actividad Autónoma N.º 10", "TitleA"))
story.append(p("Expiración de sesión JWT, almacenamiento seguro y autorización por permisos", "H1A"))
story.append(Spacer(1, 1.1 * cm))
cover_rows = [
    [p("Estudiante", "H2A"), p(datos["integrantes"][0])],
    [p("Proyecto", "H2A"), p("PharmaMobil - cliente Kotlin Multiplatform")],
    [p("Rama", "H2A"), p(datos["rama"])],
    [p("Enlace a la rama", "H2A"), p(datos["url_rama"])],
    [p("Fecha", "H2A"), p(datos["fecha"])],
    [p("Entorno", "H2A"), p(datos["entorno"])],
]
story.append(Table(cover_rows, colWidths=[3.4 * cm, 13.7 * cm], style=TableStyle([
    ("BACKGROUND", (0, 0), (0, -1), MINT),
    ("BACKGROUND", (1, 0), (1, -1), GRAY),
    ("BOX", (0, 0), (-1, -1), 0.6, colors.HexColor("#C7D2CE")),
    ("INNERGRID", (0, 0), (-1, -1), 0.3, colors.HexColor("#D9E1DE")),
    ("VALIGN", (0, 0), (-1, -1), "TOP"),
    ("LEFTPADDING", (0, 0), (-1, -1), 9),
    ("RIGHTPADDING", (0, 0), (-1, -1), 9),
    ("TOPPADDING", (0, 0), (-1, -1), 8),
    ("BOTTOMPADDING", (0, 0), (-1, -1), 8),
])))
story.append(Spacer(1, 1.2 * cm))
story.append(callout("Alcance", "Pruebas reales en Android y backend local PharmaSoft. iOS no fue evaluado por indicación del usuario y por no disponer de macOS/Xcode.", MINT))
story.append(PageBreak())

# Resumen y restauración
story.append(p("1. Resumen de ejecución", "H1A"))
story.append(p("Se ejecutaron seis escenarios de autenticación, tres verificaciones de almacenamiento, cuatro efectos de cierre de sesión y una matriz de permisos de 18 celdas. Se incluyen las seis capturas reales de Postman y las capturas de Android obtenidas con adb. Las salidas reales del archivo cifrado y Logcat se capturaron abiertas en Bloc de notas, sin alterar el contenido."))
story.append(callout("Hallazgo principal", "El evento de sesión podía perderse por usar tryEmit. El cambio a emit garantiza que un 401 por expiración o invalidez llegue al observador y produzca cierre de sesión y navegación al login.", AMBER))
story.append(Spacer(1, 0.35 * cm))
story.append(p("Control de restauración", "H2A"))
rest = resultados["restauracion"]
rest_rows = [
    [p("Comprobación", "WhiteA"), p("Resultado", "WhiteA")],
    [p("Configuración JWT"), p(rest["config"])],
    [p("Usuarios"), p(", ".join(rest["usuarios"]))],
    [p("Login posterior"), p(f"HTTP {rest['login_status']} · {rest['expiraEnSegundos']} segundos")],
    [p("Fecha de control"), p(rest["verificado"])],
]
story.append(Table(rest_rows, colWidths=[5.2 * cm, 11.9 * cm], style=TableStyle([
    ("BACKGROUND", (0, 0), (-1, 0), GREEN),
    ("BACKGROUND", (0, 1), (-1, -1), GRAY),
    ("BOX", (0, 0), (-1, -1), 0.6, colors.HexColor("#C7D2CE")),
    ("INNERGRID", (0, 0), (-1, -1), 0.3, colors.HexColor("#D9E1DE")),
    ("VALIGN", (0, 0), (-1, -1), "TOP"),
    ("LEFTPADDING", (0, 0), (-1, -1), 8),
    ("RIGHTPADDING", (0, 0), (-1, -1), 8),
    ("TOPPADDING", (0, 0), (-1, -1), 7),
    ("BOTTOMPADDING", (0, 0), (-1, -1), 7),
])))
story.append(Spacer(1, 0.45 * cm))
story.append(p("Método y trazabilidad", "H2A"))
for item in [
    "Backend Spring Boot en http://localhost:8080 y Oracle Free en Docker.",
    "Android usó http://10.0.2.2:8080 desde Pixel 8 Pro.",
    "Los escenarios 2 y 6 alteraron temporalmente configuración/estado y fueron restaurados.",
    "Cada escenario incluye la reacción de Android y la respuesta real de Postman. Las repeticiones para capturar el servidor se registran con su propia fecha y hora.",
]:
    story.append(p("• " + item))

# Escenarios
for idx, item in enumerate(resultados["bitacora"], start=1):
    story.append(PageBreak())
    story.append(p(f"2.{idx} {item['id']} · {item['nombre']}", "H1A"))
    details = [
        [p("Fecha y hora", "H2A"), p(item["fecha_hora"])],
        [p("Plataforma", "H2A"), p(item["plataforma"])],
        [p("Pasos", "H2A"), p(item["pasos"])],
        [p("HTTP", "H2A"), p(item["http"])],
        [p("message literal", "H2A"), p(item["mensaje"])],
        [p("Qué vio el usuario", "H2A"), p(item["vio_usuario"])],
    ]
    if "detecta_y_lleva_login" in item:
        details.append([p("Detección", "H2A"), p(item["detecta_y_lleva_login"])])
    left = [Table(details, colWidths=[3.2 * cm, 7.0 * cm], style=TableStyle([
        ("BACKGROUND", (0, 0), (0, -1), MINT),
        ("BACKGROUND", (1, 0), (1, -1), GRAY),
        ("BOX", (0, 0), (-1, -1), 0.5, colors.HexColor("#C7D2CE")),
        ("INNERGRID", (0, 0), (-1, -1), 0.25, colors.HexColor("#D9E1DE")),
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("LEFTPADDING", (0, 0), (-1, -1), 6),
        ("RIGHTPADDING", (0, 0), (-1, -1), 6),
        ("TOPPADDING", (0, 0), (-1, -1), 5),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 5),
    ])), Spacer(1, 0.18 * cm), server_record(EVID / f"A_esc{idx}_{['valido','expirado','alterado','sin_cabecera','prefijo','deshabilitado'][idx-1]}_servidor.txt")]
    screenshot = EVID / f"A_esc{idx}_{['valido','expirado','alterado','sin_cabecera','prefijo','deshabilitado'][idx-1]}_pantalla.png"
    right = [image_fit(screenshot, 5.5 * cm, 12.2 * cm), Spacer(1, 0.1 * cm), p("Captura real del emulador Android", "CaptionA")]
    story.append(Table([[left, right]], colWidths=[10.6 * cm, 6.5 * cm], style=TableStyle([
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("LEFTPADDING", (0, 0), (-1, -1), 0),
        ("RIGHTPADDING", (0, 0), (-1, -1), 8),
    ])))
    story.append(Spacer(1, 0.22 * cm))
    story.append(callout("Conclusión", item["conclusion"], MINT))
    server_png = EVID / f"A_esc{idx}_{['valido','expirado','alterado','sin_cabecera','prefijo','deshabilitado'][idx-1]}_servidor.png"
    if server_png.exists():
        story.append(PageBreak())
        story.append(p(f"2.{idx} Evidencia de servidor · {item['nombre']}", "H1A"))
        story.append(p("Captura original de Postman tras ejecutar GET /api/v1/productos. El código HTTP y el message provienen de la respuesta real del backend; la pestaña visible evita mostrar el JWT."))
        story.append(p("Repetición capturada: " + item.get("repeticion_postman_capturada", "")))
        story.append(Spacer(1, 0.3 * cm))
        story.append(image_fit(server_png, 17.1 * cm, 13.0 * cm))
        story.append(Spacer(1, 0.3 * cm))
        story.append(callout("Resultado observado", "HTTP " + item["http"] + " · " + item["mensaje"], MINT))
    if not server_png.exists():
        story.append(Spacer(1, 0.15 * cm))
        story.append(p("Control: la captura visual de Postman/terminal está pendiente; el registro textual mostrado proviene del resultado real guardado y no contiene tokens.", "SmallA"))

# Almacenamiento
story.append(PageBreak())
story.append(p("3. Almacenamiento seguro", "H1A"))
alm = resultados["almacen"]
alm_rows = [
    [p("Prueba", "WhiteA"), p("Cómo se verificó", "WhiteA"), p("Resultado observado", "WhiteA")],
    [p("Archivo cifrado"), p(alm["archivo_como"]), p(alm["archivo_obs"])],
    [p("Log saneado"), p(alm["log_como"]), p(alm["log_obs"])],
    [p("Persistencia"), p(alm["sesion_como"]), p(alm["sesion_obs"])],
    [p("iOS"), p("No ejecutado"), p(alm["ios_nota"])],
]
story.append(Table(alm_rows, colWidths=[3.1 * cm, 6.8 * cm, 7.2 * cm], style=TableStyle([
    ("BACKGROUND", (0, 0), (-1, 0), GREEN),
    ("BACKGROUND", (0, 1), (-1, -1), GRAY),
    ("BOX", (0, 0), (-1, -1), 0.6, colors.HexColor("#C7D2CE")),
    ("INNERGRID", (0, 0), (-1, -1), 0.25, colors.HexColor("#D9E1DE")),
    ("VALIGN", (0, 0), (-1, -1), "TOP"),
    ("LEFTPADDING", (0, 0), (-1, -1), 6),
    ("RIGHTPADDING", (0, 0), (-1, -1), 6),
    ("TOPPADDING", (0, 0), (-1, -1), 6),
    ("BOTTOMPADDING", (0, 0), (-1, -1), 6),
])))
story.append(Spacer(1, 0.35 * cm))
story.append(image_fit(EVID / "A_alm2_logcat_saneado.png", 17.1 * cm, 7.0 * cm))
story.append(p("Salida real de Logcat abierta en Bloc de notas: Authorization: *** y GET 200.", "CaptionA"))
story.append(Spacer(1, 0.25 * cm))
story.append(Table([[image_fit(EVID / "A_alm3_sesion_persiste.png", 5.2 * cm, 8.3 * cm), callout("Sesión persistente", "Tras iniciar sesión, se forzó el cierre del proceso con adb am force-stop y se volvió a abrir la app. La captura muestra que entró a Inicio sin solicitar credenciales.", MINT, 10.8 * cm)]], colWidths=[6.0 * cm, 11.1 * cm], style=TableStyle([("VALIGN", (0, 0), (-1, -1), "TOP")])))
story.append(PageBreak())
story.append(p("3.1 Archivo cifrado en Android", "H1A"))
story.append(p("Se extrajo sesion_segura.xml con adb shell run-as pe.edu.upeu.pharmamobil cat shared_prefs/sesion_segura.xml. La salida original se abrió en Bloc de notas y se capturó. Se ve una clave y un valor cifrados, además de los keysets internos de AndroidX Security. La búsqueda eyJ devolvió cero coincidencias."))
story.append(Spacer(1, 0.3 * cm))
story.append(image_fit(EVID / "A_alm1_archivo_cifrado.png", 17.1 * cm, 12.0 * cm))
story.append(p("Captura real de la salida de ADB; no es una pantalla simulada ni una captura de Device Explorer.", "CaptionA"))
story.append(Spacer(1, 0.4 * cm))
story.append(callout("Observación", "El token no se conserva en texto claro. Al cerrar sesión desaparece la entrada cifrada del token y permanecen únicamente los keysets del mecanismo de cifrado.", MINT))

# Cierre
story.append(PageBreak())
story.append(p("4. Cierre de sesión completo", "H1A"))
close_rows = [[p("Efecto", "WhiteA"), p("Estado", "WhiteA"), p("Observación", "WhiteA")]]
labels = ["Token borrado del almacén", "Caché HTTP limpiada", "Interfaz limpia", "Back stack protegido"]
for label, item in zip(labels, resultados["cierre"]):
    close_rows.append([p(label), p("OK" if item["ok"] else "Pendiente"), p(item["obs"])])
story.append(Table(close_rows, colWidths=[4.5 * cm, 2.0 * cm, 10.6 * cm], style=TableStyle([
    ("BACKGROUND", (0, 0), (-1, 0), GREEN),
    ("BACKGROUND", (0, 1), (-1, -1), GRAY),
    ("TEXTCOLOR", (1, 1), (1, -1), GREEN),
    ("BOX", (0, 0), (-1, -1), 0.6, colors.HexColor("#C7D2CE")),
    ("INNERGRID", (0, 0), (-1, -1), 0.25, colors.HexColor("#D9E1DE")),
    ("VALIGN", (0, 0), (-1, -1), "TOP"),
    ("LEFTPADDING", (0, 0), (-1, -1), 6),
    ("RIGHTPADDING", (0, 0), (-1, -1), 6),
    ("TOPPADDING", (0, 0), (-1, -1), 6),
    ("BOTTOMPADDING", (0, 0), (-1, -1), 6),
])))
story.append(Spacer(1, 0.35 * cm))
story.append(Table([
    [image_fit(EVID / "A_cierre3_login_limpio.png", 5.1 * cm, 10.0 * cm), image_fit(EVID / "A_cierre4_atras_bloqueado.png", 5.1 * cm, 10.0 * cm)],
    [p("Login sin datos anteriores", "CaptionA"), p("Atrás no vuelve a la app autenticada", "CaptionA")],
], colWidths=[8.55 * cm, 8.55 * cm], style=TableStyle([("ALIGN", (0, 0), (-1, -1), "CENTER"), ("VALIGN", (0, 0), (-1, -1), "TOP")])))
story.append(Spacer(1, 0.25 * cm))
story.append(callout("Prueba de caché", "Verificación real: el proceso Android PID 2627 hizo una petición 200 con sesión; después del logout, el mismo cliente y proceso hizo una petición sin Authorization y recibió 401. Refuerzo: AuthRepositorioRestTest, 2 pruebas, 0 fallos.", MINT))
story.append(PageBreak())
story.append(p("4.1 Almacén y cliente después del logout", "H1A"))
story.append(image_fit(EVID / "A_cierre1_almacen_vacio.png", 17.1 * cm, 9.4 * cm))
story.append(p("Salida real del archivo: solo quedan keysets, sin entrada de JWT.", "CaptionA"))
story.append(Spacer(1, 0.3 * cm))
story.append(image_fit(EVID / "A_cierre2_cliente_sin_token.png", 17.1 * cm, 9.4 * cm))
story.append(p("Logcat real: GET sin Authorization, HTTP 401 y token_presente=false. Mismo PID 2627.", "CaptionA"))
story.append(Spacer(1, 0.2 * cm))
story.append(p("Las dos salidas de ADB se abrieron en Bloc de notas para tomar estas capturas. La reapertura posterior permaneció en el login; evidencia adicional: A_cierre_reapertura_login.png.", "SmallA"))

# Matriz
story.append(PageBreak())
story.append(p("5. Matriz de permisos", "H1A"))
matrix = resultados["matriz"]
matrix_rows = [[p("Operación", "WhiteA"), p("admin", "WhiteA"), p("vendedor", "WhiteA"), p("almacen", "WhiteA")]]
for operation, roles in matrix.items():
    matrix_rows.append([
        p(operation),
        p(f"HTTP {roles['admin']['http']} · {roles['admin']['ui']}", "SmallA"),
        p(f"HTTP {roles['vendedor']['http']} · {roles['vendedor']['ui']}", "SmallA"),
        p(f"HTTP {roles['almacen']['http']} · {roles['almacen']['ui']}", "SmallA"),
    ])
story.append(Table(matrix_rows, colWidths=[5.2 * cm, 3.95 * cm, 3.95 * cm, 3.95 * cm], repeatRows=1, style=TableStyle([
    ("BACKGROUND", (0, 0), (-1, 0), GREEN),
    ("BACKGROUND", (0, 1), (-1, -1), GRAY),
    ("BOX", (0, 0), (-1, -1), 0.6, colors.HexColor("#C7D2CE")),
    ("INNERGRID", (0, 0), (-1, -1), 0.3, colors.HexColor("#D9E1DE")),
    ("VALIGN", (0, 0), (-1, -1), "TOP"),
    ("LEFTPADDING", (0, 0), (-1, -1), 6),
    ("RIGHTPADDING", (0, 0), (-1, -1), 6),
    ("TOPPADDING", (0, 0), (-1, -1), 7),
    ("BOTTOMPADDING", (0, 0), (-1, -1), 7),
])))
story.append(Spacer(1, 0.35 * cm))
story.append(p("Lectura de la matriz", "H2A"))
story.append(p("El backend rechazó con 403 las operaciones fuera del permiso de cada rol. Los códigos provienen de peticiones directas autenticadas; la oferta de acciones se observó en Android. El vendedor ve productos en modo consulta; almacén no ve Clientes ni Pedidos. Clientes utiliza el repositorio en memoria de esta versión académica. Registrar venta y Reportes aún no cuentan con pantallas funcionales."))
story.append(callout("Corrección de interfaz", "La portada del vendedor ahora dice Consultar productos y explica que puede revisar catálogo y stock. Commit 2c787d0.", MINT))

story.append(PageBreak())
story.append(p("5.1 Evidencia visual por rol", "H1A"))
role_images = [
    ("admin", EVID / "A_perm_admin.png"),
    ("vendedor", EVID / "A_perm_vendedor.png"),
    ("almacen", EVID / "A_perm_almacen.png"),
]
story.append(Table([
    [image_fit(path, 5.2 * cm, 12.0 * cm) for _, path in role_images],
    [p(name, "CaptionA") for name, _ in role_images],
], colWidths=[5.7 * cm, 5.7 * cm, 5.7 * cm], style=TableStyle([("ALIGN", (0, 0), (-1, -1), "CENTER"), ("VALIGN", (0, 0), (-1, -1), "TOP")])))
story.append(Spacer(1, 0.25 * cm))
story.append(p("Evidencia adicional: A_perm_vendedor_productos.png muestra inventario sin formulario, editar ni eliminar; A_perm_almacen_productos.png muestra el formulario de registro autorizado.", "SmallA"))

# Hallazgos y commits
story.append(PageBreak())
story.append(p("6. Hallazgos, correcciones y conclusiones", "H1A"))
story.append(p("Hallazgos", "H2A"))
for h in resultados["hallazgos"]:
    story.append(p("• " + h))
story.append(Spacer(1, 0.25 * cm))
story.append(p("Commits de la actividad", "H2A"))
commit_rows = [[p("Hash", "WhiteA"), p("Mensaje", "WhiteA")]]
for c in resultados["commits"]:
    commit_rows.append([p(c["hash"], "CodeA"), p(c["mensaje"])])
story.append(Table(commit_rows, colWidths=[3.2 * cm, 13.9 * cm], style=TableStyle([
    ("BACKGROUND", (0, 0), (-1, 0), GREEN),
    ("BACKGROUND", (0, 1), (-1, -1), GRAY),
    ("BOX", (0, 0), (-1, -1), 0.6, colors.HexColor("#C7D2CE")),
    ("INNERGRID", (0, 0), (-1, -1), 0.3, colors.HexColor("#D9E1DE")),
    ("LEFTPADDING", (0, 0), (-1, -1), 7),
    ("RIGHTPADDING", (0, 0), (-1, -1), 7),
    ("TOPPADDING", (0, 0), (-1, -1), 6),
    ("BOTTOMPADDING", (0, 0), (-1, -1), 6),
])))
story.append(Spacer(1, 0.35 * cm))
story.append(p("Conclusiones", "H2A"))
for c in resultados["conclusiones"]:
    story.append(p("• " + c))

# Control de evidencias
story.append(PageBreak())
story.append(p("7. Control de evidencias", "H1A"))
required = []
suffixes = ["valido", "expirado", "alterado", "sin_cabecera", "prefijo", "deshabilitado"]
for idx, suffix in enumerate(suffixes, start=1):
    required.extend([f"A_esc{idx}_{suffix}_servidor.png", f"A_esc{idx}_{suffix}_pantalla.png"])
required.extend([
    "A_alm1_archivo_cifrado.png", "A_alm2_logcat_saneado.png", "A_alm3_sesion_persiste.png",
    "A_cierre1_almacen_vacio.png", "A_cierre2_cliente_sin_token.png", "A_cierre3_login_limpio.png", "A_cierre4_atras_bloqueado.png",
    "A_perm_admin.png", "A_perm_vendedor.png", "A_perm_almacen.png",
])
rows = [[p("Archivo", "WhiteA"), p("Estado", "WhiteA")]]
for name in required:
    present = (EVID / name).exists()
    rows.append([p(name, "SmallA"), p("Obtenida" if present else "Pendiente visual", "SmallA")])
story.append(Table(rows, colWidths=[12.6 * cm, 4.5 * cm], repeatRows=1, style=TableStyle([
    ("BACKGROUND", (0, 0), (-1, 0), GREEN),
    ("BACKGROUND", (0, 1), (-1, -1), GRAY),
    ("TEXTCOLOR", (1, 1), (1, -1), DARK),
    ("BOX", (0, 0), (-1, -1), 0.6, colors.HexColor("#C7D2CE")),
    ("INNERGRID", (0, 0), (-1, -1), 0.25, colors.HexColor("#D9E1DE")),
    ("LEFTPADDING", (0, 0), (-1, -1), 6),
    ("RIGHTPADDING", (0, 0), (-1, -1), 6),
    ("TOPPADDING", (0, 0), (-1, -1), 3),
    ("BOTTOMPADDING", (0, 0), (-1, -1), 3),
])))
story.append(Spacer(1, 0.25 * cm))
story.append(callout("Estado de entrega", "Se obtuvieron las 22 capturas principales solicitadas: 12 de escenarios, 3 de almacenamiento, 4 de cierre y 3 de roles. Hay 3 capturas adicionales. Se verificó la restauración a 60 minutos y los tres usuarios activos. iOS quedó fuera del alcance indicado. El push de los commits locales permanece pendiente de autorización.", MINT))
story.append(Spacer(1, 0.25 * cm))
story.append(p("Repositorio y rama", "H2A"))
story.append(p(datos["url_rama"]))
story.append(PageBreak())
story.append(p("8. Lista de cotejo previa a la entrega", "H1A"))
cotejo_textos=[
    "PDF con el nombre S10_ActividadAutonoma_Quispe.pdf.",
    "Portada con nombre completo, sesión, fecha y enlace a la rama.",
    "Seis escenarios con sus capturas respectivas.",
    "Código HTTP y message literal en cada escenario (200 no contiene message de error).",
    "Expiración detectada y navegación al login documentadas.",
    "Evidencia del archivo cifrado sin JWT legible.",
    "Captura de Logcat con Authorization saneada.",
    "Cuatro efectos del logout verificados, incluido el cliente HTTP real.",
    "Matriz de seis operaciones y tres usuarios con HTTP y oferta de UI.",
    "Expiración restaurada a 60 minutos y usuarios reactivados.",
    "Commits de la actividad accesibles en la rama remota: pendiente del push autorizado."
]
cotejo_rows=[[p("N.º","WhiteA"),p("Criterio observable","WhiteA"),p("Estado","WhiteA")]]
for i,texto in enumerate(cotejo_textos,1):
    cotejo_rows.append([p(str(i)),p(texto),p("Sí" if resultados["cotejo"][str(i)] else "Pendiente")])
story.append(Table(cotejo_rows,colWidths=[1.1*cm,13.4*cm,2.6*cm],style=TableStyle([
    ("BACKGROUND",(0,0),(-1,0),GREEN),
    ("BACKGROUND",(0,1),(-1,-1),GRAY),
    ("INNERGRID",(0,0),(-1,-1),0.3,colors.HexColor("#D9E1DE")),
    ("VALIGN",(0,0),(-1,-1),"TOP"),
    ("LEFTPADDING",(0,0),(-1,-1),7),
    ("RIGHTPADDING",(0,0),(-1,-1),7),
    ("TOPPADDING",(0,0),(-1,-1),7),
    ("BOTTOMPADDING",(0,0),(-1,-1),7)
])))
story.append(Spacer(1,0.4*cm))
story.append(p("Notas de ejecución", "H2A"))
for nota in resultados["notas"]:
    story.append(p("• "+nota,"SmallA"))

doc.build(story)
print(OUT)
