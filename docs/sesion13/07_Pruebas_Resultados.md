# Paso 7: pruebas y regresión final

Fecha: 4 de noviembre de 2026. Autor: Jorge Quispe. Rama: feature/integrador-13.

## Ejecución real final

Se ejecutó `gradlew.bat :shared:verifyCommonMainPharmaDbMigration :shared:testAndroidHostTest :androidApp:assembleDebug --rerun-tasks --console=plain`. Resultado: **BUILD SUCCESSFUL en 1 min 7 s; 79 tareas ejecutadas; 119 pruebas, cero fallos, cero errores y cero omitidas**. No se reutilizaron resultados de tareas para declarar esta ejecución.

La salida completa está en `evidencias/sesion13/salida-pruebas.txt`; el resumen calculado desde los XML JUnit, tamaño y SHA-256 del APK está en `pruebas/resumen-final.json`. El reporte HTML se conserva en `pruebas/reporte-final/index.html`. APK: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`, 25 819 289 bytes, SHA-256 `26f298beeaf896929571fa6adb0913d272816a0833698e037957bf61c4a2499c`.

## Cobertura verificable

| Requisito | Pruebas reales |
|---|---|
| 503 incrementa y conserva pendiente | SincronizacionRefinadaTest.unFalloTransitorioSumaIntentoYNoMarcaFallida |
| 409 permanente, con motivo y sin segundo POST | SincronizacionRefinadaTest.unFalloPermanenteMarcaLaFilaYNoLaReintenta |
| Máximo cinco, sin sexto y persistencia al recrear repositorio | cincoIntentosPersistidosSinSextoNiEsperaDespuesDelUltimo; IntegradorRegresionFinalTest.nuevasInstanciasLeenIntentosPersistidosYNoHacenSextoPost |
| Backoff virtual y sin espera final | formulaDeEsperaIncluyeDieciseisYAcotaElDesplazamiento; cincoIntentosPersistidosSinSextoNiEsperaDespuesDelUltimo |
| Subida antes de GET, conservación, colisiones y no duplicados | ProductoRepositorioOfflineTest; ProductoRepositorioOfflineAutonomaTest; EstadoEnvioLocalTest; rechazoDeUnaFilaNoImpideSubirLasDemasYLaBajadaEsLaUltima |
| Transporte tipado, validación y autorización | mapeoTipadoIncluyeTransporteValidacionYAutorizacion; EjecutarLlamadaTest; el401NotificaLaPoliticaDeSesionYDetieneLosSiguientesEnvios |
| Contadores, fecha y enCurso desde SQL observable | EstadoSincronizacionTest.estadoCombinaConsultasSqlFechaEnCursoYConectividadSinContarAMano |
| Permisos, explicación previa y Ajustes | PermisosTest: tabla pura y tres resultados usando FakeGestorPermisos |
| Telemetría por interfaz y privacidad | TelemetriaSincronizacionTest; PrivacidadTelemetriaTest; FakeTelemetria solo en commonTest |
| Migración sin perder campos y última fecha | EstadoEnvioLocalTest.migrarVersionDosConservaTodosLosCamposYMarcaDeSincronizacion; tarea SQLDelight y actualización real del emulador 2→3 |
| Concurrencia sin duplicar POST | IntegradorRegresionFinalTest.dosSincronizacionesConcurrentesNoDuplicanLaCreacion |
| Resultado incierto y conciliación segura | respuestaPerdidaSeBloqueaAntesDePostYOtroCicloNoReenvia; bloqueoDuraderoExisteAntesDeInvocarElMotorHttp; conciliarRespuestaInciertaValidaDatosYConfirmaSinNuevoPost |
| Regresión de login/JWT/logout/CRUD/local/UI | Se conservan las 94 pruebas iniciales, más las ampliaciones; cierre voluntario corregido con dos pruebas propias |

Cada escenario de integración común crea su propio driver y esquema en memoria, usa MockEngine y runTest. Las esperas de reintento son virtuales. No se utilizan emulador ni backend en las pruebas automatizadas. La prueba de reconstrucción crea nuevas instancias del repositorio sobre la misma base; no se presenta como prueba de muerte del proceso Android. La persistencia instalada, los permisos y Firebase se verificaron aparte en el emulador real.

La búsqueda en commonTest y androidHostTest no encontró `@Ignore` ni `@Disabled`. No se comentaron ni retiraron pruebas. El fixture anterior que representaba falta de red antes de POST se precisó con EnvioNoIniciadoException, manteniendo todas sus comprobaciones; la respuesta perdida tiene prueba independiente y exige bloqueo seguro.

## Límites explícitos

iOS fue excluido por el estudiante. No se dispuso de prueba en teléfono físico ni se afirma haberla ejecutado. La fórmula de espera incluye 16 s, pero después del quinto fallo no se duerme, porque no queda un sexto intento. Un POST incierto no se reproduce automáticamente aunque el error de transporte sea de clase transitoria. Las capturas reales de permisos y Firebase se conservan en sus carpetas y en el PDF.
