package com.dev.uiElements

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.dev.Dao.Recordatorio
import com.dev.Dao.SubNotificacion
import com.dev.agenda_movil.AppViewModelProvider
import com.dev.agenda_movil.MainActivity
import com.dev.agenda_movil.R
import com.dev.utils.crearUriPersistente
import com.dev.utils.crearUriVideoPersistente
import kotlinx.coroutines.launch
import java.nio.file.Files.copy
import java.text.SimpleDateFormat
import java.util.*


@Composable
fun RecordatoriosScreen(
    navController: NavController,
    windowSizeClass: WindowSizeClass
) {
    val viewModel: RecordatoriosViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val recordatorios by viewModel.recordatorios.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val recordatoriosFiltrados = remember(searchQuery, recordatorios) {
        if (searchQuery.isBlank()) {
            recordatorios
        } else {
            recordatorios.filter {
                it.titulo.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val isExpandedScreen = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

    if (isExpandedScreen) {
        RecordatoriosListAndDetailContent(
            recordatorios = recordatoriosFiltrados,
            viewModel = viewModel,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it }
        )
    } else {
        Scaffold(
            topBar = {
                TopBar(onAddClick = { navController.navigate(Screen.Agregar.route) })
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { navController.navigate(Screen.Agregar.route) }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.agregar))
                }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(stringResource(id = R.string.buscar_recordatorio)) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = stringResource(id = R.string.buscar))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    singleLine = true
                )

                ReminderList(
                    reminders = recordatoriosFiltrados,
                    onItemClick = { recordatorio ->
                        navController.navigate(Screen.Editar.createRoute(recordatorio.id))
                    }
                )
            }
        }
    }
}

@Composable
fun RecordatoriosListAndDetailContent(
    recordatorios: List<Recordatorio>,
    viewModel: RecordatoriosViewModel,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    val selectedRecordatorio by viewModel.selectedRecordatorio

    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text(stringResource(id = R.string.buscar_recordatorio)) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = stringResource(id = R.string.buscar))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                singleLine = true
            )

            ReminderList(
                reminders = recordatorios,
                onItemClick = { recordatorio ->
                    viewModel.selectRecordatorio(recordatorio)
                }
            )
        }

        Box(modifier = Modifier.weight(1.5f)) {
            selectedRecordatorio?.let {
                EditReminderScreen(
                    recordatorioId = it.id,
                    onBack = { viewModel.selectRecordatorio(null) } // Oculta la vista de detalle
                )
            } ?: run {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(stringResource(id = R.string.selecciona_recordatorio_detalle))
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(onAddClick: () -> Unit) {
    TopAppBar(
        title = { Text(stringResource(id = R.string.recordatorios)) }, //aqui usamos los strings de los idiomas
        navigationIcon = {
            IconButton(onClick = { /* abrir calendario */ }) {
                Icon(Icons.Default.DateRange, contentDescription = stringResource(id = R.string.calendario))
            }
        },
        actions = {
            IconButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.agregar))
            }
        }
    )
}



@Composable
fun ReminderList(
    reminders: List<Recordatorio>,
    onItemClick: (Recordatorio) -> Unit
) {
    val context = LocalContext.current
    val viewModel: RecordatoriosViewModel = viewModel(factory = AppViewModelProvider.Factory)

    LazyColumn {
        itemsIndexed(reminders) { _, reminder ->
            ReminderItem(
                reminder = reminder,
                onEdit = onItemClick,
                onDelete = { recordatorio ->
                    viewModel.eliminar(context, recordatorio)
                }
            )
        }
    }
}



@Composable
fun ReminderItem(
    reminder: Recordatorio,
    onEdit: (Recordatorio) -> Unit,
    onDelete: (Recordatorio) -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            onClick = { onEdit(reminder) }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = reminder.titulo, style = MaterialTheme.typography.titleMedium)
                Text(text = reminder.descripcion, style = MaterialTheme.typography.bodyMedium)

                if (!reminder.fechaInicio.isNullOrBlank()) {
                    Text(
                        text = stringResource(id = R.string.inicio, reminder.fechaInicio),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                if (!reminder.fechaFin.isNullOrBlank()) {
                    Text(
                        text = stringResource(id = R.string.fin, reminder.fechaFin),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                if (reminder.cumplido) {
                    Text(
                        text = stringResource(id = R.string.cumplido),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF388E3C)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = { onEdit(reminder) }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(id = R.string.editar_recordatorio))
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(id = R.string.eliminar_recordatorio_titulo))
                    }
                }
            }
        }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                properties = DialogProperties(dismissOnClickOutside = false),
                title = { Text(stringResource(id = R.string.eliminar_recordatorio_titulo)) },
                text = { Text(stringResource(id = R.string.eliminar_recordatorio_mensaje)) },
                confirmButton = {
                    Button(onClick = {
                        onDelete(reminder)
                        showDeleteConfirm = false
                    }) {
                        Text(stringResource(id = R.string.eliminar))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text(stringResource(id = R.string.cancelar))
                    }
                }
            )
        }
    }
}

