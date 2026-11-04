# Paso 6: Firebase detrás de la interfaz de dominio

## Configuración y compatibilidad

El estudiante creó PharmaMobil-JorgeQuispe, proyecto `pharmamobil-jorgequispe`, y registró Android con `pe.edu.upeu.pharmamobil`. `google-services.json` está junto a androidApp/build.gradle.kts, verificado contra el paquete real. Se mantiene local y excluido de Git por decisión expresa del estudiante. No se cambia applicationId ni se publican claves del archivo.

Se verificaron los POM oficiales de Maven Central y el README del tag GitLive v2.7.0: Crashlytics y Messaging 2.7.0 están publicados; dependen de Kotlin stdlib 2.2.21 y coroutines 1.10.2. El proyecto conserva Kotlin 2.4.10 y coroutines 1.10.2. No se cambian Kotlin, Compose, Ktor, Koin, AGP ni SQLDelight. Se incorporan Google Services 4.5.0 y Crashlytics Gradle 3.0.8, según la documentación oficial consultada, además de los dos módulos GitLive 2.7.0.

Fuentes primarias: https://github.com/GitLiveApp/firebase-kotlin-sdk/tree/v2.7.0, https://raw.githubusercontent.com/GitLiveApp/firebase-kotlin-sdk/v2.7.0/firebase-crashlytics/src/commonMain/kotlin/dev/gitlive/firebase/crashlytics/crashlytics.kt y https://firebase.google.com/docs/crashlytics/android/get-started.

## Interfaz y llamadas reales

`domain/port/Telemetria.kt` declara registrarError y registrarEvento sin imports Firebase. `data/firebase/TelemetriaFirebase.kt` usa realmente Firebase.crashlytics.log y recordException; no contiene métodos vacíos. Koin conserva una instancia única. El repositorio solo depende de Telemetria; registra inicio/fin de la sincronización, fallos transitorios/permanentes y errores inesperados. Un error de telemetría no sustituye el error original ni interrumpe el catálogo.

Los eventos se guardan como logs técnicos asociados a los reportes de Crashlytics; no se presentan como eventos de Analytics. La dependencia Messaging también es real: una comprobación debug obtiene su token únicamente para comprobar que no esté vacío y no lo imprime, persiste ni exporta. No se afirma que se haya enviado una notificación push desde la consola; el aviso de sincronización del paso 5 es una notificación local del sistema.

## Privacidad

PrivacidadTelemetria usa una lista cerrada de claves y valores técnicos: pantalla, operación, tipo, código HTTP y contadores numéricos acotados. Una clave permitida con un valor personal también se descarta. Nunca se envía el mensaje, causa, pila original, cabecera, cuerpo HTTP ni datos del producto. registrarError crea un ReporteTecnicoSeguro sin causa original. Los nombres de evento también están limitados a una lista fija.

FakeTelemetria existe solo en commonTest. Se prueba el uso de la interfaz en la ruta real de sincronización, el orden de eventos y la ausencia de valores privados. PrivacidadTelemetriaTest comprueba el filtrado de credenciales y valores no permitidos, además de la excepción sanitizada.

## Prueba real pendiente de registrar

El receptor ya existente del APK debug incorpora DEBUG_FIREBASE_NO_FATAL, que registra un evento técnico y un error controlado no fatal a través de la interfaz, y comprueba Messaging sin revelar su token. El resultado de compilación, Logcat y la observación de consola se incorporarán después de ejecutarlos. No se provoca un cierre de la app ni se afirma recepción remota antes de observarla.


## Resultado real verificado

BUILD SUCCESSFUL en 5 min 34 s durante la primera resolución del SDK; 115 pruebas, cero fallos, errores y omitidas. APK instalado conservando datos y permisos. Logcat del PID verificado de PharmaMobil confirmó FirebaseApp inicializado, Crashlytics Android 20.1.0, llamada real al no fatal y Messaging con token disponible sin imprimirlo. El log del emulador usa UTC; los metadatos de evidencia y la consola muestran America/Lima.

Se ejecutó el receptor debug, sin provocar cierres por excepción. Se reabrió la app para cerrar la sesión técnica y procesar el reporte. Se comprobó además un evento persistido y el encolado real a DataTransport. Durante el diagnóstico se repitió una vez el no fatal con el SDK ya inicializado; no se inventa una cantidad de eventos a partir de esas llamadas.

La consola del proyecto mostró **un evento recuperable** de `TelemetriaFirebase.registrarError`, excepción `ReporteTecnicoSeguro`, mensaje «Fallo técnico: controlado», versión 1.0, emulador Sdk_gphone16k_x86_64 con Android 17 y recepción del 4 de noviembre de 2026 a las 15:50:27. Se verificaron sus tres claves (operacion=prueba_no_fatal, pantalla=diagnostico, tipo_error=controlado) y el registro técnico asociado. Aquí sí hay recepción remota observada; no se deduce solo de que la llamada local no falló.

Evidencias: firebase/02_logcat_sdk_no_fatal.txt, 02_comprobacion_real.json, 03_logcat_real_android_studio.jpg, 04_estado_transporte.txt, 05_imports_sdk.txt y capturas 06–09 de la consola. La auditoría encontró los tres imports del SDK exclusivamente en data/firebase/TelemetriaFirebase.kt. El token FCM y el JSON de configuración no se exportaron.

Limitaciones: no se implementó una campaña push desde Firebase, porque la guía pide integrar Messaging y telemetría, no enviar una campaña. El aviso de sincronización sigue siendo local. iOS no se verificó por alcance expreso del estudiante. No se integró Analytics ni se presenta un log de Crashlytics como evento de Analytics.
