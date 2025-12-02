package com.dev.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.dev.Dao.Recordatorio
import java.text.SimpleDateFormat
import java.util.Locale

object NotificacionesHelper {

    fun programarNotificacion(context: Context, recordatorio: Recordatorio, triggerTimeMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(context, "No se puede programar alarmas exactas. Revisa permisos o configuración de batería.", Toast.LENGTH_LONG).show()
                return
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "No tienes permiso para mostrar notificaciones", Toast.LENGTH_LONG).show()
                return
            }
        }

        val intent = Intent(context, NotificacionReceiver::class.java).apply {
            putExtra("titulo", recordatorio.titulo)
            putExtra("descripcion", recordatorio.descripcion)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            recordatorio.titulo.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            Toast.makeText(context, "Error al programar la notificación: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun programarSubnotificaciones(context: Context, recordatorio: Recordatorio) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        recordatorio.subnotificaciones.forEach { sub ->
            val intent = Intent(context, SubNotificacionReceiver::class.java).apply {
                putExtra("titulo", recordatorio.titulo)
                putExtra("descripcion", recordatorio.descripcion)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                sub.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val horaInicio = convertirFechaHoraATimestamp(sub.fechaInicio, sub.horaInicio)
            val horaFin = convertirFechaHoraATimestamp(sub.fechaFin, sub.horaFin)
            val intervaloMillis = sub.intervaloMinutos * 60 * 1000L

            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                horaInicio,
                intervaloMillis,
                pendingIntent
            )

            val cancelIntent = PendingIntent.getBroadcast(
                context,
                ("cancel_" + sub.hashCode()).hashCode(),
                Intent(context, CancelReceiver::class.java).apply {
                    putExtra("requestCode", sub.hashCode())
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                horaFin,
                cancelIntent
            )
        }
    }

    fun programarNotificacionesPorFechas(context: Context, recordatorio: Recordatorio) {
        recordatorio.fechaInicio?.let { fecha ->
            recordatorio.horaInicio?.let { hora ->
                val trigger = convertirFechaHoraATimestamp(fecha, hora)
                programarNotificacion(context, recordatorio, trigger)
            }
        }

        recordatorio.fechaFin?.let { fecha ->
            recordatorio.horaFin?.let { hora ->
                val trigger = convertirFechaHoraATimestamp(fecha, hora)
                programarNotificacion(context, recordatorio, trigger)
            }
        }
    }

    fun convertirFechaHoraATimestamp(fecha: String, hora: String): Long {
        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return try {
            formato.parse("$fecha $hora")?.time ?: System.currentTimeMillis()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }
}