# Matriz de cumplimiento - sesión 13

Fecha inicial: 04/11/2026. Rama: feature/integrador-13, creada desde develop después de auditar ramas. Los estados «pendiente» no equivalen a errores comprobados en el recorrido.

| Paso | Requisito | Estado inicial | Archivos | Pruebas | Estado final |
|---|---|---|---|---|---|
| Auditoría | Identidad local, remoto y selección de base con sesión 11 | Árbol limpio; remoto correcto; rama predeterminada anterior | 00_Auditoria_Inicial.md; evidencias/sesion13/git/ | fetch, log, rev-list; autor local verificado | Verificado; develop y feature/integrador-13 publicados |
| Auditoría | Línea base real de hoy | 94 pruebas existentes | evidencias/sesion13/pruebas/linea-base.txt | 94 aprobadas; 0 fallos/errores/omitidas; APK generado | Verificado |
| 1 | Recorrer flujo completo y registrar huecos | Código inspeccionado y línea base comprobada | 01_Tabla_Huecos_JorgeQuispe.md | Ocho momentos reales con capturas; POST offline confirmado por Postman ID 103; expiración legítima con borrador conservado | Recorrido verificado; logout autorizado y corregido en 4f89b54 (96 pruebas) |
| 2 | Guardar intentos y último error por pendiente | Columnas ausentes; esquema 2 con migración 1.sqm existente | Producto.sq, ProductoLocal.kt, 2.sqm, EstadoEnvioLocalTest | Android 2→3 conserva 12 filas y 1 pendiente; 99 pruebas aprobadas | Implementado; verificación Gradle de migración pendiente por carga SQLite en Windows |
| 3 | Distinguir fallo transitorio de permanente | Jerarquías ErrorApi/ErrorSesion; sin estado persistido por fila | ProductoRepositorioOffline.kt, EjecutarLlamada.kt | Mapeo 400/401/403/404/409/5xx/transporte | Pendiente |
| 3 | Espera creciente y máximo de intentos | Mutex existente; sin política de backoff | Ruta única de sincronización existente | Tiempo virtual; límite y persistencia | Pendiente |
| 3 | Mantener visibles las filas que agotan intentos | Solo existe marca pendiente | ProductoLocal y dominio Producto | Permanentes y agotadas no se reenvían | Pendiente |
| 3 | Subir antes de bajar sin perder datos | Implementado y probado en sesión 11 | ProductoRepositorioOffline.kt | Regresión; POST incierto requiere política segura | Base verificada; refinamiento pendiente |
| 4 | Contadores y antigüedad visibles | Fecha observable implementada; faltan contadores y conectividad nativa | ProductoViewModel.kt, ProductoScreen.kt | combine/stateIn y cambios SQLDelight | Pendiente |
| 5 | Interfaz común GestorPermisos | Ausente | Pendiente domain/permiso | Fake de permisos | Pendiente |
| 5 | Android distingue tres estados | Ausente | Pendiente implementación y puente Android | Denegado, concedido y definitivo reales | Pendiente |
| 5 | Declaraciones de permiso | Solo INTERNET | AndroidManifest.xml; iOS excluido | Notificaciones API 33 y degradación anterior | Pendiente |
| 5 | Explicar antes de pedir; Ajustes si es definitivo | Ausente | Pendiente UI de permiso contextual | Lógica común y capturas reales | Pendiente |
| 6 | Firebase detrás de interfaz y SDK solo en datos | Proyecto creado por estudiante; app Android registrada; JSON pendiente | 00_Auditoria_Inicial.md; evidencia firebase/01_android_registrado.jpg | Inicialización y evento real todavía no ejecutados | Pendiente; no se simula integración |
| 7 | Pruebas commonTest aprobadas | Línea base 94/94 aprobada hoy | commonTest; helper expect/actual existente | Nuevas pruebas de intentos/permisos/telemetría y regresión | Pendiente ampliación |
| Git | Commit real por paso distribuido en la sesión | Sin cambios funcionales de sesión 13 todavía | Rama feature/integrador-13 | Revisar autor Jorge Quispe y commits por paso | Pendiente; no se harán commits vacíos |
| Entorno | iOS y dispositivo físico | iOS excluido por el estudiante; equipo Windows | 00_Auditoria_Inicial.md | No se afirma ejecución iOS | Sin evidencia iOS; físico no confirmado |
