package com.dev.uiElements

sealed class Screen(val route: String) {
    object Lista : Screen("lista")
    object Agregar : Screen("agregar")
    object Editar : Screen("editar/{id}") {
        fun createRoute(id: Int) = "editar/$id"
    }
}


