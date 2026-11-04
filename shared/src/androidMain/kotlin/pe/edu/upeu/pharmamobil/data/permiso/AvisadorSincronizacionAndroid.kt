package pe.edu.upeu.pharmamobil.data.permiso

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import pe.edu.upeu.pharmamobil.domain.model.ResumenSync
import pe.edu.upeu.pharmamobil.domain.port.AvisadorSincronizacion

class AvisadorSincronizacionAndroid(contexto: Context) : AvisadorSincronizacion {
    private val contexto = contexto.applicationContext
    override fun avisar(resumen: ResumenSync) {
        if (Build.VERSION.SDK_INT >= 33 && contexto.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) return
        val gestor = contexto.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!gestor.areNotificationsEnabled()) return
        if (Build.VERSION.SDK_INT >= 26) gestor.createNotificationChannel(NotificationChannel(
            CANAL, "Resultados de sincronización", NotificationManager.IMPORTANCE_DEFAULT))
        val constructor = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(contexto, CANAL) else Notification.Builder(contexto)
        val notificacion = constructor.setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle(if (resumen.fallidas > 0) "Productos que requieren atención" else "Sincronización terminada")
            .setContentText("Enviados: ${resumen.enviadas} · Por enviar: ${resumen.pendientes} · Requieren atención: ${resumen.fallidas}")
            .setAutoCancel(true).build()
        // El permiso puede revocarse entre la comprobación y notify; el catálogo sigue usable.
        try { gestor.notify(13, notificacion) } catch (_: SecurityException) { }
    }
    private companion object { const val CANAL = "resultados_sincronizacion" }
}
