package com.dev.uiElements

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dev.Dao.Recordatorio
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RecordatoriosScreen() {
    val context = LocalContext.current
    val viewModel: RecordatoriosViewModel = viewModel(
        factory = RecordatoriosViewModelFactory(context)
    )

    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopBar(onAddClick = { showDialog = true }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            SearchBar()
            ReminderList(
                reminders = viewModel.recordatorios,
                onEdit = { actualizado -> viewModel.editarRecordatorio(actualizado) }
            )
        }

        if (showDialog) {
            AddReminderDialog(
                onDismiss = { showDialog = false },
                onSave = { nuevo ->
                    viewModel.agregarRecordatorio(nuevo)
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
fun SearchBar() {
    OutlinedTextField(
        value = "",
        onValueChange = { /* actualizar búsqueda */ },
        label = { Text("Buscar Recordatorios") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Buscar")
        }
    )
}

@Composable
fun ReminderList(
    reminders: List<Recordatorio>,
    onEdit: (Recordatorio) -> Unit
) {
    LazyColumn {
        itemsIndexed(reminders) { _, reminder ->
            ReminderItem(
                reminder = reminder,
                onEdit = { actualizado ->
                    onEdit(actualizado)
                }
            )
        }
    }
}


@Composable
fun ReminderItem(
    reminder: Recordatorio,
    onEdit: (Recordatorio) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

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
                        text = "✔ Cumplido",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF388E3C),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = {
                        println("Lápiz presionado")
                        showEditDialog = true
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
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
                    showEditDialog = false
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
                    fechaFin = fechaFin,
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
                OutlinedTextField(
                    value = fechaInicio,
                    onValueChange = {},
                    label = { Text("Fecha de inicio") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker { fechaInicio = it } },
                    readOnly = true
                )
                OutlinedTextField(
                    value = fechaFin,
                    onValueChange = {},
                    label = { Text("Fecha de fin") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker { fechaFin = it } },
                    readOnly = true
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = cumplido, onCheckedChange = { cumplido = it })
                    Text("Cumplido", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    )
}




@Preview(showBackground = true)
@Composable
fun PreviewRecordatoriosScreen() {
    RecordatoriosScreen()
}