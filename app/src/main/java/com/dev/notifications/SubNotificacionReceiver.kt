package com.dev.notifications

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dev.agenda_movil.R

class SubNotificacionReceiver : BroadcastReceiver() {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        val titulo = intent.getStringExtra("TITULO") ?: "Recordatorio"
        val descripcion = intent.getStringExtra("DESCRIPCION") ?: ""

        // Usa el canal correcto: "subnotificaciones_channel" o "recordatorios_channel"
        val notification = NotificationCompat.Builder(context, "subnotificaciones_channel")
            .setContentTitle(titulo)
            .setContentText(descripcion)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // ID único para cada notificación
        val notificationId = titulo.hashCode() + descripcion.hashCode()
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}


