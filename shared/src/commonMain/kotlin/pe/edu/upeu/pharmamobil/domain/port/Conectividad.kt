package pe.edu.upeu.pharmamobil.domain.port

import kotlinx.coroutines.flow.StateFlow

/** Disponibilidad de una red; no garantiza que PharmaSoft responda. */
interface Conectividad { val conectado: StateFlow<Boolean> }
