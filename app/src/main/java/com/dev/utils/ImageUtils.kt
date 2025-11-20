package com.dev.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
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
    val imagesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val file = File(imagesDir, "recordatorio_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
}
