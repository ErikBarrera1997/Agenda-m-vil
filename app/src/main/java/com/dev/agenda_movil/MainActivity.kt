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
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.core.content.ContextCompat
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
import com.dev.utils.crearUriPersistente
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : ComponentActivity() {

    private var permisosConcedidos = false

    // Launcher para permisos múltiples
    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
            val imagesGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions[Manifest.permission.READ_MEDIA_IMAGES] ?: false
            } else {
                permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: false
            }
            val videosGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions[Manifest.permission.READ_MEDIA_VIDEO] ?: false
            } else {
                true
            }

            permisosConcedidos = cameraGranted && imagesGranted && videosGranted

            if (!permisosConcedidos) {
                Toast.makeText(this, "No se otorgaron permisos de cámara/galería", Toast.LENGTH_SHORT).show()
            }
        }

    // Launcher para notificaciones
    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(this, "No se otorgó permiso para notificaciones", Toast.LENGTH_SHORT).show()
            }
        }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //crear canal de notificación
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "recordatorios_channel",
                "Recordatorios",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Canal para recordatorios"
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        //pedir permisos de notificaciones en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }


        //pedir permisos de cámara + imágenes y tambien videoooooooos
        val permisos = mutableListOf(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permisos.add(Manifest.permission.READ_MEDIA_IMAGES)
            permisos.add(Manifest.permission.READ_MEDIA_VIDEO) // 👈 necesario para videos
        } else {
            permisos.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                permisos.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        requestPermissionsLauncher.launch(permisos.toTypedArray())


        setContent {
            Agenda_movilTheme {
                val navController = rememberNavController()
                val windowSizeClass = calculateWindowSizeClass(this)

                NavHost(
                    navController = navController,
                    startDestination = Screen.Lista.route
                ) {
                    composable(Screen.Lista.route) {
                        RecordatoriosScreen(
                            navController = navController,
                            windowSizeClass = windowSizeClass,
                           // onAgregarFoto = { abrirCamara() }
                        )
                    }

                    composable(Screen.Agregar.route) {
                        AddReminderScreen(onBack = { navController.popBackStack() })
                    }

                    composable(
                        route = Screen.Editar.route,
                        arguments = listOf(navArgument("id") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getInt("id") ?: return@composable
                        EditReminderScreen(
                            recordatorioId = id,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }

    // Helper
    fun tienePermisosGaleria(): Boolean = permisosConcedidos

    //Acción para abrir cámara
    fun abrirCamara() {
        if (tienePermisosGaleria()) {
            val uri = crearUriPersistente(this)
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, uri)
            }
           // startActivityForResult(intent, REQUEST_CODE_CAMERA)
        } else {
            Toast.makeText(this, "No puedes abrir la cámara sin permisos", Toast.LENGTH_SHORT).show()
        }
    }




    fun programarNotificacion(recordatorio: Recordatorio, triggerTimeMillis: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "No se puede programar alarmas exactas. Revisa permisos o configuración de batería.", Toast.LENGTH_LONG).show()
                return
            }
        }

        // Verificar permiso de notificaciones en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "No tienes permiso para mostrar notificaciones", Toast.LENGTH_LONG).show()
                return
            }
        }

        val intent = Intent(this, NotificacionReceiver::class.java).apply {
            putExtra("titulo", recordatorio.titulo)
            putExtra("descripcion", recordatorio.descripcion)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
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
            Toast.makeText(this, "Error al programar la notificación: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun programarNotificacionesPorFechas(recordatorio: Recordatorio) {
        recordatorio.fechaInicio?.let { fecha ->
            recordatorio.horaInicio?.let { hora ->
                val trigger = convertirFechaHoraATimestamp(fecha, hora)
                programarNotificacion(recordatorio, trigger)
            }
        }

        recordatorio.fechaFin?.let { fecha ->
            recordatorio.horaFin?.let { hora ->
                val trigger = convertirFechaHoraATimestamp(fecha, hora)
                programarNotificacion(recordatorio, trigger)
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