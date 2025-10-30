package com.dev.uiElements

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.dev.Dao.Recordatorio
import com.dev.Data.RecordatorioFormState
import com.dev.agenda_movil.AppViewModelProvider
import com.dev.agenda_movil.MainActivity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RecordatoriosScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val activity = context as? MainActivity
    val viewModel: RecordatoriosViewModel = viewModel(factory = AppViewModelProvider.Factory)
    var searchQuery by remember { mutableStateOf("") }
    val recordatorios by viewModel.recordatorios.collectAsState()

    val recordatoriosFiltrados = remember(searchQuery, recordatorios) {
        if (searchQuery.isBlank()) {
            recordatorios
        } else {
            recordatorios.filter {
                it.titulo.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopBar(onAddClick = { navController.navigate(Screen.Agregar.route) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.Agregar.route) }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar recordatorio") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                singleLine = true
            )

            ReminderList(
                reminders = recordatoriosFiltrados,
                onEdit = { actualizado ->
                    navController.navigate(Screen.Editar.createRoute(actualizado.id))
                },
                onDelete = { eliminado ->
                    viewModel.eliminar(eliminado)
                },
                activity = activity
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(onAddClick: () -> Unit) {
    TopAppBar(
        title = { Text("Recordatorios") },
        navigationIcon = {
            IconButton(onClick = { /* abrir calendario */ }) {
                Icon(Icons.Default.DateRange, contentDescription = "Calendario")
            }
        },
        actions = {
            IconButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        }
    )
}



@Composable
fun ReminderList(
    reminders: List<Recordatorio>,
    onEdit: (Recordatorio) -> Unit,
    onDelete: (Recordatorio) -> Unit,
    activity: MainActivity?
) {
    LazyColumn {
        itemsIndexed(reminders) { _, reminder ->
            ReminderItem(
                reminder = reminder,
                onEdit = onEdit,
                onDelete = onDelete,
                activity = activity
            )
        }
    }
}


@Composable
fun ReminderItem(
    reminder: Recordatorio,
    onEdit: (Recordatorio) -> Unit,
    onDelete: (Recordatorio) -> Unit,
    activity: MainActivity?
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = reminder.titulo, style = MaterialTheme.typography.titleMedium)
                Text(text = reminder.descripcion, style = MaterialTheme.typography.bodyMedium)

                if (!reminder.fechaInicio.isNullOrBlank()) {
                    Text(
                        text = "Inicio: ${reminder.fechaInicio}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                if (!reminder.fechaFin.isNullOrBlank()) {
                    Text(
                        text = "Fin: ${reminder.fechaFin}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                if (reminder.cumplido) {
                    Text(
                        text = "Cumplido",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF388E3C)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar recordatorio")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar recordatorio")
                    }
                }
            }
        }

        if (showEditDialog) {
            EditarRecordatorioDialog(
                recordatorio = reminder,
                onDismiss = { showEditDialog = false },
                onSave = {
                    onEdit(it)
                    // activity?.programarNotificacionesPorFechas(it)
                    showEditDialog = false
                }
            )
        }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                properties = DialogProperties(dismissOnClickOutside = false),
                title = { Text("¿Eliminar recordatorio?") },
                text = { Text("Esta acción no se puede deshacer.") },
                confirmButton = {
                    Button(onClick = {
                        onDelete(reminder)
                        showDeleteConfirm = false
                    }) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun AddReminderDialog(
    onDismiss: () -> Unit,
    onSave: (Recordatorio) -> Unit
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    var formState by remember { mutableStateOf(RecordatorioFormState()) }
    val focusManager = LocalFocusManager.current


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

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false),
        confirmButton = {
            Button(onClick = {
                val camposValidos = formState.titulo.isNotBlank() &&
                        formState.descripcion.isNotBlank() &&
                        formState.fechaFin.isNotBlank() &&
                        formState.horaFin.isNotBlank()

                if (camposValidos) {
                    val nuevo = Recordatorio(
                        titulo = formState.titulo,
                        descripcion = formState.descripcion,
                        fechaInicio = formState.fechaInicio.ifBlank { null },
                        horaInicio = formState.horaInicio.ifBlank { null },
                        fechaFin = formState.fechaFin.ifBlank { null },
                        horaFin = formState.horaFin.ifBlank { null },
                        cumplido = formState.cumplido
                    )
                    onSave(nuevo)
                } else {
                    formState = formState.copy(showErrors = true)
                }
            }) {
                Text("Guardar")
            }

        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        title = { Text("Nuevo Recordatorio") },
        text = {
            Column {
                OutlinedTextField(
                    value = formState.titulo,
                    onValueChange = { formState = formState.copy(titulo = it) },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )
                if (formState.showErrors && formState.titulo.isBlank()) {
                    Text("El título no puede estar vacío", color = Color.Red, style = MaterialTheme.typography.labelSmall)
                }

                OutlinedTextField(
                    value = formState.descripcion,
                    onValueChange = { formState = formState.copy(descripcion = it) },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )

                if (formState.showErrors && formState.descripcion.isBlank()) {
                    Text("La descripción no puede estar vacía", color = Color.Red, style = MaterialTheme.typography.labelSmall)
                }

                Text("Fecha de inicio")
                OutlinedButton(
                    onClick = { showDatePicker { formState = formState.copy(fechaInicio = it) } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(formState.fechaInicio.ifEmpty { "Seleccionar fecha" })
                }

                Text("Hora de inicio")
                OutlinedButton(
                    onClick = { showTimePicker { formState = formState.copy(horaInicio = it) } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(formState.horaInicio.ifEmpty { "Seleccionar hora" })
                }

                Text("Fecha de fin")
                OutlinedButton(
                    onClick = { showDatePicker { formState = formState.copy(fechaFin = it) } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(formState.fechaFin.ifEmpty { "Seleccionar fecha" })
                }
                if (formState.showErrors && formState.fechaFin.isBlank()) {
                    Text("La fecha de fin es obligatoria", color = Color.Red, style = MaterialTheme.typography.labelSmall)
                }

                Text("Hora de fin")
                OutlinedButton(
                    onClick = { showTimePicker { formState = formState.copy(horaFin = it) } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(formState.horaFin.ifEmpty { "Seleccionar hora" })
                }
                if (formState.showErrors && formState.horaFin.isBlank()) {
                    Text("La hora de fin es obligatoria", color = Color.Red, style = MaterialTheme.typography.labelSmall)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = formState.cumplido,
                        onCheckedChange = { formState = formState.copy(cumplido = it) }
                    )
                    Text("Cumplido", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    )
}


@Composable
fun AddReminderScreen(onBack: () -> Unit) {
    val viewModel: RecordatoriosViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val context = LocalContext.current
    val activity = context as? MainActivity

    AddReminderDialog(
        onDismiss = onBack,
        onSave = { nuevo ->
            viewModel.agregar(nuevo)
            activity?.programarNotificacionesPorFechas(nuevo)
            onBack()
        }
    )
}

fun showTimePicker(context: Context, onTimeSelected: (String) -> Unit) {
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





