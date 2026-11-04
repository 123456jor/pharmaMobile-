package pe.edu.upeu.pharmamobil.presentation.permiso

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.pharmamobil.domain.permiso.EstadoPermiso

@Composable
fun PermisosCard(viewModel: PermisosViewModel = koinViewModel()) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val ciclo = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(ciclo) {
        val observador = LifecycleEventObserver { _, evento -> if (evento == Lifecycle.Event.ON_RESUME) viewModel.comprobar() }
        ciclo.addObserver(observador); viewModel.comprobar()
        onDispose { ciclo.removeObserver(observador) }
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Avisos de sincronización", style = MaterialTheme.typography.titleSmall)
            Text(when (estado.estado) {
                EstadoPermiso.Concedido -> "Permiso concedido. Recibirás un aviso al terminar la sincronización."
                EstadoPermiso.Denegado -> "Permiso denegado. Puedes activarlo para recibir el resultado de la sincronización. Es opcional."
                EstadoPermiso.DenegadoParaSiempre -> "Permiso denegado permanentemente. Puedes activarlo desde Ajustes. La app sigue funcionando."
            }, style = MaterialTheme.typography.bodySmall)
            estado.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (estado.estado != EstadoPermiso.Concedido) OutlinedButton(onClick = viewModel::preparar,
                enabled = !estado.ocupado) {
                Text(if (estado.estado == EstadoPermiso.DenegadoParaSiempre) "Ir a Ajustes" else "Activar avisos")
            }
        }
    }
    if (estado.explicando) AlertDialog(onDismissRequest = viewModel::cancelarExplicacion,
        title = { Text("Recibir avisos de sincronización") },
        text = { Text("PharmaMobil puede avisarte cuando termine de enviar productos o haya cambios que requieran atención. Android te pedirá permiso para mostrar esos avisos. Puedes rechazarlo y seguir usando la app.") },
        confirmButton = { Button(onClick = viewModel::solicitar) { Text("Continuar y solicitar") } },
        dismissButton = { TextButton(onClick = viewModel::cancelarExplicacion) { Text("Ahora no") } })
}
