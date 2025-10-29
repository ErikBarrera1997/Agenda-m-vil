package com.dev.agenda_movil

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dev.Dao.Recordatorio
import com.dev.agenda_movil.ui.theme.Agenda_movilTheme
import com.dev.notifications.NotificacionReceiver
import com.dev.uiElements.RecordatoriosScreen
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (!isGranted) {
                Toast.makeText(this, "No se otorgó permiso para notificaciones", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                }
                else -> {
                    requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

        crearCanalDeNotificacion(this)

        val ahora = Calendar.getInstance().apply { add(Calendar.MINUTE, 1) }
        val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(ahora.time)
        val hora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(ahora.time)

        val recordatorioPrueba = Recordatorio(
            titulo = "Prueba rápida",
            descripcion = "Esta es una notificación de prueba.",
            fechaInicio = fecha,
            horaInicio = hora,
            fechaFin = null,
            horaFin = null,
            cumplido = false
        )

        programarNotificacionesPorFechas(recordatorioPrueba)

        setContent {
            Agenda_movilTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RecordatoriosScreen(
                        onRecordatorioAgregado = { recordatorio ->
                            programarNotificacionesPorFechas(recordatorio)
                        }
                    )
                }
            }
        }
    }

    fun crearCanalDeNotificacion(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                "recordatorios_channel",
                "Recordatorios",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para notificaciones de recordatorios"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(canal)
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun mostrarNotificacion(context: Context, titulo: String, descripcion: String) {
        val builder = NotificationCompat.Builder(context, "recordatorios_channel")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(titulo)
            .setContentText(descripcion)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(titulo.hashCode(), builder.build())
    }

    fun programarNotificacion(context: Context, recordatorio: Recordatorio, triggerTimeMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(context, "No se puede programar alarmas exactas. Revisa permisos o configuración de batería.", Toast.LENGTH_LONG).show()
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

    fun programarNotificacionesPorFechas(recordatorio: Recordatorio) {
        val fechaInicioMillis = combinarFechaYHora(recordatorio.fechaInicio, recordatorio.horaInicio)
        val fechaFinMillis = combinarFechaYHora(recordatorio.fechaFin, recordatorio.horaFin)

        if (fechaInicioMillis != null && fechaInicioMillis > System.currentTimeMillis()) {
            val inicioRecordatorio = recordatorio.copy(
                titulo = recordatorio.titulo,
                descripcion = "El recordatorio '${recordatorio.titulo}' ha comenzado."
            )
            programarNotificacion(this, inicioRecordatorio, fechaInicioMillis)
        }

        if (fechaFinMillis != null && fechaFinMillis > System.currentTimeMillis()) {
            val finRecordatorio = recordatorio.copy(
                titulo = recordatorio.titulo,
                descripcion = "El recordatorio '${recordatorio.titulo}' ha finalizado."

            )
            programarNotificacion(this, finRecordatorio, fechaFinMillis)
        }
    }

    fun combinarFechaYHora(fecha: String?, hora: String?): Long? {
        if (fecha.isNullOrBlank() || hora.isNullOrBlank()) return null
        return try {
            val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            formato.parse("$fecha $hora")?.time
        } catch (e: Exception) { null }
    }

}








