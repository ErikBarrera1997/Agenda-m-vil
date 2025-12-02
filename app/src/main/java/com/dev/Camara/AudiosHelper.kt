package com.dev.Camara

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import java.io.File
import java.io.InputStream
import java.io.FileInputStream

object AudiosHelper {

    // MIME por defecto para audio. Ajusta según tu códec/grabador.
    private const val DEFAULT_AUDIO_MIME = "audio/mpeg" // mp3
    private const val DEFAULT_RELATIVE_PATH = "Music/AgendaMovil" // carpeta visible (scoped storage)

    fun eliminarAudio(context: Context, uriString: String?): Boolean {
        if (uriString.isNullOrBlank()) return false
        return try {
            val uri = Uri.parse(uriString)
            if (isContentUri(uri)) {
                // content:// vía MediaStore / SAF
                val rows = context.contentResolver.delete(uri, null, null)
                rows > 0
            } else {
                // file:// o ruta absoluta
                val file = File(uri.path ?: return false)
                if (file.exists()) file.delete() else false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun existe(context: Context, uriString: String?): Boolean {
        if (uriString.isNullOrBlank()) return false
        return try {
            val uri = Uri.parse(uriString)
            if (isContentUri(uri)) {
                context.contentResolver.openFileDescriptor(uri, "r")?.use { true } ?: false
            } else {
                val file = File(uri.path ?: return false)
                file.exists()
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Crea una Uri persistente en MediaStore para escribir un nuevo audio.
     * Usa esta Uri como destino de tu grabador (Output).
     */
    fun crearUriAudioPersistente(context: Context, displayName: String = defaultDisplayName()): Uri? {
        return try {
            val values = ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Audio.Media.MIME_TYPE, DEFAULT_AUDIO_MIME)
                // En Android 10+ usar RELATIVE_PATH para scoped storage
                put(MediaStore.Audio.Media.RELATIVE_PATH, DEFAULT_RELATIVE_PATH)
            }
            context.contentResolver.insert(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                values
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Escribe bytes de audio en una Uri (por ejemplo, si tu grabador te da un InputStream o byte[]).
     */
    fun escribirAudio(context: Context, destUri: Uri, input: InputStream): Boolean {
        return try {
            context.contentResolver.openOutputStream(destUri, "w")?.use { out ->
                input.copyTo(out)
                true
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Copia un archivo local (file:// o ruta absoluta) hacia una Uri de MediaStore.
     */
    fun copiarArchivoAudio(context: Context, source: File, destUri: Uri): Boolean {
        return try {
            FileInputStream(source).use { input ->
                escribirAudio(context, destUri, input)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun tamanoBytes(context: Context, uriString: String?): Long? {
        if (uriString.isNullOrBlank()) return null
        return try {
            val uri = Uri.parse(uriString)
            if (isContentUri(uri)) {
                context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize }
            } else {
                val file = File(uri.path ?: return null)
                if (file.exists()) file.length() else null
            }
        } catch (_: Exception) {
            null
        }
    }

    // Limpia URIs que ya no existen físicamente (orphaned) de una lista
    fun limpiarOrfanos(context: Context, uris: List<String>): List<String> {
        return uris.filter { existe(context, it) }
    }

    private fun isContentUri(uri: Uri): Boolean = uri.scheme.equals("content", ignoreCase = true)

    private fun defaultDisplayName(): String {
        val ts = System.currentTimeMillis()
        return "audio_$ts.mp3"
    }
}