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
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dev.Dao.Recordatorio
import com.dev.agenda_movil.ui.theme.Agenda_movilTheme
import com.dev.notifications.NotificacionReceiver
import com.dev.uiElements.AddReminderScreen
import com.dev.uiElements.EditReminderScreen
import com.dev.uiElements.RecordatoriosScreen
import com.dev.uiElements.Screen


class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(this, "No se otorgó permiso para notificaciones", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        //crearCanalDeNotificacion(this)

        setContent {
            Agenda_movilTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = Screen.Lista.route) {
                    composable(Screen.Lista.route) {
                        RecordatoriosScreen(navController = navController)
                    }

                    composable(Screen.Agregar.route) {
                        AddReminderScreen(onBack = { navController.popBackStack() })
                    }

                    composable(
                        route = Screen.Editar.route,
                        arguments = listOf(navArgument("id") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getInt("id") ?: return@composable
                        EditReminderScreen(recordatorioId = id, onBack = { navController.popBackStack() })
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
}
}








