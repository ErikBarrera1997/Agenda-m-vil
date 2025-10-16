package com.dev.uiElements

data class Recordatorio(
    val id: Int,
    var titulo: String,
    var descripcion: String,
    var fechaInicio: String? = null,
    var fechaFin: String? = null,
    val cumplido: Boolean = false
)
