# Matriz de cumplimiento - sesión 13

Fecha inicial: 04/11/2026. Rama: feature/integrador-13, creada desde develop después de auditar ramas. Los estados «pendiente» no equivalen a errores comprobados en el recorrido.

| Paso | Requisito | Estado inicial | Archivos | Pruebas | Estado final |
|---|---|---|---|---|---|
| Auditoría | Identidad local, remoto y selección de base con sesión 11 | Árbol limpio; remoto correcto; rama predeterminada anterior | 00_Auditoria_Inicial.md; evidencias/sesion13/git/ | fetch, log, rev-list; autor local verificado | Verificado; develop y feature/integrador-13 publicados |
| Auditoría | Línea base real de hoy | 94 pruebas existentes | evidencias/sesion13/pruebas/linea-base.txt | 94 aprobadas; 0 fallos/errores/omitidas; APK generado | Verificado |
| 1 | Recorrer flujo completo y registrar huecos | Código inspeccionado y línea base comprobada | 01_Tabla_Huecos_JorgeQuispe.md | Ocho momentos reales con capturas; POST offline confirmado por Postman ID 103; expiración legítima con borrador conservado | Recorrido verificado; logout autorizado y corregido en 4f89b54 (96 pruebas) |
| 2 | Guardar intentos y último error por pendiente | Columnas ausentes; esquema 2 con migración 1.sqm existente | Producto.sq, ProductoLocal.kt, 2.sqm, EstadoEnvioLocalTest | Android 2→3 conserva 12 filas y 1 pendiente; 99 pruebas aprobadas | Verificado: Android 2→3, 99 pruebas y tarea SQLDelight aprobada con inicializador del worker |
| 3 | Distinguir fallo transitorio de permanente | Jerarquías ErrorApi/ErrorSesion; sin estado persistido por fila | ProductoRepositorioOffline.kt, EjecutarLlamada.kt | Mapeo 400/401/403/404/409/5xx/transporte | Verificado: commit 7bdbe76; 107 pruebas, sin fallos; protección de POST incierto |
| 3 | Espera creciente y máximo de intentos | Mutex existente; sin política de backoff | Ruta única de sincronización existente | Tiempo virtual; límite y persistencia | Verificado: commit 7bdbe76; 107 pruebas, sin fallos; protección de POST incierto |
| 3 | Mantener visibles las filas que agotan intentos | Solo existe marca pendiente | ProductoLocal y dominio Producto | Permanentes y agotadas no se reenvían | Verificado: commit 7bdbe76; 107 pruebas, sin fallos; protección de POST incierto |
| 3 | Subir antes de bajar sin perder datos | Implementado y probado en sesión 11 | ProductoRepositorioOffline.kt | Regresión; POST incierto requiere política segura | Verificado: commit 7bdbe76; 107 pruebas, sin fallos; protección de POST incierto |
| 4 | Contadores y antigüedad visibles | Fecha observable implementada; faltan contadores y conectividad nativa | ProductoViewModel.kt, ProductoScreen.kt | combine/stateIn y cambios SQLDelight | Verificado: 109 pruebas; emulador sin red → ID 123; corrección en misma fila y fecha persistida |
| 5 | Interfaz común GestorPermisos | Ausente | domain/permiso/GestorPermisos.kt | Fake de permisos | Verificado: 112 pruebas; tres estados Android y notificación real, capturas 11a–11i |
| 5 | Android distingue tres estados | Ausente | GestorPermisosAndroid.kt y PuentePermisos | Denegado, concedido y definitivo reales | Verificado: 112 pruebas; tres estados Android y notificación real, capturas 11a–11i |
| 5 | Declaraciones de permiso | Solo INTERNET | AndroidManifest.xml; iOS excluido | Notificaciones API 33 y degradación anterior | Verificado: 112 pruebas; tres estados Android y notificación real, capturas 11a–11i |
| 5 | Explicar antes de pedir; Ajustes si es definitivo | Ausente | PermisosCard.kt y PermisosViewModel.kt | Lógica común y capturas reales | Verificado: 112 pruebas; tres estados Android y notificación real, capturas 11a–11i |
| 6 | Firebase detrás de interfaz y SDK solo en datos | Proyecto creado por estudiante; app Android registrada; JSON pendiente | 00_Auditoria_Inicial.md; evidencia firebase/01_android_registrado.jpg | Inicialización y evento real todavía no ejecutados | Verificado: SDK real, 115 pruebas, no fatal recibido en consola y Messaging comprobado sin exportar token |
| 7 | Pruebas commonTest aprobadas | Línea base 94/94 aprobada hoy | commonTest; helper expect/actual existente | Nuevas pruebas de intentos/permisos/telemetría y regresión | Verificado: 119 aprobadas, cero fallos/errores/omitidas; migración y APK; 79 tareas ejecutadas |
| Git | Commit real por paso distribuido en la sesión | Sin cambios funcionales de sesión 13 todavía | Rama feature/integrador-13 | Revisar autor Jorge Quispe y commits por paso | Pasos 1–7 publicados con autor Jorge Quispe; 119 pruebas aprobadas y cierre con PDF y Postman real |
| Entorno | iOS y dispositivo físico | iOS excluido por el estudiante; equipo Windows | 00_Auditoria_Inicial.md | No se afirma ejecución iOS | Sin evidencia iOS; físico no confirmado |


