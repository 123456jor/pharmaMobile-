# Actividad autónoma 10 - evidencias reales

Estudiante: Jorge Armando Quispe Huamán. Rama: `feature/auth-jwt-quispe`.

## Contenido

- 12 capturas de los seis escenarios: una respuesta de Postman y una pantalla Android por escenario.
- 3 capturas de almacenamiento: XML cifrado, Logcat saneado y persistencia tras cerrar el proceso.
- 4 capturas de cierre: almacén sin token, cliente sin Authorization y con 401, login limpio y bloqueo de retorno.
- 3 capturas principales de roles y 3 adicionales (productos vendedor/almacén y reapertura tras logout).
- `datos.json`, `resultados.json`, respuestas HTTP iniciales, logs reales y restauración final.

Las imágenes PNG son capturas originales. Los registros de ADB se extrajeron a archivos de texto y se abrieron en Bloc de notas para capturarlos. Se indica ese método en el PDF; no son capturas de terminal ni Device Explorer. Las repeticiones de servidor se ejecutaron en Postman y tienen hora propia. El reloj de Logcat es UTC; las horas de Windows se registran con UTC-05:00.

No se incluyen JWT completos. El XML muestra únicamente material cifrado. iOS quedó fuera del alcance solicitado y no se verificó.

## Herramientas debug reproducibles

Estas acciones existen únicamente en `androidApp/src/debug`. El manifiesto release generado no contiene el receiver ni sus acciones; ver `verificacion_release.txt`.

```powershell
adb shell am broadcast -n pe.edu.upeu.pharmamobil/.AuthDebugReceiver -a pe.edu.upeu.pharmamobil.DEBUG_ALTERAR_TOKEN
adb shell am broadcast -n pe.edu.upeu.pharmamobil/.AuthDebugReceiver -a pe.edu.upeu.pharmamobil.DEBUG_PETICION_PROTEGIDA
```

La primera cambia un carácter de la firma guardada. Para forzar la lectura del token modificado, se cerró y reabrió el proceso. La segunda usa el `HttpClient` real de Koin, sin construir otro cliente. Registra código HTTP, `message` y presencia del token sin imprimirlo.

La comprobación de caché mantuvo el PID Android 2627: GET 200 antes del logout, GET 401 sin Authorization después, sin reiniciar la app. Los dos logs se conservan completos.

La colección `postman/S10-Autonoma.postman_collection.json` permite repetir los seis escenarios. El login guarda el JWT en una variable de Postman; el archivo versionado no contiene tokens. La expiración y la deshabilitación requieren preparar y restaurar el backend/BD según la actividad.

## Generar el PDF

Con Python, ReportLab y Pillow disponibles, desde la raíz:

```powershell
python generar_actividad_autonoma.py --evid evidencias_autonoma
```

Salida: `output/pdf/S10_ActividadAutonoma_Quispe.pdf`.

## Estado final

`restauracion_final.json` confirma `expiracion-minutos: 60`, login HTTP 200 con 3600 segundos y admin/almacen/vendedor activos. Los puntos 1 a 11 de la lista de cotejo están documentados. El usuario autorizó el push y los commits de la actividad se publicaron en `feature/auth-jwt-quispe`. La verificación remota está registrada en `publicacion_github.json`.
