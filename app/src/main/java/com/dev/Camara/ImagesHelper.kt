package com.dev.Camara

import android.content.Context
import android.net.Uri


//AQUI SE USAN LAS FUNCIONALIDADES QUE SE HACEN SOBRE LA IMAGEN, BORRAN EN MEMORIA FISICA
object ImagenHelper {
    fun eliminarImagen(context: Context, uriString: String?) {
        uriString?.let {
            val parsedUri = Uri.parse(it)
            try {
                context.contentResolver.delete(parsedUri, null, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

