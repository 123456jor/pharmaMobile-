package pe.edu.upeu.pharmamobil.build;

import app.cash.sqldelight.core.SqlDelightDatabaseProperties;
import app.cash.sqldelight.gradle.DriverInitializer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Se ejecuta dentro del trabajador de SQLDelight, antes de abrir SQLite JDBC. */
public final class InicializarSqlite implements DriverInitializer {
    @Override
    public void execute(SqlDelightDatabaseProperties database, Properties properties) {
        String directory = properties.getProperty("directorioTemporalSqlite");
        if (directory == null) {
            throw new IllegalStateException("Falta configurar directorioTemporalSqlite");
        }
        try {
            Path writable = Files.createDirectories(Path.of(directory));
            System.setProperty("org.sqlite.tmpdir", writable.toAbsolutePath().toString());
        } catch (IOException error) {
            throw new IllegalStateException("No se pudo preparar el temporal de SQLite JDBC", error);
        }
    }
}
