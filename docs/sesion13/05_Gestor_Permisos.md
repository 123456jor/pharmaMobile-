# Paso 5: permiso nativo de notificaciones

## Propósito y archivos

La capacidad elegida es NOTIFICACIONES: un aviso real de Android resume los productos enviados, por enviar y que requieren atención al terminar una sincronización. No se solicita cámara ni ubicación porque no se usan. Sus nombres se contemplan en el contrato común para futuras capacidades, sin declarar ni pedir permisos innecesarios.

`domain/permiso/GestorPermisos.kt` declara la interfaz, Permiso, EstadoPermiso y dos funciones puras: resolverEstadoPermiso y accionPermiso. El dominio no importa Android. La función de presentación distingue explicar, solicitar, continuar e ir a Ajustes y se prueba sin emulador.

`data/permiso/GestorPermisosAndroid.kt` implementa los nombres de sistema: CAMERA, ACCESS_FINE_LOCATION y POST_NOTIFICATIONS (solo API 33+). Antes de Android 13 este último no requiere diálogo y se considera concedido. Se guarda pedido_<permiso> para distinguir primera solicitud de denegación definitiva cuando rationale es false. Se verifica el permiso del sistema cada vez, sin almacenar una concesión ficticia.

`PuentePermisos` registra ActivityResultContracts.RequestPermission en onCreate, antes de STARTED, y espera mediante CompletableDeferred. Un Mutex evita diálogos simultáneos. La Activity se referencia débilmente; al destruirla se cancela la espera, se desregistra el launcher y el gestor descarta el puente si sigue siendo el actual. El ViewModel no recibe ninguna Activity.

`PermisosViewModel` impide solicitar antes de explicar. `PermisosCard` explica en contexto y ofrece Activar avisos o Ir a Ajustes. Al volver a la app consulta de nuevo el estado real en ON_RESUME. No bloquea login, consultas ni registro por negar un permiso opcional.

`AvisadorSincronizacionAndroid` crea un canal y una notificación real. Comprueba el permiso y que las notificaciones estén habilitadas; si se revoca entre comprobar y notificar, no interrumpe la sincronización. El dominio solo conoce AvisadorSincronizacion. Se inyectan las implementaciones Android por Koin. POST_NOTIFICATIONS se declaró en el manifiesto.

## Pruebas y límites

PermisosTest verifica la tabla de tres estados y las acciones de presentación; un FakeGestorPermisos solo en commonTest acredita que no se solicita antes de explicar y que Concedido, Denegado y DenegadoParaSiempre no se sustituyen entre sí. El grafo de pruebas reemplaza permisos y avisos por dobles de prueba, sin cambiar la app real. Las capturas y resultados se agregan tras ejecutar los tres estados en Android.

iOS sigue excluido por indicación del estudiante. No se crea un gestor iOS que simule concesiones ni se afirma que se haya compilado o probado allí.

Referencia: https://developer.android.com/training/permissions/requesting y https://firebase.google.com/docs/cloud-messaging/android/get-started (permiso de notificaciones Android 13+).


## Resultado real

BUILD SUCCESSFUL en 30 s; 112 pruebas, cero fallos, errores y omitidas. APK instalado con `adb install -r`, conservando datos. En el primer diálogo del sistema se eligió Don't allow: la app mantuvo Denegado y permitió repetir tras otra explicación. Se rechazó el segundo diálogo: mostró DenegadoParaSiempre y el botón Ir a Ajustes abrió App info real. En Notifications se activó All PharmaMobil notifications; al regresar, ON_RESUME actualizó a Concedido. Se pulsó Sincronizar y el panel del sistema mostró el aviso «Sincronización terminada» con los contadores reales.

Capturas originales: 11a (estado inicial), 11b (explicación), 11c (diálogo Android), 11d (denegado), 11e (definitivo), 11f (App info), 11g (concesión en Settings), 11h (concedido en la app) y 11i (notificación real). No se usó pm grant ni se borraron datos para simular el resultado. Se corrigió durante compilación la constante de Ajustes: pertenece a android.provider.Settings, no a Intent.

No se afirma una prueba real en Android anterior a API 33 ni en teléfono físico: la regla de degradación está implementada, mientras el recorrido se ejecutó en el emulador API 37.1.
