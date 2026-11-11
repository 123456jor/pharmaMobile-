# Entrega Android - Sesión 14

Rama: `feature/builds-14`

Identidad: `pe.edu.upeu.pharmamobil`, versión `1` (`1.0`).

## Artefactos generados

- `output/entrega14/JorgeQuispe_PharmaMobil_Android.aab`: bundle release firmado para distribución.
- `output/entrega14/JorgeQuispe_PharmaMobil_Android_Revision.apk`: instalable release firmado para revisión.

Ambos se generaron el 11 de noviembre de 2026 con JDK 21, tareas `:androidApp:bundleRelease` y `:androidApp:assembleRelease`. R8/minificación estaba activada. Build: `BUILD SUCCESSFUL`, 2 min 16 s, 95 tareas (72 ejecutadas, 7 desde caché, 16 al día).

## Firma

Almacén personal JKS guardado fuera del repositorio: `C:\Users\joorg\pharmaMobile-signing\equipo7.jks`. `keystore.properties` local está excluido por `.gitignore`. No versionar ninguno de estos archivos.

Certificado autofirmado: Jorge Quispe, UPEU; RSA 2048; válido del 11-11-2026 al 29-03-2054. SHA-256: `65:5D:5A:F2:2D:A2:53:AF:20:FA:70:45:81:64:14:43:FA:20:FD:F4:4C:12:AE:6C:C0:AD:E3:8A:0C:DE:3B:52`.

`apksigner verify --verbose --print-certs` confirmó firma v2 y un firmante. `keytool -printcert -jarfile` confirmó que el AAB lleva el mismo certificado. No se incluye contraseña ni clave privada.

## Limitaciones de validación

El APK no se instaló en el emulador porque ya tenía la app debug instalada con otra firma; `adb install -r` exigiría desinstalarla y borrar sus datos. No se borraron datos. Por eso el recorrido de la versión minificada aún no está verificado.

El backend Android actualmente apunta a `http://10.0.2.2:8080/api/v1/`, que solo es alcanzable desde el emulador. Antes del recorrido conectado en un teléfono físico habrá que compilar con una dirección de backend accesible desde la red Wi-Fi del teléfono y permitir ese puerto en el firewall. No usar `10.0.2.2` en el teléfono. La dirección concreta dependerá de la red de la prueba; no se inventó.

Las pruebas en teléfonos físicos están pendientes de Jorge. Registrar modelo, versión Android, instalación, recorrido, resultados y defectos reales en la matriz antes de presentar. iOS queda excluido por indicación de Jorge.

R8 imprimió avisos de que su lector de metadatos Kotlin es anterior a la versión Kotlin del proyecto. El build finalizó correctamente, pero el comportamiento minificado debe probarse con el APK instalado antes de declarar ese punto verificado. No se cambiaron versiones de dependencias.
