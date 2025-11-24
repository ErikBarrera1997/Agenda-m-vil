package com.dev.utils

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

//fun crearUriTemporal(context: Context): Uri {
  //  val imageFile = File.createTempFile("recordatorio_", ".jpg", context.cacheDir).apply {
    //    createNewFile()
      //  deleteOnExit()
   // }
    //return FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile)
//}


/////ESTE ES EL PRIVIDER
fun crearUriPersistente(context: Context): Uri {
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "recordatorio_${System.currentTimeMillis()}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Scoped Storage (Android 10+)
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Recordatorios")
        }
    }

    val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

    if (uri == null) {
        throw IllegalStateException("No se pudo crear la URI persistente. Verifica permisos y RELATIVE_PATH.")
    }

    return uri
}
