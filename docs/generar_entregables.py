import json
from pathlib import Path
from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import mm
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, Preformatted

ROOT = Path(__file__).resolve().parent.parent
DOCS = ROOT / "docs"
OUT = DOCS / "entrega"
OUT.mkdir(exist_ok=True)
CFG = json.loads((DOCS / "config.json").read_text(encoding="utf-8"))
BLUE = colors.HexColor("#123B5D")
TEAL = colors.HexColor("#087E8B")
PALE = colors.HexColor("#EAF2F7")
PENDING = "PENDIENTE — completar con evidencia real"
styles = getSampleStyleSheet()
styles.add(ParagraphStyle(name="Cover", parent=styles["Title"], fontName="Helvetica-Bold", fontSize=25, leading=31, textColor=BLUE, alignment=TA_CENTER, spaceAfter=12))
styles.add(ParagraphStyle(name="H1x", parent=styles["Heading1"], fontSize=17, leading=21, textColor=BLUE, spaceBefore=5, spaceAfter=9))
styles.add(ParagraphStyle(name="H2x", parent=styles["Heading2"], fontSize=12, leading=15, textColor=TEAL, spaceBefore=6, spaceAfter=5))
styles.add(ParagraphStyle(name="Bodyx", parent=styles["BodyText"], fontSize=9, leading=13, spaceAfter=6))
styles.add(ParagraphStyle(name="Smallx", parent=styles["BodyText"], fontSize=7.4, leading=10))
styles.add(ParagraphStyle(name="Pendingx", parent=styles["BodyText"], fontSize=9, leading=12, textColor=colors.HexColor("#805000"), backColor=colors.HexColor("#FFF4D6"), borderColor=colors.HexColor("#D9A441"), borderWidth=.5, borderPadding=7, spaceAfter=7))

