package pe.edu.upeu.pharmamobil.data.local

interface AlmacenSeguro {
    /** Bloqueo persistente tras logout voluntario, independiente del vencimiento del JWT. */
    fun bloquearConsultaLocal(bloqueada: Boolean) {}
    fun consultaLocalBloqueada(): Boolean = false
    fun guardarSesion(sesion: pe.edu.upeu.pharmamobil.domain.model.Sesion) {}
    fun leerSesion(): pe.edu.upeu.pharmamobil.domain.model.Sesion? = null
    fun guardarToken(token: String)
    fun leerToken(): String?
    fun borrarToken()
}
