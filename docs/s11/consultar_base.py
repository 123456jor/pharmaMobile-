"""Copia y consulta SQLite real del APK debug. No inventa ni inserta productos."""
import datetime
import json
from pathlib import Path
import sqlite3
import subprocess
import sys

ADB = r"C:\Users\joorg\AppData\Local\Android\Sdk\platform-tools\adb.exe"
escenario = sys.argv[1]
archivo = Path("tmp/s11") / (escenario + ".db")
archivo.parent.mkdir(parents=True, exist_ok=True)
archivo.write_bytes(subprocess.check_output([
    ADB, "exec-out", "run-as", "pe.edu.upeu.pharmamobil", "cat", "databases/pharma.db"
]))
with sqlite3.connect(archivo) as conexion:
    conexion.row_factory = sqlite3.Row
    sql = "SELECT id,nombre,precio,stock,pendiente,estado,categoriaId FROM Producto ORDER BY nombre"
    filas = [dict(fila) for fila in conexion.execute(sql)]
    resultado = {
        "fecha_hora_lima": datetime.datetime.now().astimezone().isoformat(),
        "consulta": sql,
        "filas": filas,
        "consulta_fecha": "SELECT * FROM Sincronizacion",
        "sincronizacion": [dict(fila) for fila in conexion.execute("SELECT * FROM Sincronizacion")],
        "version": conexion.execute("PRAGMA user_version").fetchone()[0]
    }
destino = Path("docs/s11/consultas") / (escenario + ".json")
destino.write_text(json.dumps(resultado, ensure_ascii=False, indent=2), encoding="utf8")
print(json.dumps({"archivo": str(destino), "cantidad": len(filas),
                  "pendientes": [fila for fila in filas if fila["pendiente"]],
                  "sincronizacion": resultado["sincronizacion"]}, ensure_ascii=False))