def p(text, style="Bodyx"):
    return Paragraph(str(text).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"), styles[style])

def table(rows, widths=None, small=True):
    converted = [[p(c, "Smallx" if small else "Bodyx") for c in row] for row in rows]
    t = Table(converted, colWidths=widths, repeatRows=1, hAlign="LEFT")
    t.setStyle(TableStyle([
        ("BACKGROUND", (0,0), (-1,0), BLUE), ("TEXTCOLOR", (0,0), (-1,0), colors.white),
        ("GRID", (0,0), (-1,-1), .35, colors.HexColor("#BCCBD6")),
        ("VALIGN", (0,0), (-1,-1), "TOP"), ("LEFTPADDING", (0,0), (-1,-1), 5),
        ("RIGHTPADDING", (0,0), (-1,-1), 5), ("TOPPADDING", (0,0), (-1,-1), 5),
        ("BOTTOMPADDING", (0,0), (-1,-1), 5), ("ROWBACKGROUNDS", (0,1), (-1,-1), [colors.white, PALE])]))
    return t

def frame(canvas, doc):
    canvas.saveState()
    w, h = A4
    canvas.setFillColor(BLUE); canvas.rect(0, h-7*mm, w, 7*mm, fill=1, stroke=0)
    canvas.setFont("Helvetica", 8); canvas.setFillColor(colors.HexColor("#647887"))
    canvas.drawString(18*mm, 12*mm, "PharmaMobil · Sesión 08 · Evidencia verificable")
    canvas.drawRightString(w-18*mm, 12*mm, f"Página {doc.page}")
    canvas.restoreState()

def cover(subtitle):
    name = CFG.get("nombre_completo") or "[COMPLETAR NOMBRE COMPLETO]"
    surname = CFG.get("apellidos") or "[COMPLETAR APELLIDOS]"
    date = CFG.get("fecha_entrega") or "[COMPLETAR FECHA]"
    branch_url = CFG.get("url_rama") or "PENDIENTE DE PUBLICAR"
    return [Spacer(1, 37*mm), p("PHARMAMOBIL", "Cover"), p("SESIÓN 08", "H1x"), p(subtitle, "H2x"), Spacer(1, 9*mm),
      table([["Estudiante", name], ["Apellidos", surname], ["Fecha", date], ["Rama prevista", "feature/crud-productos-quispe · " + branch_url]], [40*mm, 115*mm], False),
      Spacer(1, 14*mm), p("Estado de esta entrega", "H2x"),
      p("Implementación y pruebas Android host preparadas. Los resultados que requieren ejecutar la app en Android/iOS y conectar PharmaSoft quedan explícitamente pendientes; este documento no los presenta como observados.", "Pendingx"), PageBreak()]

def evidence_box(label, filename):
    path = DOCS / "evidencias" / filename
    if path.exists() and path.suffix.lower() in (".png", ".jpg", ".jpeg"):
        from reportlab.platypus import Image
        im = Image(str(path), width=155*mm, height=82*mm, kind="proportional")
        return [p(label, "H2x"), im]
    box = Table([[p(f"CAPTURA REAL PENDIENTE<br/><br/>{label}<br/>{filename}", "Pendingx")]], colWidths=[155*mm], rowHeights=[35*mm])
    box.setStyle(TableStyle([("BOX",(0,0),(-1,-1),1,colors.HexColor("#D9A441")),("VALIGN",(0,0),(-1,-1),"MIDDLE"),("ALIGN",(0,0),(-1,-1),"CENTER")]))
    return [box]

def activity():
    story = cover("ACTIVIDAD AUTÓNOMA · CRUD DE PRODUCTOS")
    story += [p("1. Alcance y arquitectura", "H1x"), p("Se implementó el flujo de productos con separación por capas: UI y estado, casos de uso, repositorio y API HTTP Ktor. La llamada REST incluye listar con paginación, obtener, crear, actualizar y eliminar. Los DTO se mantienen separados del modelo de dominio.")]
    story += [table([["Operación", "Petición", "HTTP real", "Resultado en app"],
      ["Listar", "GET /api/v1/productos?pagina=0&tamanio=20", "PENDIENTE", "PENDIENTE"], ["Obtener", "GET /api/v1/productos/{id}", "PENDIENTE", "PENDIENTE"],
      ["Crear", "POST /api/v1/productos", "PENDIENTE", "PENDIENTE"], ["Actualizar", "PUT /api/v1/productos/{id}", "PENDIENTE", "PENDIENTE"], ["Eliminar", "DELETE /api/v1/productos/{id}", "PENDIENTE", "PENDIENTE"]], [23*mm, 68*mm, 27*mm, 37*mm]), Spacer(1,6*mm),
      p("La tabla conserva los campos reales vacíos en docs/evidencias/matriz-operaciones.json. Se confirmó el contrato leyendo PharmaSoft, pero el servidor y la base Oracle no estaban disponibles para registrar respuestas HTTP reales.", "Pendingx"), PageBreak()]
    story += [p("2. Estados y manejo de errores", "H1x"), table([["Situación", "Contrato de PharmaSoft", "Manejo/evidencia"], ["HTTP 400", "DTO con validationErrors por campo", "Validacion; prueba MockEngine"], ["HTTP 404", "Producto no encontrado", "NoEncontrado; prueba MockEngine"], ["HTTP 409", "ReglaNegocioException; nombre duplicado o producto ya inactivo", "Conflicto; prueba MockEngine"], ["DELETE", "204; baja lógica estado=false, conserva fila", "La app filtra inactivos tras recargar"], ["5xx / sin conexión / timeout", "Contrato estático confirmado", "Mapeo implementado; falta ejecución real"], ["Cancelación", "La corrutina se cancela", "CancellationException relanzada; prueba automatizada"]], [32*mm, 65*mm, 58*mm]), Spacer(1,5*mm), p("El controlador también expone GET /api/v1/categorias. El formulario solicita un categoriaId existente. La ficha propone que un producto con ventas cause 409 al eliminar, pero el backend aplica baja lógica y no rechaza por ventas; se documenta en cambio la regla 409 real de nombre duplicado y la segunda baja del mismo ID."), PageBreak()]
    story += [p("3. Pruebas automatizadas disponibles", "H1x"), p("Salida guardada en docs/evidencias/salida-pruebas.txt con rutas locales redactadas. Resultado: BUILD SUCCESSFUL; Gradle reporta 56 pruebas Android host/commonTest y los XML muestran cero fallos y cero errores."), p("Cobertura incluye estados de pantalla, errores de validación, conservación de lista ante fallo, baja lógica con filtro de productos inactivos, cancelación y traducción de HTTP 400/404/409 con MockEngine."), p("La compilación :androidApp:compileDebugKotlin tuvo éxito. El APK debug no se pudo firmar porque el entorno denegó debug.keystore.lock. La prueba iOS requiere macOS y Xcode, no disponibles en este equipo. La prueba integrada Android contra PharmaSoft requiere servidor y base Oracle."), p("Comando ejecutado: .\\gradlew.bat :shared:testAndroidHostTest", "Smallx"), PageBreak()]
    story += [p("4. Bitácora de ocho escenarios", "H1x"), p("La bitácora separa expresamente respuestas esperadas por la guía y observaciones pendientes. Ejecutar cada procedimiento de docs/scripts/ y anotar fecha, plataforma, HTTP, ErrorApi, texto mostrado y ruta de la captura."), p("Validación de nombre y precio, producto inexistente, eliminación duplicada, regla de negocio 409, servidor detenido, timeout forzado y cancelación al salir de la pantalla."), p("No se inventaron mensajes, códigos, capturas ni resultados del servicio." ,"Pendingx"), PageBreak()]
    story += [p("5. Evidencia visual por completar", "H1x"), p("Capturar cada flujo en Android y iOS, cuando el entorno esté disponible. Colocar imágenes en docs/evidencias/android/ y docs/evidencias/ios/." )]
    for label, f in [("Listar productos · Android", "android/productos-lista.png"),("Crear / validación · Android", "android/producto-crear.png"),("Editar / eliminar · Android", "android/producto-editar-eliminar.png"),("Errores reales del servidor", "errores/esc1-validacion-nombre-android.png")]: story += evidence_box(label,f)+[Spacer(1,4*mm)]
    return story

def guide():
    story = cover("GUÍA PRÁCTICA · IMPLEMENTACIÓN Y EVIDENCIAS")
    story += [p("1. Lista de comprobación de la guía", "H1x"), table([["Criterio", "Implementación / estado"],
      ["Configurar cliente Ktor multiplataforma", "Implementado; motores CIO (Android) y Darwin (iOS)."], ["Modelar request/response y paginación", "DTO y wrapper de página implementados; verificar forma exacta del backend."],
      ["Listar y obtener productos", "API, repositorio, casos de uso y UI implementados."], ["Crear, actualizar y eliminar", "Flujos implementados; integración real pendiente."],
      ["Validación del servidor", "400 mapeado; falta contrastar contrato real."], ["Errores 404 y 409", "Mapeos implementados; 409 de negocio pendiente de comprobar."],
      ["Sin conexión y timeout", "Mapeos implementados; observación visual pendiente."], ["Cancelar solicitud", "CancellationException se relanza y está cubierta por test."],
      ["Probar Android e iOS", "Android host tests ejecutados; iOS requiere Mac/Xcode."], ["Adjuntar capturas y bitácora", "Plantillas preparadas; capturas reales pendientes."]], [56*mm, 99*mm]), PageBreak()]
    story += [p("2. Pasos de ejecución", "H1x"), p("1) Inicia Oracle y PharmaSoft. 2) Comprueba GET /api/health y GET /api/v1/categorias. 3) Arranca la app Android en emulador (10.0.2.2:8080) o iOS (localhost:8080). 4) Usa un ID de categoría existente. 5) Ejecuta listar, crear, actualizar y baja lógica; verifica 204 y que el producto desaparezca del inventario activo. 6) Prueba 400/404/409 con los guiones; el caso 409 por ventas descrito en la ficha no coincide con esta implementación. 7) Repite en simulador iOS. 8) Guarda capturas, completa la bitácora y actualiza ambos PDFs."), p("Contrato contrastado en el código fuente del backend. GET /api/health agotó el tiempo de espera; faltó confirmar ejecución contra Oracle y obtener respuestas/capturas reales.", "Pendingx"), PageBreak()]
    story += [p("3. Resultado de pruebas locales", "H1x"), p("Android host/commonTest: 56 pruebas, 0 fallos y 0 errores; salida completa en docs/evidencias/salida-pruebas.txt. :androidApp:compileDebugKotlin compiló correctamente. El ensamblado del APK debug se bloqueó porque el entorno denegó el archivo de bloqueo debug.keystore.lock."), p("iOS simulator y CRUD integrado esperan su plataforma/servidor Oracle. Archivos .http y .ps1 en docs/scripts documentan entradas de prueba. El caso de cancelación se provoca navegando fuera de la pantalla mientras la operación está activa."), PageBreak()]
    story += [p("4. Evidencias que todavía se requieren", "H1x"), p("Use una captura sin datos personales reales; registre fecha y plataforma. Nunca transcriba la respuesta esperada como si fuera observada." )]
    for label,f in [("Android · lista", "android/productos-lista.png"),("Android · crear, editar, eliminar", "android/producto-crud.png"),("iOS · lista y CRUD", "ios/producto-crud.png"),("Errores de servidor · ocho escenarios", "errores/esc1-validacion-nombre-android.png")]: story += evidence_box(label,f)+[Spacer(1,4*mm)]
    story += [PageBreak(), p("5. Bitácora y entrega", "H1x"), p("Completa docs/bitacora.md y docs/evidencias/matriz-operaciones.json con códigos y mensajes recibidos. Copia capturas en rutas indicadas por la bitácora. Registra los commits de esta entrega en feature/crud-productos-quispe."), p("El remoto de destino es https://github.com/123456jor/pharmaMobile-.git. La carpeta Android Studio original no tenía historial .git; se prepara aquí un historial inicial para el repositorio remoto vacío." ,"Pendingx"), p("Conclusión: el CRUD y la gestión de errores están implementados y tienen cobertura host común; la verificación integrada contra Oracle y las capturas en ambos sistemas siguen pendientes.")]
    return story

for filename, title, flow in [("Actividad_Autonoma_S08.pdf", "Actividad autónoma", activity()), ("Guia_Practica_S08.pdf", "Guía práctica", guide())]:
    doc = SimpleDocTemplate(str(OUT / filename), pagesize=A4, rightMargin=18*mm, leftMargin=18*mm, topMargin=16*mm, bottomMargin=19*mm, title=title, author="")
    doc.build(flow, onFirstPage=frame, onLaterPages=frame)
    print(OUT / filename)
