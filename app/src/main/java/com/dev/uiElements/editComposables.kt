package com.dev.uiElements

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.dev.Dao.SubNotificacion
import com.dev.Data.RecordatorioFormState
import com.dev.agenda_movil.MainActivity
import com.dev.agenda_movil.R
import com.dev.utils.crearUriPersistente
import com.dev.utils.crearUriVideoPersistente
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EditarRecordatorioDialog(
    formState: RecordatorioFormState,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onUpdate: (RecordatorioFormState.() -> RecordatorioFormState) -> Unit,
    onAgregarImagen: (Uri) -> Unit,
    onEliminarImagen: (String) -> Unit,
    onAgregarVideo: () -> Unit,
    onEliminarVideo: (String) -> Unit,
    onAgregarAudio: () -> Unit,
    onEliminarAudio: (String) -> Unit,
    onAgregarSubnotificacion: (SubNotificacion) -> Unit,
    onEliminarSubnotificacion: (SubNotificacion) -> Unit
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val focusManager = LocalFocusManager.current
    //var showImagePreview by remember { mutableStateOf(false) }
    fun showDatePicker(onDateSelected: (String) -> Unit) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                onDateSelected(dateFormatter.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val now = Calendar.getInstance()
        TimePickerDialog(
            context,
            { _, hour, minute ->
                val horaFormateada = String.format("%02d:%02d", hour, minute)
                onTimeSelected(horaFormateada)
            },
            now.get(Calendar.HOUR_OF_DAY),
            now.get(Calendar.MINUTE),
            true
        ).show()
    }

    var showSubDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false),
        confirmButton = {
            Button(onClick = onSave) {
                Text(stringResource(id = R.string.guardar))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancelar))
            }
        },
        title = { Text(stringResource(id = R.string.editar_recordatorio)) },
        text = {
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState) //habilita scroll
            ){
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showSubDialog = true }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Configurar subnotificaciones")
                        Spacer(Modifier.width(4.dp))
                        Text("Configurar")
                    }

                }

                formState.subnotificaciones.forEach { sub ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Cada ${sub.intervaloMinutos} min hasta ${sub.fechaFin} ${sub.horaFin}")
                        IconButton(onClick = { onEliminarSubnotificacion(sub) }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Eliminar subnotificación",
                                tint = Color.Red
                            )
                        }
                    }
                }


                if (showSubDialog) {
                    ConfigurarSubnotificacionesDialog(
                        fechaInicio = formState.fechaInicio,
                        horaInicio = formState.horaInicio,
                        fechaFin = formState.fechaFin,
                        horaFin = formState.horaFin,
                        onDismiss = { showSubDialog = false },
                        onSave = { sub ->
                            onUpdate {
                                copy(subnotificaciones = subnotificaciones + sub)
                            }
                            showSubDialog = false
                        }
                    )
                }



                OutlinedTextField(
                    value = formState.titulo,
                    onValueChange = { onUpdate { copy(titulo = it) } },
                    label = { Text(stringResource(id = R.string.titulo)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
                if (formState.showErrors && formState.titulo.isBlank()) {
                    Text(
                        stringResource(id = R.string.error_titulo_vacio),
                        color = Color.Red,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                OutlinedTextField(
                    value = formState.descripcion,
                    onValueChange = { onUpdate { copy(descripcion = it) } },
                    label = { Text(stringResource(id = R.string.descripcion)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
                if (formState.showErrors && formState.descripcion.isBlank()) {
                    Text(
                        stringResource(id = R.string.error_descripcion_vacia),
                        color = Color.Red,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Text(stringResource(id = R.string.fecha_inicio), style = MaterialTheme.typography.labelSmall)
                OutlinedButton(
                    onClick = { showDatePicker { onUpdate { copy(fechaInicio = it) } } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(formState.fechaInicio.ifEmpty { stringResource(id = R.string.seleccionar_fecha) })
                }

                Text(stringResource(id = R.string.hora_inicio), style = MaterialTheme.typography.labelSmall)
                OutlinedButton(
                    onClick = { showTimePicker { onUpdate { copy(horaInicio = it) } } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(formState.horaInicio.ifEmpty { stringResource(id = R.string.seleccionar_hora) })
                }

                Text(stringResource(id = R.string.fecha_fin), style = MaterialTheme.typography.labelSmall)
                OutlinedButton(
                    onClick = { showDatePicker { onUpdate { copy(fechaFin = it) } } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(formState.fechaFin.ifEmpty { stringResource(id = R.string.seleccionar_fecha) })
                }
                if (formState.showErrors && formState.fechaFin.isBlank()) {
                    Text(
                        stringResource(id = R.string.error_fecha_fin),
                        color = Color.Red,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Text(stringResource(id = R.string.hora_fin), style = MaterialTheme.typography.labelSmall)
                OutlinedButton(
                    onClick = { showTimePicker { onUpdate { copy(horaFin = it) } } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(formState.horaFin.ifEmpty { stringResource(id = R.string.seleccionar_hora) })
                }
                if (formState.showErrors && formState.horaFin.isBlank()) {
                    Text(
                        stringResource(id = R.string.error_hora_fin),
                        color = Color.Red,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = formState.cumplido,
                        onCheckedChange = { onUpdate { copy(cumplido = it) } }
                    )
                    Text(stringResource(id = R.string.cumplido), modifier = Modifier.padding(start = 8.dp))
                }
                //Text("URI: ${formState.imagenUri}")
                Spacer(modifier = Modifier.height(8.dp))

//=============================================================================================================
                if (formState.imagenesUri.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(formState.imagenesUri) { uriString ->
                            val uri = Uri.parse(uriString)
                            Box(
                                modifier = Modifier
                                    .width(180.dp)
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.LightGray)
                                    //.clickable { showImagePreview = true }
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Imagen adjunta",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                IconButton(
                                    onClick = { onEliminarImagen(uriString) }, // 👈 ahora recibe el uri específico
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar imagen", tint = Color.White)
                                }
                            }
                        }
                    }

                    // Botón para agregar otra imagen
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = {
                            val nueva = crearUriPersistente(context)
                            onAgregarImagen(nueva)
                        }) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Agregar imagen")
                            Spacer(Modifier.width(4.dp))
                            Text("Agregar")
                        }
                    }
                } else {
                    TextButton(onClick = {
                        val nueva = crearUriPersistente(context)
                        onAgregarImagen(nueva)
                    }) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Agregar imagen")
                        Spacer(Modifier.width(4.dp))
                        Text("Agregar imagen")
                    }
                }
 //VIDEOS===============================================================
//=================================================================================================================
                if (formState.videosUri.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        formState.videosUri.forEach { uriString ->
                            val uri = Uri.parse(uriString)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black)
                            ) {
                                AndroidView(
                                    factory = { ctx -> VideoView(ctx) },
                                    modifier = Modifier.fillMaxSize(),
                                    update = { videoView ->
                                        videoView.setVideoURI(uri)
                                        videoView.setOnPreparedListener { mp ->
                                            mp.isLooping = true
                                            videoView.start()
                                        }
                                        videoView.setOnErrorListener { _, _, _ ->
                                            Toast.makeText(context, "No se pudo reproducir el video", Toast.LENGTH_SHORT).show()
                                            true
                                        }
                                    }
                                )
                                IconButton(
                                    onClick = { onEliminarVideo(uriString) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar video", tint = Color.White)
                                }

                            }
                        }
                    }

                    // Botón para agregar otro video
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { onAgregarVideo() }) {
                            Icon(Icons.Default.Videocam, contentDescription = "Agregar video")
                            Spacer(Modifier.width(4.dp))
                            Text("Agregar")
                        }
                    }
                } else {
                    TextButton(onClick = { onAgregarVideo() }) {
                        Icon(Icons.Default.Videocam, contentDescription = "Agregar video")
                        Spacer(Modifier.width(4.dp))
                        Text("Agregar video")
                    }
                }
//============================================================================================================
                if (formState.audiosUri.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(formState.audiosUri) { uriString ->
                            val uri = Uri.parse(uriString)
                            Box(
                                modifier = Modifier
                                    .width(200.dp)
                                    .height(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.DarkGray)
                            ) {
                                // Preview simple con botón Play
                                AndroidView<Button>(
                                    factory = { ctx ->
                                        Button(ctx).apply {
                                            text = "Play"
                                            setOnClickListener {
                                                val player = MediaPlayer().apply {
                                                    setDataSource(ctx, uri)
                                                    setOnPreparedListener { start() }
                                                }
                                                player.prepareAsync()
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )

                                IconButton(
                                    onClick = { onEliminarAudio(uriString) }, // 👈 callback
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar audio", tint = Color.White)
                                }
                            }
                        }
                    }

                    // Botón para agregar otro audio
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { onAgregarAudio() }) {
                            Icon(Icons.Default.Mic, contentDescription = "Agregar audio")
                            Spacer(Modifier.width(4.dp))
                            Text("Agregar")
                        }
                    }
                } else {
                    TextButton(onClick = { onAgregarAudio() }) {
                        Icon(Icons.Default.Mic, contentDescription = "Agregar audio")
                        Spacer(Modifier.width(4.dp))
                        Text("Agregar audio")
                    }
                }
            }
        }
    )

}

@Composable
fun EditReminderScreen(recordatorioId: Int, onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = RecordatoriosRepositoryProvider.provide(context)
    val viewModel: EditComposablesViewModel = viewModel(
        factory = EditComposablesViewModelFactory(repository, recordatorioId)
    )

    val recordatorio by viewModel.recordatorio.collectAsState()
    val formState by viewModel.formState
    val mostrarDialogo by viewModel.mostrarDialogo
    val snackbarMessage by viewModel.snackbarMessage

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var nuevaUri by remember { mutableStateOf<Uri?>(null) }
    val launcherCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            nuevaUri?.let { uri ->
                viewModel.agregarImagen(context, uri)
            }
        }
    }

    var nuevaUriVideo by remember { mutableStateOf<Uri?>(null) }
    val launcherVideo = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        if (success) {
            nuevaUriVideo?.let { uri ->
                val fd = context.contentResolver.openFileDescriptor(uri, "r")
                if (fd != null) {
                    viewModel.agregarVideo(context, uri)
                } else {
                    Toast.makeText(context, "El video no se grabó correctamente", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val launcherAudio = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.actualizarCampo {
                    copy(audiosUri = audiosUri + uri.toString())
                }
            }
        }
    }


    LaunchedEffect(recordatorio) {
        recordatorio?.let { viewModel.inicializarFormulario(it) }
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { mensaje ->
            scope.launch {
                snackbarHostState.showSnackbar(mensaje)
                viewModel.consumirSnackbar()
            }
            onBack()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (mostrarDialogo) {
                EditarRecordatorioDialog(
                    formState = formState,
                    onDismiss = {
                        viewModel.cerrarDialogo()
                        onBack()
                    },
                    onSave = {
                        viewModel.guardar(context) {
                            onBack()
                        }
                    },
                    onUpdate = { viewModel.actualizarCampo(it) },
                    onAgregarImagen = { uri ->
                        nuevaUri = uri
                        launcherCamara.launch(uri)
                    },
                    onEliminarImagen = { uri ->
                        viewModel.eliminarImagen(context, uri)
                    },
                    onAgregarVideo = {
                        val nueva = crearUriVideoPersistente(context)
                        nuevaUriVideo = nueva
                        launcherVideo.launch(nueva)
                    },
                    onEliminarVideo = { uri ->
                        viewModel.eliminarVideo(context, uri)
                    },
                    onAgregarAudio = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                            == PackageManager.PERMISSION_GRANTED
                        ) {
                            // Si ya tiene permiso, lanzar grabadora
                            val intent = Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION)
                            launcherAudio.launch(intent)
                        } else {
                            (context as? MainActivity)?.solicitarPermisoAudio()
                        }
                    },
                    onEliminarAudio = { uri ->
                        viewModel.eliminarAudio(context, uri)
                    },
                    onAgregarSubnotificacion = { sub ->
                        viewModel.agregarSubnotificacion(sub)
                    },
                    onEliminarSubnotificacion = { sub: SubNotificacion ->
                        viewModel.eliminarSubnotificacion(context, sub)
                    }
                )
            }
        }
    }
}



