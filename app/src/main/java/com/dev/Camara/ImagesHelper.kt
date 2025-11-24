package com.dev.Camara

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import java.io.File

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

