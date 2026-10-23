package pe.edu.upeu.pharmamobil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.time.Clock
import org.koin.core.parameter.parametersOf
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

import pe.edu.upeu.pharmamobil.navigation.Screen
import pe.edu.upeu.pharmamobil.presentation.cliente.ClienteScreen
import pe.edu.upeu.pharmamobil.presentation.components.EstadoVacio
import pe.edu.upeu.pharmamobil.presentation.inicio.InicioScreen
import pe.edu.upeu.pharmamobil.presentation.producto.ProductoScreen
import pe.edu.upeu.pharmamobil.presentation.sesion.LoginScreen
import pe.edu.upeu.pharmamobil.presentation.sesion.SesionViewModel
import pe.edu.upeu.pharmamobil.theme.PharmaMobilTheme

/** Una sola fuente para el menu lateral y el titulo de la barra superior. */
private data class Destino(
    val screen: Screen,
    val titulo: String,
    val icono: ImageVector
)

private val DESTINOS = listOf(
    Destino(Screen.Inicio, "Inicio", Icons.Default.Home),
    Destino(Screen.Productos, "Productos", Icons.Default.Medication),
    Destino(Screen.Clientes, "Clientes", Icons.Default.Person),
    Destino(Screen.Pedidos, "Pedidos", Icons.Default.ShoppingCart)
)


private val ScreenSaver = Saver<Screen, Int>(
    save = { pantalla ->
        DESTINOS.indexOfFirst { it.screen == pantalla }
    },
    restore = { indice ->
        DESTINOS[indice].screen
    }
)

