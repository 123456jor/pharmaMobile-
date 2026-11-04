package pe.edu.upeu.pharmamobil.domain.port

import pe.edu.upeu.pharmamobil.domain.model.ResumenSync

interface AvisadorSincronizacion { fun avisar(resumen: ResumenSync) }
