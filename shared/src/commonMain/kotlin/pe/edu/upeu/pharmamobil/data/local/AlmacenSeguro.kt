package pe.edu.upeu.pharmamobil.data.local

interface AlmacenSeguro {
    fun guardarSesion(sesion: pe.edu.upeu.pharmamobil.domain.model.Sesion) {}
    fun leerSesion(): pe.edu.upeu.pharmamobil.domain.model.Sesion? = null
    fun guardarToken(token: String)
    fun leerToken(): String?
    fun borrarToken()
}
