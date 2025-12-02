package com.dev.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dev.Dao.Recordatorio
import com.dev.notifications.NotificacionesHelper
import com.dev.uiElements.RecordatoriosRepositoryProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BootReceiverSystem : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val repository = RecordatoriosRepositoryProvider.provide(context)

            CoroutineScope(Dispatchers.IO).launch {
                val recordatorios: List<Recordatorio> = repository.getAll().first() // suspende y obtiene la lista
                val ahora = System.currentTimeMillis()

                for (recordatorio in recordatorios) {
                    val finMillis = recordatorio.fechaFin?.let { fecha ->
                        recordatorio.horaFin?.let { hora ->
                            try {
                                val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                formato.parse("$fecha $hora")?.time
                            } catch (e: Exception) {
                                null
                            }
                        }
                    }

                    if (!recordatorio.cumplido && (finMillis == null || finMillis > ahora)) {
                        NotificacionesHelper.programarNotificacionesPorFechas(context, recordatorio)
                        NotificacionesHelper.programarSubnotificaciones(context, recordatorio)
                    }
                }
            }
        }
    }
}