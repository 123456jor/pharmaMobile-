package pe.edu.upeu.pharmamobil.domain.model

/** No representa una sesión autenticada ni concede permisos para modificar datos. */
class ConsultaLocalException(val sesionExpirada: Boolean, causa: Throwable) :
    Exception("Solo se pueden consultar los datos de este dispositivo sin conexión", causa)
