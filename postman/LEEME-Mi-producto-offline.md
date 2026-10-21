# Tu prueba offline en Postman

## Primero, en Android

Crea **Prueba Offline Jorge 01** en modo avión, con precio **5.50**, stock **15** y categoría **1**. Recupera la conexión y pulsa **Sincronizar**.

## Después, en Postman

Colección: **S11 - Mi producto offline - Jorge**.

1. Abre **01 Iniciar sesion** y pulsa **Send**. Debe dar 200.
2. Abre **02 Encontrar mi producto** y pulsa **Send**. En **Test Results** deben aprobarse nombre único, ID positivo, precio, stock, categoría y estado. El ID se guarda automáticamente.
3. Abre **03 Ver mi producto** y pulsa **Send**. En **Body** verás solo tu producto, con el nombre y el ID positivo.

Toma tu captura con URL, **200 OK** y cuerpo del producto visibles.

Aún no creaste el producto: por eso no se declara aprobada su comprobación. Si consultas antes de crearlo/sincronizarlo, 02 indica que no existe y limpia el ID anterior. La colección no registra ni modifica productos; la creación debes hacerla en Android.

Para otro nombre/datos: abre la colección → **Variables** y cambia `nombreMiProducto`, `precioEsperado`, `stockEsperado`, `categoriaEsperada`. No escribas manualmente el ID: 02 lo obtiene del servidor. Usa un nombre distinto para cada nueva prueba para que se pueda comprobar que aparece una sola vez.
