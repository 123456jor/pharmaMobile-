package pe.edu.upeu.pharmamobil.presentation.producto

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

@Composable
internal fun DetalleEnvioDialog(
    producto: ProductoUi, conectado: Boolean, cerrar: () -> Unit,
    corregir: () -> Unit, reintentar: () -> Unit, conciliar: (Long) -> Unit
) {
    var revisado by remember(producto.id) { mutableStateOf(false) }
    var idServidor by remember(producto.id) { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = cerrar,
        title = { Text("Qué pasó con el envío") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(producto.nombre)
                Text(producto.ultimoError ?: "Aún no se ha enviado")
                if (producto.requiereConciliacion) {
                    Text("Comprueba en PharmaSoft si el producto ya está registrado. Si aparece, indica su ID para vincularlo sin enviarlo otra vez.")
                    OutlinedTextField(value = idServidor, onValueChange = { idServidor = it },
                        label = { Text("ID encontrado en el servidor") }, singleLine = true)
                    OutlinedButton(onClick = { idServidor.toLongOrNull()?.let(conciliar) },
                        enabled = conectado && (idServidor.toLongOrNull() ?: 0) > 0) { Text("Conciliar producto") }
                }
                if (producto.requiereConciliacion || producto.puedeReintentar) {
                    Text("Reintentar habilita este producto y realiza un nuevo envío. Hazlo solo después de comprobar que no está en el servidor.")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = revisado, onCheckedChange = { revisado = it })
                        Text("He revisado el servidor y confirmo que no llegó")
                    }
                } else Text("Revisa y corrige este registro. Se conservará el mismo producto guardado en el dispositivo.")
                if (!conectado) Text("Conéctate para comprobar el servidor o reintentar")
            }
        },
        confirmButton = {
            if (producto.requiereConciliacion || producto.puedeReintentar) {
                Button(onClick = reintentar, enabled = revisado && conectado) { Text("Reintentar ahora") }
            } else Button(onClick = corregir) { Text("Revisar y corregir") }
        },
        dismissButton = { TextButton(onClick = cerrar) { Text("Cerrar") } }
    )
}
