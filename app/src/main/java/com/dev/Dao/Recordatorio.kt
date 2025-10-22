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
    val cumplido: Boolean = false
)