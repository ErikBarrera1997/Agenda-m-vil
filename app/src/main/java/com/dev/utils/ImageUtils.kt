package com.dev.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

fun crearUriTemporal(context: Context): Uri {
    val imageFile = File.createTempFile("recordatorio_", ".jpg", context.cacheDir).apply {
        createNewFile()
        deleteOnExit()
    }
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile)
}
