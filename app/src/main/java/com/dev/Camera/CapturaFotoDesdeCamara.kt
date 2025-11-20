package com.dev.Camera

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.CameraAlt
import com.dev.utils.crearUriPersistente


@Composable
fun CapturaFotoDesdeCamara(
    onFotoCapturada: (Uri) -> Unit
) {
    val context = LocalContext.current
    val uriTemporal = remember { mutableStateOf<Uri?>(null) }

    val launcherCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val uri = uriTemporal.value
        if (success && uri != null) {
            onFotoCapturada(uri)
        }
    }

    Button(onClick = {
        val nuevaUri = crearUriPersistente(context)
        uriTemporal.value = nuevaUri
        launcherCamara.launch(nuevaUri)
    }) {
        Icon(Icons.Default.CameraAlt, contentDescription = "Abrir cámara")
        Spacer(Modifier.width(8.dp))
        Text("Tomar foto")
    }
}