//VENTANA PARA AGREGAR RECORDATORIOS
@Composable
fun AddReminderDialog(
    onDismiss: () -> Unit,
    onSave: (Recordatorio) -> Unit
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val focusManager = LocalFocusManager.current
    val viewModel: RecordatoriosViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val formState = viewModel.formState.value
    var showSubDialog by remember { mutableStateOf(false) }

    //
    var uriPersistente by remember { mutableStateOf<Uri?>(null) }

    //AQUI SE USA PARA ABRIR LA CAMARA'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
    val launcherCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val uri = uriPersistente
        if (success && uri != null) {
            viewModel.actualizarCampo {
                copy(imagenesUri = imagenesUri + uri.toString())
            }
        }
    }

    val launcherVideo = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        val uri = uriPersistente
        if (success && uri != null) {
            viewModel.actualizarCampo {
                copy(videosUri = videosUri + uri.toString())
            }
        }
    }

    var nuevaUriAudio by remember { mutableStateOf<Uri?>(null) }

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
                val hora = String.format("%02d:%02d", hour, minute)
                onTimeSelected(hora)
            },
            now.get(Calendar.HOUR_OF_DAY),
            now.get(Calendar.MINUTE),
            true
        ).show()
    }

    val scrollState = rememberScrollState()
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false),
        confirmButton = {
            Button(onClick = {
                viewModel.validarYConstruir()?.let { recordatorio ->
                    onSave(recordatorio)
                    viewModel.limpiarFormulario()
                }
            }) {
                Text(stringResource(id = R.string.guardar))     //BOTON DE GUARDAAARRRRRRRR
            }
        },
        dismissButton = {
            TextButton(onClick = {
                viewModel.limpiarFormulario()
                onDismiss()
            }) { Text(stringResource(id = R.string.cancelar)) }
        },
        title = { Text(stringResource(id = R.string.nuevo_recordatorio)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState) //habilita scroll
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { showSubDialog = true },
                        enabled = formState.fechaFin.isNotBlank() && formState.horaFin.isNotBlank()
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = "Configurar subnotificaciones")
                        Spacer(Modifier.width(4.dp))
                        Text("Configurar subnotificación")
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
                            viewModel.updateFormState {
                                copy(subnotificaciones = subnotificaciones + sub)
                            }
                            showSubDialog = false
                        }
                    )
                }

                Column(modifier = Modifier.padding(8.dp)) {
                    if (formState.subnotificaciones.isEmpty()) {
                        Text("No hay subnotificaciones configuradas")
                    } else {
                        formState.subnotificaciones.forEach { sub ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Cada ${sub.intervaloMinutos} min hasta ${sub.fechaFin} ${sub.horaFin}")
                                IconButton(onClick = {
                                    viewModel.updateFormState {
                                        copy(subnotificaciones = subnotificaciones - sub)
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Eliminar",
                                        tint = Color.Red
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = formState.titulo,
                    onValueChange = { viewModel.actualizarCampo { copy(titulo = it) } },
                    label = { Text(stringResource(id = R.string.titulo)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
                if (formState.showErrors && formState.titulo.isBlank()) {
                    Text(stringResource(id = R.string.error_titulo_vacio), color = Color.Red, style = MaterialTheme.typography.labelSmall)
                }

                OutlinedTextField(
                    value = formState.descripcion,
                    onValueChange = { viewModel.actualizarCampo { copy(descripcion = it) } },
                    label = { Text(stringResource(id = R.string.descripcion)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
                if (formState.showErrors && formState.descripcion.isBlank()) {
                    Text(stringResource(id = R.string.error_descripcion_vacia), color = Color.Red, style = MaterialTheme.typography.labelSmall)
                }

                Text(stringResource(id = R.string.fecha_inicio))
                OutlinedButton(
                    onClick = { showDatePicker { viewModel.actualizarCampo { copy(fechaInicio = it) } } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(formState.fechaInicio.ifEmpty { stringResource(id = R.string.seleccionar_fecha) })
                }

                Text(stringResource(id = R.string.hora_inicio))
                OutlinedButton(
                    onClick = { showTimePicker { viewModel.actualizarCampo { copy(horaInicio = it) } } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(formState.horaInicio.ifEmpty { stringResource(id = R.string.seleccionar_hora) })
                }

                Text(stringResource(id = R.string.fecha_fin))
                OutlinedButton(
                    onClick = { showDatePicker { viewModel.actualizarCampo { copy(fechaFin = it) } } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(formState.fechaFin.ifEmpty { stringResource(id = R.string.seleccionar_fecha) })
                }
                if (formState.showErrors && formState.fechaFin.isBlank()) {
                    Text(stringResource(id = R.string.error_fecha_fin), color = Color.Red, style = MaterialTheme.typography.labelSmall)
                }

                Text(stringResource(id = R.string.hora_fin))
                OutlinedButton(
                    onClick = { showTimePicker { viewModel.actualizarCampo { copy(horaFin = it) } } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(formState.horaFin.ifEmpty { stringResource(id = R.string.seleccionar_hora) })
                }
                if (formState.showErrors && formState.horaFin.isBlank()) {
                    Text(stringResource(id = R.string.error_hora_fin), color = Color.Red, style = MaterialTheme.typography.labelSmall)
                }
 //               Text("URI: ${formState.imagenUri}")
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = formState.cumplido,
                        onCheckedChange = { viewModel.actualizarCampo { copy(cumplido = it) } }
                    )
                    Text(stringResource(id = R.string.cumplido), modifier = Modifier.padding(start = 8.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))
                //Text(stringResource(id = R.string.imagen), style = MaterialTheme.typography.titleSmall)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        val nuevaUri = crearUriPersistente(context)
                        uriPersistente = nuevaUri
                        launcherCamara.launch(nuevaUri)
                    }) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Abrir cámara")
                    }
                    Text(text = stringResource(id = R.string.tomar_foto), modifier = Modifier.padding(start = 8.dp))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if ((context as MainActivity).tienePermisosGaleria()) {
                            val nuevaUriVideo = crearUriVideoPersistente(context)
                            uriPersistente = nuevaUriVideo
                            launcherVideo.launch(nuevaUriVideo)
                        } else {
                            Toast.makeText(context, "No puedes grabar video sin permisos", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.Videocam, contentDescription = "Grabar video")
                    }

                    Text(text = stringResource(id = R.string.tomar_video), modifier = Modifier.padding(start = 8.dp))
                }

//========================================================================================================
                // Mostrar todas las imágenes persistentes
                if (formState.imagenesUri.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(formState.imagenesUri) { uriString ->
                            AsyncImage(
                                model = Uri.parse(uriString),
                                contentDescription = "Imagen adjunta",
                                modifier = Modifier
                                    .width(200.dp)
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.LightGray),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Sin imágenes",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
//====================================================================================================
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
                                    factory = { ctx ->
                                        VideoView(ctx).apply {
                                            setVideoURI(uri)
                                            setOnPreparedListener { mp ->
                                                mp.isLooping = true
                                                start()
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Sin videos",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
//==============================================================================================================


            }
        }
    )
}

@Composable
fun ConfigurarSubnotificacionesDialog(
    fechaInicio: String,
    horaInicio: String,
    fechaFin: String,
    horaFin: String,
    onDismiss: () -> Unit,
    onSave: (SubNotificacion) -> Unit
) {
    var intervalo by remember { mutableStateOf("30") } // minutos por defecto
    var unidad by remember { mutableStateOf("minutos") }
    val dropdown = UnidadDropdown()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurar subnotificación") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = intervalo,
                    onValueChange = { intervalo = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Intervalo") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(Modifier.height(8.dp))

                dropdown.Render(
                    selected = unidad,
                    onSelected = { unidad = it },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))
                val pluralUnidad = if (intervalo == "1") unidad.removeSuffix("s") else unidad
                Text("Se repetirá cada $intervalo $pluralUnidad hasta $fechaFin $horaFin")
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val valor = intervalo.toIntOrNull()
                if (valor != null && valor > 0) {
                    val minutos = when (unidad) {
                        "horas" -> valor * 60
                        "días" -> valor * 24 * 60
                        else -> valor
                    }

                    val sub = SubNotificacion(
                        intervaloMinutos = minutos,
                        fechaInicio = fechaInicio,
                        horaInicio = horaInicio,
                        fechaFin = fechaFin,
                        horaFin = horaFin
                    )
                    onSave(sub)
                }
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun AddReminderScreen(onBack: () -> Unit) {
    val viewModel: RecordatoriosViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.mostrarDialogoAgregar()
    }

    LaunchedEffect(viewModel.snackbarMessage.value) {
        viewModel.snackbarMessage.value?.let { mensajeId ->
            scope.launch {
                snackbarHostState.showSnackbar(context.getString(mensajeId))
                viewModel.consumirSnackbar()
            }
            onBack()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (viewModel.mostrarDialogo.value) {
                AddReminderDialog(
                    onDismiss = {
                        viewModel.cerrarDialogo()
                        onBack()
                    },
                    onSave = { nuevo ->
                        viewModel.agregarYNotificar(nuevo, context)
                    }
                )
            }
        }
    }
}
