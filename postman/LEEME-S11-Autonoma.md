# Postman: actividad autónoma 11

Colección: S11-Autonoma.postman_collection.json. Corresponde a la evidencia real del producto 83 (AA_Autonoma_Jorge_23Oct, precio 8.50, stock 17, categoría 1).

1. Iniciar Docker/Oracle y el backend PharmaSoft. Para Postman no necesitas tener abierto el emulador.
2. Importar la colección. Configurar claveAdmin en el entorno de tu sesión de Postman. No guardar contraseñas ni tokens en Git.
3. Ejecutar 01 Login: debe responder 200 y guardar JWT en la colección.
4. Ejecutar 02 Encontrar: revisa el listado completo, nombre, precio, stock e ID positivo. No crea un producto ni sustituye el registro desde Android.
5. Ejecutar 03 Ver: consulta específicamente /productos/83 y contrasta sus datos. La captura de la actividad está en docs/s11/capturas/esc3_postman_producto_83.jpg.

Para otro producto, usar la colección anterior S11-Mi-producto-offline, cambiar sus variables y ejecutar Login, Encontrar y Ver en orden. No aceptar un 404 ni cambiar las pruebas para hacerlo pasar. Si falta el producto, sincronizar primero en Android. Si devuelve 401, repetir login; si devuelve 403, revisar el permiso del usuario.

La primera consulta de la actividad se intentó sobre una solicitud anterior con una variable vacía y luego una edición inconsistente de su URL; produjo 404 y 400. No se usaron esas respuestas como evidencia de éxito. Se importó una colección nueva con URL completa verificada; el GET devolvió 200 con el producto 83.
