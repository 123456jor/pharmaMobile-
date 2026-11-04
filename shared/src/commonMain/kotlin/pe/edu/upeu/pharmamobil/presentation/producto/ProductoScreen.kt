package pe.edu.upeu.pharmamobil.presentation.producto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import kotlin.time.Clock
import pe.edu.upeu.pharmamobil.domain.model.formatearAntiguedad
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upeu.pharmamobil.presentation.components.EstadoVacio
import pe.edu.upeu.pharmamobil.presentation.components.MensajeExito
import pe.edu.upeu.pharmamobil.presentation.components.ValidatedTextField

@Composable
fun ProductoScreen(viewModel: ProductoViewModel, modifier: Modifier = Modifier, permiteEscritura: Boolean = true,
    soloConsulta: Boolean = false) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sync by viewModel.sincronizacion.collectAsStateWithLifecycle()
    val ocupada = uiState.operacion is ProductoUiState.Operacion.EnCurso
    var ahora by remember { mutableStateOf(Clock.System.now().toEpochMilliseconds()) }
    var detalle by remember { mutableStateOf<ProductoUi?>(null) }
    val lista = androidx.compose.foundation.lazy.rememberLazyListState()
    LaunchedEffect(uiState.productoEditandoId) {
        if (uiState.productoEditandoId != null) lista.animateScrollToItem(0)
    }
    LaunchedEffect(sync.ultimaSync) {
        while (true) {
            ahora = Clock.System.now().toEpochMilliseconds()
            delay(60_000) // Solo renueva el texto de antigüedad; no consulta la red.
        }
    }
    detalle?.let { producto ->
        DetalleEnvioDialog(producto, sync.conectado && !soloConsulta,
            cerrar = { detalle = null },
            corregir = { detalle = null; viewModel.comenzarEdicion(producto) },
            reintentar = { detalle = null; viewModel.reintentar(producto.id, true) },
            conciliar = { id -> detalle = null; viewModel.conciliar(producto.id, id) })
    }
    // Un único contenedor desplazable deja visibles lista y acciones incluso con teclado.
    LazyColumn(modifier.fillMaxSize().padding(horizontal = 16.dp),
        state = lista,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)) {
        item(key = "estado-sync") {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (sync.enCurso) "Sincronizando productos…" else
                        "${if (sync.conectado) "Conectado" else "Sin conexión"} · ${formatearAntiguedad(ahora, sync.ultimaSync)}",
                        style = MaterialTheme.typography.titleSmall)
                    Text("Por enviar: ${sync.pendientes} · Requieren atención: ${sync.fallidas}",
                        style = MaterialTheme.typography.bodyMedium)
                    if (sync.fallidas > 0) Text("Revisa las filas marcadas y pulsa Ver qué pasó",
                        color = MaterialTheme.colorScheme.error)
                    if (!sync.conectado) Text("Puedes consultar lo guardado. Los nuevos productos quedan pendientes.",
                        style = MaterialTheme.typography.bodySmall)
                    OutlinedButton(onClick = viewModel::sincronizar,
                        enabled = !soloConsulta && !sync.enCurso && !ocupada) {
                        Text(if (sync.enCurso) "Sincronizando…" else "Sincronizar")
                    }
                }
            }
        }
        uiState.avisoRed?.let { aviso -> item { Text(aviso, color = MaterialTheme.colorScheme.error) } }
        if (permiteEscritura) item(key = "formulario") {
            FormularioProductoCard(uiState.formulario, ocupada || sync.enCurso,
                uiState.productoEditandoId != null, viewModel::onNombreChange, viewModel::onPrecioChange,
                viewModel::onStockChange, viewModel::onCategoriaIdChange,
                if (uiState.productoEditandoId == null) viewModel::registrar else viewModel::actualizar,
                viewModel::cancelarEdicion)
        }
        uiState.mensajeExito?.let { item { MensajeExito(it) } }
        (uiState.operacion as? ProductoUiState.Operacion.Fallida)?.let { error ->
            item { Text(error.mensaje, color = MaterialTheme.colorScheme.error) }
        }
        item { EncabezadoInventario(uiState.fase) }
        when (val fase = uiState.fase) {
            ProductoUiState.Fase.Cargando -> item { CircularProgressIndicator(); Text("Cargando inventario…") }
            ProductoUiState.Fase.SinProductos -> item {
                EstadoVacio(Icons.Default.Inventory2, "Todavía no hay productos",
                    if (soloConsulta) "No hay datos guardados. Conéctate e inicia sesión para sincronizar."
                    else "Registra el primero con el formulario de arriba.")
            }
            is ProductoUiState.Fase.ConProductos -> items(fase.productos, key = { it.id }) { producto ->
                ProductoItem(producto, ocupada || sync.enCurso, permiteEscritura,
                    onCompartir = { viewModel.compartir(producto) },
                    onEditar = { viewModel.comenzarEdicion(producto) },
                    onEliminar = { viewModel.eliminar(producto.id) },
                    conectado = sync.conectado,
                    onDetalle = { detalle = producto })
            }
            is ProductoUiState.Fase.Error -> item {
                Text(fase.mensaje, color = MaterialTheme.colorScheme.error)
                FilledTonalButton(onClick = viewModel::sincronizar) { Text("Reintentar") }
            }
        }
    }
}

