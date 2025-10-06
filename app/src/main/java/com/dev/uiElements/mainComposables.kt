package com.dev.uiElements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun RecordatoriosScreen() {
    val recordatorios = remember { mutableStateListOf<Recordatorio>() }
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
                reminders = recordatorios,
                onEdit = { index, nuevoTitulo, nuevaDescripcion ->
                    recordatorios[index] = recordatorios[index].copy(
                        titulo = nuevoTitulo,
                        descripcion = nuevaDescripcion
                    )
                }
            )
        }

        if (showDialog) {
            AddReminderDialog(
                onDismiss = { showDialog = false },
                onSave = { titulo, descripcion ->
                    recordatorios.add(Recordatorio(recordatorios.size, titulo, descripcion))
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
    onEdit: (Int, String, String) -> Unit
) {
    LazyColumn {
        itemsIndexed(reminders) { index, reminder ->
            ReminderItem(
                reminder = reminder,
                onEdit = { nuevoTitulo, nuevaDescripcion ->
                    onEdit(index, nuevoTitulo, nuevaDescripcion)
                }
            )
        }
    }
}

@Composable
fun ReminderItem(
    reminder: Recordatorio,
    onEdit: (String, String) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = reminder.titulo, style = MaterialTheme.typography.titleMedium)
            Text(text = reminder.descripcion, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.End) {
                IconButton(onClick = { showEditDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
            }
        }
    }

    if (showEditDialog) {
        AddReminderDialog(
            initialTitle = reminder.titulo,
            initialDescription = reminder.descripcion,
            onDismiss = { showEditDialog = false },
            onSave = { nuevoTitulo, nuevaDescripcion ->
                onEdit(nuevoTitulo, nuevaDescripcion)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun AddReminderDialog(
    initialTitle: String = "",
    initialDescription: String = "",
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var titulo by remember { mutableStateOf(initialTitle) }
    var descripcion by remember { mutableStateOf(initialDescription) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = { onSave(titulo, descripcion) }) {
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
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewRecordatoriosScreen() {
    RecordatoriosScreen()
}