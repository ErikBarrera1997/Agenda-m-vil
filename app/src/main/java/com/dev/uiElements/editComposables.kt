package com.dev.uiElements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun EditarRecordatorioDialog(
    recordatorio: Recordatorio,
    onDismiss: () -> Unit,
    onSave: (Recordatorio) -> Unit
) {
    var titulo by remember { mutableStateOf(recordatorio.titulo) }
    var descripcion by remember { mutableStateOf(recordatorio.descripcion) }
    var fechaInicio by remember { mutableStateOf(recordatorio.fechaInicio ?: "") }
    var fechaFin by remember { mutableStateOf(recordatorio.fechaFin ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                onSave(
                    recordatorio.copy(
                        titulo = titulo,
                        descripcion = descripcion,
                        fechaInicio = fechaInicio,
                        fechaFin = fechaFin
                    )
                )
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        title = { Text("Editar Recordatorio") },
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
                    onValueChange = { fechaInicio = it },
                    label = { Text("Fecha de inicio") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = fechaFin,
                    onValueChange = { fechaFin = it },
                    label = { Text("Fecha de fin") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewEditarRecordatorioDialog() {
    EditarRecordatorioDialog(
        recordatorio = Recordatorio(
            id = 1,
            titulo = "Curso de Kotlin",
            descripcion = "Aprender Jetpack Compose",
            fechaInicio = "10/Octubre/2025",
            fechaFin = "30/Noviembre/2025",
            //cumplido = false
        ),
        onDismiss = {},
        onSave = {}
    )
}
