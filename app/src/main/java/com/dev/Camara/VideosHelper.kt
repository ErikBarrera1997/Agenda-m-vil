package com.dev.Camara

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.io.File

object VideosHelper{
    fun eliminarVideo(context: Context, uriString: String): Boolean {
        val uri = Uri.parse(uriString)
        return try {
            val rows = context.contentResolver.delete(uri, null, null)
            if (rows > 0) {
                Toast.makeText(context, "Video eliminado", Toast.LENGTH_SHORT).show()
                true
            } else {
                false
            }
        } catch (se: SecurityException) {
            // No tienes permisos directos, pedir confirmación al usuario
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = uri
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
            false
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al eliminar video", Toast.LENGTH_SHORT).show()
            false
        }
    }

}