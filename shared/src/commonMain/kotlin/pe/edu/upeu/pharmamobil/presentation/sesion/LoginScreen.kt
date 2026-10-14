package pe.edu.upeu.pharmamobil.presentation.sesion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(estado: SesionUiState, viewModel: SesionViewModel) {
    var mostrarClave by remember { mutableStateOf(false) }
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(Icons.Default.LocalPharmacy, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Iniciar sesión", style = MaterialTheme.typography.headlineSmall)
                estado.aviso?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                OutlinedTextField(
                    value = estado.usuario,
                    onValueChange = viewModel::onUsuarioChange,
                    label = { Text("Usuario") },
                    singleLine = true,
                    enabled = !estado.cargando,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = estado.clave,
                    onValueChange = viewModel::onClaveChange,
                    label = { Text("Contraseña") },
                    singleLine = true,
                    enabled = !estado.cargando,
                    visualTransformation = if (mostrarClave) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { mostrarClave = !mostrarClave }) {
                            Icon(if (mostrarClave) Icons.Default.VisibilityOff else Icons.Default.Visibility, "Mostrar u ocultar contraseña")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                estado.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Button(
                    onClick = viewModel::iniciarSesion,
                    enabled = !estado.cargando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (estado.cargando) CircularProgressIndicator() else Text("Ingresar")
                }
            }
        }
    }
}
