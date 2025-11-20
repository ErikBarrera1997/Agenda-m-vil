package com.dev.Data

import android.os.Bundle
import androidx.compose.runtime.saveable.Saver

// RecordatorioFormState.kt

data class RecordatorioFormState(
    val titulo: String = "",
    val descripcion: String = "",
    val fechaInicio: String = "",
    val horaInicio: String = "",
    val fechaFin: String = "",
    val horaFin: String = "",
    val cumplido: Boolean = false,
    val imagenUri: String? = null,
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
            putString("imagenUri", state.imagenUri)
            putBoolean("showErrors", state.showErrors)
        }
    },
    restore = { bundle ->
        RecordatorioFormState(
            titulo = bundle.getString("titulo") ?: "",
            descripcion = bundle.getString("descripcion") ?: "",
            fechaInicio = bundle.getString("fechaInicio") ?: "",
            horaInicio = bundle.getString("horaInicio") ?: "",
            fechaFin = bundle.getString("fechaFin") ?: "",
            horaFin = bundle.getString("horaFin") ?: "",
            cumplido = bundle.getBoolean("cumplido"),
            imagenUri = bundle.getString("imagenUri"),
            showErrors = bundle.getBoolean("showErrors")
        )
    }
)

