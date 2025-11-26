package com.dev.Camara

import android.content.Context
import android.net.Uri
import android.widget.Toast

fun eliminarVideo(context: Context, uriString: String) {
    try {
        val uri = Uri.parse(uriString)
        val rows = context.contentResolver.delete(uri, null, null)
        if (rows > 0) {
            Toast.makeText(context, "Video eliminado", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "No se pudo eliminar el video", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error al eliminar video", Toast.LENGTH_SHORT).show()
    }
}