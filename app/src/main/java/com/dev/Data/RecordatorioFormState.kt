package com.dev.Data

import android.os.Bundle
import androidx.compose.runtime.saveable.Saver
import com.dev.Dao.SubNotificacion
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// RecordatorioFormState.kt

data class RecordatorioFormState(
    val titulo: String = "",
    val descripcion: String = "",
    val fechaInicio: String = "",
    val horaInicio: String = "",
    val fechaFin: String = "",
    val horaFin: String = "",
    val cumplido: Boolean = false,

    val imagenesUri: List<String> = emptyList(),
    val videosUri: List<String> = emptyList(),
    val audiosUri: List<String> = emptyList(),
    val subnotificaciones: List<SubNotificacion> = emptyList(),
    val showErrors: Boolean = false
)


// Saver personalizado para RecordatorioFormState
fun RecordatorioFormStateSaver() = Saver<RecordatorioFormState, Bundle>(
    save = { state ->
        Bundle().apply {
            putString("titulo", state.titulo)
            putString("descripcion", state.descripcion)
            putString("fechaInicio", state.fechaInicio)
            putString("horaInicio", state.horaInicio)
            putString("fechaFin", state.fechaFin)
            putString("horaFin", state.horaFin)
            putBoolean("cumplido", state.cumplido)

            // Guardar listas simples
            putStringArrayList("imagenesUri", ArrayList(state.imagenesUri))
            putStringArrayList("videosUri", ArrayList(state.videosUri))
            putStringArrayList("audiosUri", ArrayList(state.audiosUri))

            // Guardar subnotificaciones como JSON
            val gson = Gson()
            putString("subnotificaciones", gson.toJson(state.subnotificaciones))

            putBoolean("showErrors", state.showErrors)
        }
    },
    restore = { bundle ->
        val gson = Gson()
        val subJson = bundle.getString("subnotificaciones")
        val type = object : TypeToken<List<SubNotificacion>>() {}.type
        val subList: List<SubNotificacion> = if (subJson != null) gson.fromJson(subJson, type) else emptyList()

        RecordatorioFormState(
            titulo = bundle.getString("titulo") ?: "",
            descripcion = bundle.getString("descripcion") ?: "",
            fechaInicio = bundle.getString("fechaInicio") ?: "",
            horaInicio = bundle.getString("horaInicio") ?: "",
            fechaFin = bundle.getString("fechaFin") ?: "",
            horaFin = bundle.getString("horaFin") ?: "",
            cumplido = bundle.getBoolean("cumplido"),

            imagenesUri = bundle.getStringArrayList("imagenesUri") ?: emptyList(),
            videosUri = bundle.getStringArrayList("videosUri") ?: emptyList(),
            audiosUri = bundle.getStringArrayList("audiosUri") ?: emptyList(),

            subnotificaciones = subList,
            showErrors = bundle.getBoolean("showErrors")
        )
    }
)

