package com.dev.Dao

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recordatorios")
data class Recordatorio(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titulo: String,
    val descripcion: String,
    val fechaInicio: String? = null,
    val fechaFin: String? = null,
    val horaInicio: String?,
    val horaFin: String?,
    val cumplido: Boolean = false,
    val imagenesUri: List<String> = emptyList(),
    val videosUri: List<String> = emptyList(),
    val audiosUri: List<String> = emptyList(),
    val subnotificaciones: List<SubNotificacion> = emptyList()
)

data class SubNotificacion(
    val id: Int = 0,
    val intervaloMinutos: Int,   // cada cuánto tiempo se repite
    val fechaInicio: String,     // fecha de inicio
    val horaInicio: String,      // hora de inicio
    val fechaFin: String,        // fecha límite
    val horaFin: String          // hora límite
)
