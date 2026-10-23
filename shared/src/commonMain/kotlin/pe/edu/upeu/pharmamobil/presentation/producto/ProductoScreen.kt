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
fun ProductoScreen(viewModel: ProductoViewModel, modifier: Modifier = Modifier, permiteEscritura: Boolean = true) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val ocupada = uiState.operacion is ProductoUiState.Operacion.EnCurso
    var ahora by remember { mutableStateOf(Clock.System.now().toEpochMilliseconds()) }
    LaunchedEffect(uiState.ultimaSincronizacion) {
        while (true) {
            ahora = Clock.System.now().toEpochMilliseconds()
            delay(60_000) // Actualiza el texto visible; no realiza peticiones HTTP.
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Guardado en este dispositivo", modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium)
            OutlinedButton(onClick = viewModel::sincronizar, enabled = !uiState.sincronizando && !ocupada) {
                Text(if (uiState.sincronizando) "Sincronizando…" else "Sincronizar")
            }
        }
        Text(formatearAntiguedad(ahora, uiState.ultimaSincronizacion),
            style = MaterialTheme.typography.bodySmall)
        uiState.avisoRed?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        if (permiteEscritura) FormularioProductoCard(
            formulario = uiState.formulario,
            ocupado = ocupada,
            editando = uiState.productoEditandoId != null,
            onNombreChange = viewModel::onNombreChange,
            onPrecioChange = viewModel::onPrecioChange,
            onStockChange = viewModel::onStockChange,
            onCategoriaIdChange = viewModel::onCategoriaIdChange,
            onGuardar = if (uiState.productoEditandoId == null) viewModel::registrar else viewModel::actualizar,
            onCancelar = viewModel::cancelarEdicion
        )

        uiState.mensajeExito?.let { MensajeExito(it) }
        (uiState.operacion as? ProductoUiState.Operacion.Fallida)?.let {
            Text(it.mensaje, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        EncabezadoInventario(uiState.fase)
        Box(Modifier.fillMaxWidth().weight(1f)) {
            when (val fase = uiState.fase) {
                ProductoUiState.Fase.Cargando -> Column(
                    Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator()
                    Text("Cargando inventario…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                ProductoUiState.Fase.SinProductos -> EstadoVacio(
                    icono = Icons.Default.Inventory2,
                    titulo = "Todavía no hay productos",
                    descripcion = "Registra el primero con el formulario de arriba.",
                    modifier = Modifier.align(Alignment.Center)
                )
                is ProductoUiState.Fase.ConProductos -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(fase.productos, key = { it.id }) { producto ->
                        ProductoItem(
                            producto = producto,
                            ocupado = ocupada || uiState.sincronizando || uiState.avisoRed != null,
                            permiteEscritura = permiteEscritura,
                            onCompartir = { viewModel.compartir(producto) },
                            onEditar = { viewModel.comenzarEdicion(producto) },
                            onEliminar = { viewModel.eliminar(producto.id) }
                        )
                    }
                }
                is ProductoUiState.Fase.Error -> EstadoVacio(
                    icono = Icons.Default.CloudOff,
                    titulo = "No pudimos cargar el inventario",
                    descripcion = fase.mensaje,
                    colorIcono = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center),
                    accion = { FilledTonalButton(onClick = viewModel::sincronizar) { Text("Reintentar") } }
                )
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
    producto: ProductoUi,
    ocupado: Boolean,
    permiteEscritura: Boolean,
    onCompartir: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer) {
                Icon(Icons.Default.Medication, null, Modifier.padding(8.dp).size(20.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(producto.nombre, style = MaterialTheme.typography.titleSmall)
                Text("${producto.precio}  ·  ${producto.stock}", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Categoría ${producto.categoriaId}", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(if (producto.pendiente) "Pendiente de sincronizar · ID ${producto.id}" else "ID ${producto.id}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (producto.pendiente) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (producto.requiereReposicion) Surface(shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer) {
                Text("Reponer", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
            IconButton(onClick = onCompartir) {
                Icon(Icons.Default.Share, contentDescription = "Compartir ${producto.nombre}")
            }
            if (permiteEscritura) {
                IconButton(onClick = onEditar, enabled = !ocupado && !producto.pendiente) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar ${producto.nombre}")
                }
                IconButton(onClick = onEliminar, enabled = !ocupado && !producto.pendiente) {
                    Icon(Icons.Default.Delete, contentDescription = "Dar de baja ${producto.nombre}")
                }
            }
        }
    }
}
