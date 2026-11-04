package pe.edu.upeu.pharmamobil.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pe.edu.upeu.pharmamobil.data.local.ProductoLocal
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.remote.ejecutarLlamada
import pe.edu.upeu.pharmamobil.domain.auth.SesionManager
import pe.edu.upeu.pharmamobil.domain.error.ErrorApi
import pe.edu.upeu.pharmamobil.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.model.ResumenSync
import pe.edu.upeu.pharmamobil.domain.model.ResultadoEnvio
import pe.edu.upeu.pharmamobil.domain.model.ErrorSesionException
import pe.edu.upeu.pharmamobil.domain.model.ErrorSesion
import pe.edu.upeu.pharmamobil.domain.model.esperaEntreIntentos
import pe.edu.upeu.pharmamobil.domain.port.Conectividad
import pe.edu.upeu.pharmamobil.data.remote.EnvioNoIniciadoException
import pe.edu.upeu.pharmamobil.data.remote.resultadoDeError
import pe.edu.upeu.pharmamobil.data.remote.resultadoPostIncierto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

class ProductoRepositorioOffline(
    private val local: ProductoLocal,
    private val remoto: ProductoApi,
    private val sesionManager: SesionManager? = null,
    private val ahora: () -> Long = { Clock.System.now().toEpochMilliseconds() },
    private val conectividad: Conectividad? = null
) : ProductoRepository {
    private val sincronizacion = Mutex()

    override fun observar(): Flow<List<Producto>> = local.observar()
    override fun observarUltimaSincronizacion(): Flow<Long?> = local.observarUltimaSincronizacion()
    override fun observarPendientes(): Flow<Int> = local.observarPendientes()
    override fun observarFallidas(): Flow<Int> = local.observarFallidas()
    override suspend fun listar(): List<Producto> = local.listar()

    override suspend fun registrar(producto: Producto): Producto {
        val pendiente = local.guardarPendiente(producto)
        try {
            sincronizarInterno(pendiente.id)
        } catch (fallo: ErrorApiException) {
            // Los rechazos del servidor y los errores de sesión deben ser visibles.
            if (fallo.error != ErrorApi.SinConexion && fallo.error != ErrorApi.TiempoAgotado) throw fallo
        }
        return local.porId(pendiente.id) ?: pendiente
    }

    override suspend fun sincronizar(): ResumenSync = sincronizarInterno()

    private suspend fun sincronizarInterno(idRegistro: Long? = null): ResumenSync = sincronizacion.withLock {
        var enviadas = 0
        var transporteInterrumpido = false
        var falloDelRegistro: Throwable? = null
        // Un ciclo hace como máximo un envío por fila. No hay un bucle que repita POST.
        for (pendiente in local.pendientesPorEnviar()) {
            var confirmado: Producto? = null
            var fallo: Throwable? = null
            val resultado = try {
                confirmado = ejecutarLlamada(sesionManager) {
                    if (conectividad?.conectado?.value == false) throw EnvioNoIniciadoException()
                    // Primero persiste el bloqueo. Si el proceso muere o pierde la respuesta,
                    // reiniciar la app nunca reproduce automáticamente esta creación.
                    local.marcarFallida(pendiente.id, MOTIVO_INCIERTO)
                    remoto.crear(pendiente.toRequest()).toDomain()
                }
                ResultadoEnvio.Exito(checkNotNull(confirmado).id)
            } catch (cancelacion: CancellationException) {
                // Cancelar tras iniciar POST también puede perder su respuesta.
                // Una comprobación sin red no inició POST; las demás cancelaciones se concilian.
                local.marcarFallida(pendiente.id, MOTIVO_INCIERTO)
                throw cancelacion
            } catch (error: Exception) {
                fallo = error
                resultadoDeError(error)
            }
            when (resultado) {
                is ResultadoEnvio.Exito -> {
                    local.confirmarSubida(pendiente.id, checkNotNull(confirmado))
                    enviadas++
                }
                is ResultadoEnvio.Permanente -> local.marcarFallida(pendiente.id,
                    if (fallo != null && resultadoPostIncierto(fallo)) MOTIVO_INCIERTO else resultado.motivo)
                is ResultadoEnvio.Transitorio -> {
                    if (fallo != null && resultadoPostIncierto(fallo)) {
                        // PharmaSoft no implementa claves de idempotencia. No repetir un POST ambiguo.
                        local.marcarFallida(pendiente.id, MOTIVO_INCIERTO)
                    } else {
                        local.registrarFalloTransitorio(pendiente.id, pendiente.intentos, resultado.motivo)
                        if (pendiente.intentos + 1 < Producto.MAX_INTENTOS) {
                            delay(esperaEntreIntentos(pendiente.intentos))
                        }
                    }
                }
            }
            if (pendiente.id == idRegistro) falloDelRegistro = fallo
            val api = (fallo as? ErrorApiException)?.error
            if (api == ErrorApi.SinConexion || api == ErrorApi.TiempoAgotado) transporteInterrumpido = true
            val sesion = (fallo as? ErrorSesionException)?.error
            if (sesion == ErrorSesion.SesionExpirada || sesion == ErrorSesion.CredencialesInvalidas) {
                transporteInterrumpido = true
                // ejecutarLlamada ya notificó a SesionManager: no enviar con credenciales inválidas.
                break
            }
        }
        if (!transporteInterrumpido) ejecutarLlamada(sesionManager) {
            // Solo después de recorrer los envíos: descarga completa, incluso con filas fallidas.
            val productos = mutableListOf<Producto>()
            var pagina = 0
            do {
                val respuesta = remoto.listar(pagina = pagina++)
                productos += respuesta.contenido.map { it.toDomain() }
            } while (!respuesta.ultima)
            if (local.contarPendientes() == 0 && local.contarFallidas() == 0)
                local.confirmarSincronizacion(productos, ahora())
            else local.reemplazarSincronizados(productos)
        }
        // Un error del producto recién registrado sigue llegando al campo; errores de
        // otras filas no se atribuyen al formulario actual. Todos quedan guardados por fila.
        falloDelRegistro?.let { error ->
            val api = (error as? ErrorApiException)?.error
            if (api != ErrorApi.SinConexion && api != ErrorApi.TiempoAgotado) throw error
        }
        ResumenSync(enviadas, local.contarFallidas(), local.contarPendientes())
    }

    override suspend fun corregirPendiente(producto: Producto) = sincronizacion.withLock {
        val fila = requireNotNull(local.porId(producto.id))
        require(fila.pendiente)
        require(fila.ultimoError != MOTIVO_INCIERTO) { "Comprueba y concilia el envío incierto antes de corregirlo" }
        require(producto.nombre.length in 3..150 && producto.precio >= 0.01 && producto.categoriaId > 0)
        local.corregirPendiente(producto)
        Unit
    }

    override suspend fun reintentarPendiente(id: Long, confirmaRevisionServidor: Boolean) = sincronizacion.withLock {
        val fila = requireNotNull(local.porId(id))
        require(fila.pendiente)
        require(confirmaRevisionServidor) { "Revisa primero el servidor para evitar duplicados" }
        require(fila.ultimoError == MOTIVO_INCIERTO || fila.ultimoError in ERRORES_RECUPERABLES) {
            "Corrige los datos o permisos que causaron el rechazo antes de volver a enviar"
        }
        local.reiniciarIntentos(id)
        Unit
    }

    override suspend fun conciliarPendiente(idLocal: Long, idServidor: Long) = sincronizacion.withLock {
        val fila = requireNotNull(local.porId(idLocal))
        require(fila.pendiente && fila.ultimoError == MOTIVO_INCIERTO && idServidor > 0)
        val remoto = ejecutarLlamada(sesionManager) { remoto.obtener(idServidor).toDomain() }
        require(fila.nombre == remoto.nombre && fila.precio == remoto.precio && fila.stock == remoto.stock &&
            fila.categoriaId == remoto.categoriaId) { "El producto del servidor no coincide: revisa los datos" }
        local.confirmarSubida(idLocal, remoto)
        Unit
    }

    companion object {
        const val MOTIVO_INCIERTO = pe.edu.upeu.pharmamobil.domain.model.PoliticaEnvio.INCIERTO
        val ERRORES_RECUPERABLES = pe.edu.upeu.pharmamobil.domain.model.PoliticaEnvio.recuperables
    }

    // Editar y eliminar requieren conexión, según la decisión del usuario.
    override suspend fun obtener(id: Long): Producto = ejecutarLlamada(sesionManager) {
        require(id > 0) { "Sincroniza el producto antes de editarlo" }
        remoto.obtener(id).toDomain()
    }

    override suspend fun actualizar(producto: Producto): Producto = sincronizacion.withLock {
        ejecutarLlamada(sesionManager) {
            require(producto.id > 0) { "Sincroniza el producto antes de editarlo" }
            remoto.actualizar(producto.id, producto.toRequest()).toDomain().also(local::guardarSincronizado)
        }
    }

    override suspend fun eliminar(id: Long) = sincronizacion.withLock {
        ejecutarLlamada(sesionManager) {
            require(id > 0) { "Sincroniza el producto antes de eliminarlo" }
            remoto.eliminar(id)
            local.listar().firstOrNull { it.id == id }?.let { local.guardarSincronizado(it.copy(estado = false)) }
            Unit
        }
    }
}
