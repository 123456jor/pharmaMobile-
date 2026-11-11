# Sesión 14 — Build Android firmado

Rama: `feature/builds-14`

Aplicación: `pe.edu.upeu.pharmamobil`, versión `1` (`1.0`).
Alcance: Android; iOS queda fuera de esta práctica.

## Archivos de entrega

- `output/entrega14/JorgeQuispe_PharmaMobil_Android.aab`: bundle release firmado.
- `output/entrega14/JorgeQuispe_PharmaMobil_Android_Revision.apk`: APK release firmado para instalar.
- `output/entrega14/evidencias/emulador_release.png`: captura real del emulador limpio con la pantalla de inicio de sesión.

El APK y el AAB se generaron el 11 de noviembre de 2026 con JDK 21, R8/minificación habilitada y las tareas `:androidApp:bundleRelease` y `:androidApp:assembleRelease`. Última compilación: `BUILD SUCCESSFUL` (95 tareas; 27 ejecutadas y 68 al día).

## Firma

El almacén personal está fuera del repositorio en `C:\Users\joorg\pharmaMobile-signing\equipo7.jks`. `keystore.properties` también permanece local y excluido por `.gitignore`; no se publican contraseñas ni la clave privada.

El APK fue verificado con `apksigner`: firma v2 válida, un firmante, RSA de 2048 bits, certificado de Jorge Quispe. SHA-256 del certificado: `65:5D:5A:F2:2D:A2:53:AF:20:FA:70:45:81:64:14:43:FA:20:FD:F4:4C:12:AE:6C:C0:AD:E3:8A:0C:DE:3B:52`. El AAB se firma con el mismo almacén.

## Comprobaciones realizadas en este equipo

- Se instaló el APK minificado en `emulator-5556`, un AVD aislado, y abrió `MainActivity` en la pantalla de inicio de sesión. El AVD `emulator-5554` con los datos previos se dejó intacto.
- El emulador alcanzó por TCP el puerto `10.0.2.2:8080`.
- Oracle local quedó saludable; Flyway validó tres migraciones y confirmó el esquema en versión 3, al día.
- El backend respondió `200` en `/api/health`. La petición sin token a `/api/v1/productos` respondió `401`, como exige la autenticación.
- En el logcat consultado después del arranque no aparecieron excepciones fatales, `ClassNotFoundException`, `NoSuchMethodError` ni errores de serialización.

Estas comprobaciones confirman la firma, instalación, apertura y conectividad del backend desde el emulador. No prueban un inicio de sesión válido ni el recorrido completo de la app con credenciales reales.

## URL del backend y teléfonos físicos

La build entregada apunta a `http://10.0.2.2:8080/api/v1/`, dirección que funciona dentro del emulador. El build permite cambiarla sin guardar una IP en Git, por ejemplo:

```powershell
.\gradlew.bat :androidApp:assembleRelease "-PpharmasoftApiBaseUrl=http://IP_DEL_EQUIPO:8080/api/v1/"
```

Pero la política Android actual de tráfico claro solo autoriza el host `10.0.2.2`. Por eso cambiar la URL, por sí solo, no habilita la conexión HTTP desde un teléfono físico. Para completar la prueba conectada hay que autorizar una excepción acotada a la IP LAN exacta del servidor o servir la API por HTTPS. No se amplió la excepción: el intento de permitir HTTP para cualquier host fue rechazado por la revisión automática de seguridad.

## Pendiente de validación física

Jorge indicó que probará la instalación en dos teléfonos Android. No se presentaron capturas simuladas como si fueran teléfonos reales. Para cerrar esa parte, instalar el APK sin Android Studio en ambos teléfonos y adjuntar fotos/capturas auténticas de:

1. Instalación y versión de Android/modelo de cada equipo.
2. Login correcto e incorrecto.
3. CRUD, rechazo de validación y operación denegada por rol.
4. Modo avión: consulta y creación pendiente; reconexión y sincronización sin duplicados.
5. Notificaciones conectadas, denegadas y revocadas desde Ajustes.
6. Cerrar sesión, reabrir y mostrar el estado final.

Registrar el resultado de cada paso por teléfono y cualquier defecto observado. Hasta completar esa matriz con evidencia real, la práctica no está validada al 100 % en dispositivos físicos.

## Observación de minificación

R8 emitió avisos repetidos de que su lector de metadatos Kotlin es anterior al Kotlin del proyecto. La compilación finalizó correctamente y el APK se instaló y abrió; falta recorrer autenticación y funciones conectadas con R8 activo. No se actualizaron dependencias en esta sesión.
