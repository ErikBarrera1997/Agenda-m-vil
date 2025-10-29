package com.dev.uiElements

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dev.Dao.Recordatorio
import com.dev.agenda_movil.MainActivity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RecordatoriosScreen(
    onRecordatorioAgregado: (Recordatorio) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? MainActivity
    val viewModel: RecordatoriosViewModel = viewModel(
        factory = RecordatoriosViewModelFactory(context)
    )

    var showDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val recordatoriosFiltrados = remember(searchQuery, viewModel.recordatorios) {
        if (searchQuery.isBlank()) {
            viewModel.recordatorios
        } else {
            viewModel.recordatorios.filter {
                it.titulo.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = { TopBar(onAddClick = { showDialog = true }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
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
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                singleLine = true
            )


            ReminderList(
                reminders = recordatoriosFiltrados,
                onEdit = { actualizado -> viewModel.editarRecordatorio(actualizado) },
                onDelete = { eliminado -> viewModel.eliminarRecordatorio(eliminado) },
                activity = activity
            )
        }

        if (showDialog) {
            AddReminderDialog(
                onDismiss = { showDialog = false },
                onSave = { nuevo ->
                    viewModel.agregarRecordatorio(nuevo)
                    activity?.programarNotificacionesPorFechas(nuevo) // ✅ se programa al crear
                    showDialog = false
                }
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
                    Text("Inicio: ${reminder.fechaInicio}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                if (!reminder.fechaFin.isNullOrBlank()) {
                    Text("Fin: ${reminder.fechaFin}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                if (reminder.cumplido) {
                    Text("Cumplido", style = MaterialTheme.typography.labelSmall, color = Color(0xFF388E3C))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
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
                    activity?.programarNotificacionesPorFechas(it)
                    showEditDialog = false
                }
            )
        }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
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
    val calendar = Calendar.getInstance()
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fechaInicio by remember { mutableStateOf("") }
    var fechaFin by remember { mutableStateOf("") }
    var cumplido by remember { mutableStateOf(false) }
    var horaInicio by remember { mutableStateOf("") }
    var horaFin by remember { mutableStateOf("") }

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

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                val nuevo = Recordatorio(
                    titulo = titulo,
                    descripcion = descripcion,
                    fechaInicio = fechaInicio,
                    horaInicio = horaInicio,
                    fechaFin = fechaFin,
                    horaFin = horaFin,
                    cumplido = cumplido
                )
                onSave(nuevo)
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        title = { Text("Nuevo Recordatorio") },
        text = {
            Column {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Fecha de inicio", style = MaterialTheme.typography.labelSmall)
                OutlinedButton(
                    onClick = { showDatePicker { fechaInicio = it } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (fechaInicio.isEmpty()) "Seleccionar fecha" else fechaInicio)
                }


                Text("Hora de inicio", style = MaterialTheme.typography.labelSmall)
                OutlinedButton(
                    onClick = { showTimePicker(context) { horaInicio = it } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (horaFin.isEmpty()) "Seleccionar hora" else horaFin)
                }


                Spacer(modifier = Modifier.height(8.dp))

                Text("Fecha de fin", style = MaterialTheme.typography.labelSmall)
                OutlinedButton(
                    onClick = { showDatePicker { fechaFin = it } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (fechaFin.isEmpty()) "Seleccionar fecha" else fechaFin)
                }

                Text("Hora de fin", style = MaterialTheme.typography.labelSmall)
                OutlinedButton(
                    onClick = { showTimePicker(context) { horaFin  = it } }
                    ,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (horaFin.isEmpty()) "Seleccionar hora" else horaFin)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = cumplido, onCheckedChange = { cumplido = it })
                    Text("Cumplido", modifier = Modifier.padding(start = 8.dp))
                }

            }
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