@Composable
fun App() = KoinContext {
    val sesionViewModel: SesionViewModel = koinViewModel()
    val sesionState by sesionViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { sesionViewModel.cargarSesionGuardada() }
    LaunchedEffect(sesionState.sesion?.expiracionEpochSegundos) {
        val vence = sesionState.sesion?.expiracionEpochSegundos ?: return@LaunchedEffect
        if (vence > 0) {
            delay((vence * 1000 - Clock.System.now().toEpochMilliseconds()).coerceAtLeast(0) + 1)
            sesionViewModel.cargarSesionGuardada()
        }
    }
    // Al recuperar conexión, auth/me pide un nuevo login. No sincroniza con credenciales vencidas.
    LaunchedEffect(sesionState.consultaLocal) {
        while (sesionState.consultaLocal) {
            delay(5_000)
            sesionViewModel.cargarSesionGuardada()
        }
    }

    var pantallaActual by rememberSaveable(stateSaver = ScreenSaver) {
        mutableStateOf<Screen>(Screen.Inicio)
    }

    var darkTheme by rememberSaveable {
        mutableStateOf(false)
    }

    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )

    val scope = rememberCoroutineScope()
    LaunchedEffect(sesionState.sesion) {
        if (sesionState.sesion == null) pantallaActual = Screen.Inicio
    }
    LaunchedEffect(sesionState.sesion?.permisos) {
        val permisos = sesionState.sesion?.permisos.orEmpty()
        if (pantallaActual == Screen.Clientes && "CLIENTES_LEER" !in permisos) pantallaActual = Screen.Productos
        if (pantallaActual == Screen.Pedidos && "VENTAS_REGISTRAR" !in permisos) pantallaActual = Screen.Productos
    }

    PharmaMobilTheme(
        darkTheme = darkTheme
    ) {
        val sesion = sesionState.sesion
        if (sesionState.consultaLocal) {
            Column(Modifier.fillMaxSize().padding(top = 40.dp)) {
                Text(if (sesionState.sesionLocalExpirada)
                    "Sesión vencida. Sin conexión: solo consulta local."
                    else "Sin conexión: solo consulta de datos guardados.",
                    modifier = Modifier.padding(horizontal = 16.dp))
                androidx.compose.material3.TextButton(onClick = sesionViewModel::irAlLogin) {
                    Text("Iniciar sesión al reconectar")
                }
                ProductoScreen(viewModel = koinViewModel(key = "productos-locales",
                    parameters = { parametersOf(true) }), permiteEscritura = false, soloConsulta = true)
            }
        } else if (sesion == null) {
            if (sesionState.cargando) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { androidx.compose.material3.CircularProgressIndicator() }
            } else {
                LoginScreen(sesionState, sesionViewModel)
            }
        } else {
        val destinosDisponibles = DESTINOS.filter { destino ->
            when (destino.screen) {
                Screen.Inicio, Screen.Productos -> true
                Screen.Clientes -> sesion.puede("CLIENTES_LEER")
                Screen.Pedidos -> sesion.puede("VENTAS_REGISTRAR")
            }
        }
        ModalNavigationDrawer(

            drawerState = drawerState,

            drawerContent = {

                ModalDrawerSheet {

                    DrawerHeader()

                    HorizontalDivider()

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text("Sesión: ${sesion.nombreCompleto}", modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp), style = MaterialTheme.typography.labelMedium)
                    destinosDisponibles.forEach { destino ->

                        NavigationDrawerItem(
                            label = {
                                Text(destino.titulo)
                            },
                            selected = pantallaActual == destino.screen,
                            onClick = {

                                pantallaActual = destino.screen

                                scope.launch {
                                    drawerState.close()
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = destino.icono,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.padding(
                                NavigationDrawerItemDefaults.ItemPadding
                            )
                        )
                    }

                    Spacer(
                        modifier = Modifier.weight(1f)
                    )

                    HorizontalDivider()

                    ModoOscuro(
                        activo = darkTheme,
                        onCambiar = { darkTheme = it }
                    )
                    androidx.compose.material3.TextButton(
                        onClick = { sesionViewModel.cerrarSesion() },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    ) { Icon(Icons.Default.Logout, null); Text("Cerrar sesión") }
                }
            }
        ) {

            Scaffold(

                topBar = {

                    TopAppBar(

                        title = {
                            Text(text = tituloDe(pantallaActual))
                        },

                        actions = {
                            IconButton(onClick = { sesionViewModel.cerrarSesion() }) {
                                Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión")
                            }
                        },

                        navigationIcon = {

                            IconButton(
                                onClick = {

                                    scope.launch {

                                        drawerState.open()
                                    }
                                }
                            ) {

                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Abrir menú"
                                )
                            }
                        },

                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

            ) { paddingValues ->

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {

                    sesionState.error?.let { mensaje ->
                        Text(mensaje, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.TopCenter).padding(12.dp))
                    }

                    when (pantallaActual) {

                        Screen.Inicio ->
                            InicioScreen(
                                permisos = sesion.permisos,
                                onNavegar = { destino ->
                                    if (destino in destinosDisponibles.map { it.screen }) pantallaActual = destino
                                }
                            )

                        Screen.Productos ->
                            ProductoScreen(
                                viewModel = koinViewModel(),
                                permiteEscritura = sesion.puede("CATALOGO_ESCRIBIR")
                            )

                        Screen.Clientes ->
                            ClienteScreen(
                                viewModel = koinViewModel()
                            )

                        Screen.Pedidos ->
                            EstadoVacio(
                                icono = Icons.Default.ShoppingCart,
                                titulo = "Pedidos en construcción",
                                descripcion = "Este módulo llega en una próxima sesión del curso.",
                                modifier = Modifier.align(Alignment.Center)
                            )
                    }
                }
            }
        }
        }
    }
}


@Composable
private fun DrawerHeader() {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {

            Icon(
                imageVector = Icons.Default.LocalPharmacy,
                contentDescription = null,
                modifier = Modifier
                    .padding(10.dp)
                    .size(28.dp)
            )
        }

        Column {

            Text(
                text = "PharmaMobil",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Gestión farmacéutica",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
private fun ModoOscuro(
    activo: Boolean,
    onCambiar: (Boolean) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 28.dp,
                vertical = 16.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Icon(
            imageVector = Icons.Default.DarkMode,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Modo oscuro",
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = activo,
            onCheckedChange = onCambiar
        )
    }
}


private fun tituloDe(
    screen: Screen
): String {

    return DESTINOS.first { it.screen == screen }.titulo
}
