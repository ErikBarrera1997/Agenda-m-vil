package com.dev.uiElements

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

@Composable
fun EditarRecordatorioDialog(
    recordatorio: Recordatorio,
    onDismiss: () -> Unit,
    onSave: (Recordatorio) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    var titulo by remember { mutableStateOf(recordatorio.titulo) }
    var descripcion by remember { mutableStateOf(recordatorio.descripcion) }
    var fechaInicio by remember { mutableStateOf(recordatorio.fechaInicio ?: "") }
    var fechaFin by remember { mutableStateOf(recordatorio.fechaFin ?: "") }
    var cumplido by remember { mutableStateOf(recordatorio.cumplido) }

    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

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
                onSave(
                    recordatorio.copy(
                        titulo = titulo,
                        descripcion = descripcion,
                        fechaInicio = fechaInicio,
                        fechaFin = fechaFin,
                        cumplido = cumplido
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

                Text("Fecha de inicio", style = MaterialTheme.typography.labelSmall)
                OutlinedButton(
                    onClick = { showDatePicker { fechaInicio = it } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (fechaInicio.isEmpty()) "Seleccionar fecha" else fechaInicio)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Fecha de fin", style = MaterialTheme.typography.labelSmall)
                OutlinedButton(
                    onClick = { showDatePicker { fechaFin = it } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (fechaFin.isEmpty()) "Seleccionar fecha" else fechaFin)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = cumplido, onCheckedChange = { cumplido = it })
                    Text("Cumplido", modifier = Modifier.padding(start = 8.dp))
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Audio", style = MaterialTheme.typography.titleSmall)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Controles de audio aquí")
                }
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
            titulo = "Curso de Phyton",
            descripcion = "",
            fechaInicio = "10/Octubre/2025",
            fechaFin = "30/Noviembre/2025",
            //cumplido = true
        ),
        onDismiss = {},
        onSave = {}
    )
}
