package pe.edu.upeu.pharmamobil.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

actual fun crearDriverEnMemoria(): SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