## Cotejo final de los 14 criterios (Android)

Todas las filas siguientes tienen resultado **Sí** dentro del alcance Android. Las rutas de clases corresponden a shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobil/ salvo las Android indicadas; las pruebas homónimas están en commonTest.

| Nº | Clase o archivo y método/elemento | Prueba o verificación y resultado | Evidencia | Commit |
|---|---|---|---|---|
| 1 | 01_Tabla_Huecos_JorgeQuispe.md, ocho momentos | Recorrido real completo, huecos registrados | capturas 01-08c | abae5a8, 4f89b54 |
| 2 | Producto.sq; data/local/ProductoLocal.kt, intentos/ultimoError; 2.sqm | EstadoEnvioLocalTest.migrarVersionDosConservaTodosLosCamposYMarcaDeSincronizacion; migración Gradle aprobada | migración real 2 a 3; salida-pruebas.txt | 30a6899, afbd490 |
| 3 | data/repository/ProductoRepositorioOffline.kt, sincronizar; traducción ResultadoEnvio | SincronizacionRefinadaTest.mapeoTipadoIncluyeTransporteValidacionYAutorizacion, aprobada | pruebas/reporte-final/ | 7bdbe76 |
| 4 | Política de espera y límite en sincronización | cincoIntentosPersistidosSinSextoNiEsperaDespuesDelUltimo; formulaDeEsperaIncluyeDieciseisYAcotaElDesplazamiento, aprobadas | XML y reporte HTML | 7bdbe76 |
| 5 | Estado persistido y ProductoScreen, fila fallida | EstadoEnvioLocalTest.contadoresObservablesYFallidaPermanenteExcluidaDeEnvio, aprobada | capturas 10d-10e | 7bdbe76, d30930f |
| 6 | ProductoRepositorioOffline.sincronizar, subida antes de descarga | rechazoDeUnaFilaNoImpideSubirLasDemasYLaBajadaEsLaUltima; dosSincronizacionesConcurrentesNoDuplicanLaCreacion, aprobadas | capturas 10b-10g; Postman ID 123 | 7bdbe76, 15db0d7 |
| 7 | presentation/producto/ProductoViewModel.kt y ProductoScreen.kt, estado observable | EstadoSincronizacionTest.estadoCombinaConsultasSqlFechaEnCursoYConectividadSinContarAMano, aprobada | capturas 10-10h | d30930f |
| 8 | domain/permiso/GestorPermisos.kt, interfaz | PermisosTest con FakeGestorPermisos, aprobada | 05_Gestor_Permisos.md | a8f45be |
| 9 | Android data/permiso/GestorPermisosAndroid.kt, estado/solicitar | primeraSolicitudNoSeConfundeConDenegacionDefinitiva y recorrido Android, aprobados | capturas 11d, 11e, 11h | a8f45be |
| 10 | AndroidManifest.xml, POST_NOTIFICATIONS | Diálogo y notificación reales; iOS no aplica al alcance acordado | capturas 11c, 11i | a8f45be |
| 11 | PermisosCard y PermisosViewModel, explicación y Ajustes | nuncaSolicitaAntesDeExplicarYTresResultadosNoSeFalsifican, aprobada | capturas 11b, 11e-11g | a8f45be |
| 12 | data/firebase/TelemetriaFirebase.kt, registrarEvento/registrarError | Imports SDK solo en datos; PrivacidadTelemetriaTest, aprobada | firebase/05_imports_sdk.txt; consola real | 7467096 |
| 13 | commonTest y tarea testAndroidHostTest | 119 aprobadas; 0 fallos, errores u omitidas; sin @Ignore/@Disabled | salida-pruebas.txt; captura 12 | 15db0d7 |
| 14 | feature/integrador-13 desde develop | Siete commits principales, autor Jorge Quispe, historial conservado | git/; PR #1 en borrador | hashes anteriores y cierre final |

El PR [#1](https://github.com/123456jor/pharmaMobile-/pull/1) queda en borrador para revisión, sin fusionar. La publicación final se verifica contra origin. No se afirma ejecución iOS, dispositivo físico ni campaña push remota.