@Composable
private fun FormularioProductoCard(
    formulario: FormularioProducto,
    ocupado: Boolean,
    editando: Boolean,
    onNombreChange: (String) -> Unit,
    onPrecioChange: (String) -> Unit,
    onStockChange: (String) -> Unit,
    onCategoriaIdChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(if (editando) "Actualizar producto" else "Registrar producto", style = MaterialTheme.typography.titleMedium)
            ValidatedTextField(
                value = formulario.nombre, onValueChange = onNombreChange, label = "Nombre",
                error = formulario.nombreError, leadingIcon = Icons.Default.Medication,
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ValidatedTextField(
                    value = formulario.precio, onValueChange = onPrecioChange, label = "Precio",
                    error = formulario.precioError, ayuda = "En soles", keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )
                ValidatedTextField(
                    value = formulario.stock, onValueChange = onStockChange, label = "Stock",
                    error = formulario.stockError, ayuda = "Unidades", keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }
            ValidatedTextField(
                value = formulario.categoriaId, onValueChange = onCategoriaIdChange,
                label = "ID de categoría existente", error = formulario.categoriaIdError,
                ayuda = "Consulta el ID en PharmaSoft", keyboardType = KeyboardType.Number,
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onGuardar, enabled = !ocupado, modifier = Modifier.weight(1f)) {
                    Text(if (ocupado) "Guardando…" else if (editando) "Actualizar" else "Registrar")
                }
                if (editando) OutlinedButton(onClick = onCancelar, enabled = !ocupado) { Text("Cancelar") }
            }
        }
    }
}

@Composable
private fun EncabezadoInventario(fase: ProductoUiState.Fase) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("Inventario", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        if (fase is ProductoUiState.Fase.ConProductos) {
            val cantidad = fase.productos.size
            Text(if (cantidad == 1) "1 producto" else "$cantidad productos", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun ProductoItem(
    producto: ProductoUi, ocupado: Boolean, permiteEscritura: Boolean,
    onCompartir: () -> Unit, onEditar: () -> Unit, onEliminar: () -> Unit,
    conectado: Boolean, onDetalle: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(producto.nombre, style = MaterialTheme.typography.titleSmall)
            Text("${producto.precio} · ${producto.stock} · Categoría ${producto.categoriaId}",
                style = MaterialTheme.typography.bodyMedium)
            Text(when (producto.estadoEnvio) {
                EstadoEnvioUi.Sincronizado -> "✓ Sincronizado · ID ${producto.id}"
                EstadoEnvioUi.Pendiente -> "↑ Pendiente de envío · ID ${producto.id} · ${producto.intentos}/5"
                EstadoEnvioUi.Fallido -> "! Requiere atención · ID ${producto.id}"
            }, color = if (producto.pendiente) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge)
            if (producto.requiereReposicion) Text("Reponer stock", color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onCompartir) { Icon(Icons.Default.Share, "Compartir ${producto.nombre}") }
                if (permiteEscritura) {
                    if (producto.pendiente) androidx.compose.material3.TextButton(
                        onClick = onDetalle, enabled = !ocupado) { Text("Ver qué pasó") }
                    else {
                        IconButton(onClick = onEditar, enabled = !ocupado && conectado) {
                            Icon(Icons.Default.Edit, "Editar ${producto.nombre}")
                        }
                        IconButton(onClick = onEliminar, enabled = !ocupado && conectado) {
                            Icon(Icons.Default.Delete, "Dar de baja ${producto.nombre}")
                        }
                    }
                }
            }
        }
    }
}
