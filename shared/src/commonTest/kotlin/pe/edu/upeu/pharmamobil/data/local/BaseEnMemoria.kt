package pe.edu.upeu.pharmamobil.data.local

import app.cash.sqldelight.db.SqlDriver
import pe.edu.upeu.pharmamobil.db.PharmaDb

expect fun crearDriverEnMemoria(): SqlDriver

fun baseEnMemoria(driver: SqlDriver): PharmaDb {
    PharmaDb.Schema.create(driver)
    return PharmaDb(driver)
}
