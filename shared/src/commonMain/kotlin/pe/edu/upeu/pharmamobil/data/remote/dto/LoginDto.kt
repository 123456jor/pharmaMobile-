package pe.edu.upeu.pharmamobil.data.remote.dto

import kotlinx.serialization.Serializable
import pe.edu.upeu.pharmamobil.domain.model.Sesion

@Serializable
data class LoginRequestDto(val username: String, val password: String)

@Serializable
data class LoginResponseDto(
    val token: String,
    val tipo: String,
    val expiraEnSegundos: Long,
    val username: String,
    val nombreCompleto: String,
    val roles: List<String>,
    val permisos: List<String>
)

fun LoginResponseDto.aSesion() = Sesion(token, username, nombreCompleto, permisos.toSet())

@Serializable
data class UsuarioActualDto(
    val username: String,
    val nombreCompleto: String,
    val roles: List<String> = emptyList(),
    val permisos: List<String> = emptyList()
)

@Serializable
data class ErrorSesionDto(
    val status: Int? = null,
    val error: String? = null,
    val message: String? = null,
    val path: String? = null
)
