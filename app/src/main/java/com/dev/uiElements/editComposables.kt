package com.dev.uiElements

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dev.Dao.Recordatorio
import com.dev.Data.RecordatorioFormState
import com.dev.Data.RecordatorioFormStateSaver
import com.dev.agenda_movil.AppViewModelProvider
import com.dev.agenda_movil.MainActivity
import com.dev.agenda_movil.R
import kotlinx.coroutines.launch
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
    val calendar = remember { Calendar.getInstance() }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val focusManager = LocalFocusManager.current


    val initialFormState = remember(recordatorio.id) {
        RecordatorioFormState(
            titulo = recordatorio.titulo,
            descripcion = recordatorio.descripcion,
            fechaInicio = recordatorio.fechaInicio ?: "",
            horaInicio = recordatorio.horaInicio ?: "",
            fechaFin = recordatorio.fechaFin ?: "",
            horaFin = recordatorio.horaFin ?: "",
            cumplido = recordatorio.cumplido
        )
    }

    var formState by rememberSaveable(stateSaver = RecordatorioFormStateSaver()) {
        mutableStateOf(initialFormState)
    }


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
                    val actualizado = recordatorio.copy(
                        titulo = formState.titulo,
                        descripcion = formState.descripcion,
                        fechaInicio = formState.fechaInicio.ifBlank { null },
                        horaInicio = formState.horaInicio.ifBlank { null },
                        fechaFin = formState.fechaFin.ifBlank { null },
                        horaFin = formState.horaFin.ifBlank { null },
                        cumplido = formState.cumplido
                    )
                    onSave(actualizado)
                } else {
                    formState = formState.copy(showErrors = true)
                }
            }) {
                Text(stringResource(id = R.string.guardar))
            }

        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(id = R.string.cancelar)) }
        },
        title = { Text(stringResource(id = R.string.editar_recordatorio)) },
        text = {
            Column {
                OutlinedTextField(
                    value = formState.titulo,
                    onValueChange = { formState = formState.copy(titulo = it) },
                    label = { Text(stringResource(id = R.string.titulo)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )

                if (formState.showErrors && formState.titulo.isBlank()) {
                    Text(stringResource(id = R.string.error_titulo_vacio), color = Color.Red, style = MaterialTheme.typography.labelSmall)
                }

                OutlinedTextField(
                    value = formState.descripcion,
                    onValueChange = { formState = formState.copy(descripcion = it) },
                    label = { Text(stringResource(id = R.string.descripcion)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )

                if (formState.showErrors && formState.descripcion.isBlank()) {
                    Text(stringResource(id = R.string.error_descripcion_vacia), color = Color.Red, style = MaterialTheme.typography.labelSmall)
                }


                Text(stringResource(id = R.string.fecha_inicio), style = MaterialTheme.typography.labelSmall)
                OutlinedButton(
                    onClick = { showDatePicker { formState = formState.copy(fechaInicio = it) } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(formState.fechaInicio.ifEmpty { stringResource(id = R.string.seleccionar_fecha) })
                }

                Text(stringResource(id = R.string.hora_inicio), style = MaterialTheme.typography.labelSmall)
                OutlinedButton(
                    onClick = { showTimePicker { formState = formState.copy(horaInicio = it) } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(formState.horaInicio.ifEmpty { stringResource(id = R.string.seleccionar_hora) })
                }

                Text(stringResource(id = R.string.fecha_fin), style = MaterialTheme.typography.labelSmall)
                OutlinedButton(
                    onClick = { showDatePicker { formState = formState.copy(fechaFin = it) } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(formState.fechaFin.ifEmpty { stringResource(id = R.string.seleccionar_fecha) })
                }
                if (formState.showErrors && formState.fechaFin.isBlank()) {
                    Text(stringResource(id = R.string.error_fecha_fin), color = Color.Red, style = MaterialTheme.typography.labelSmall)
                }


                Text(stringResource(id = R.string.hora_fin), style = MaterialTheme.typography.labelSmall)
                OutlinedButton(
                    onClick = { showTimePicker { formState = formState.copy(horaFin = it) } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(formState.horaFin.ifEmpty { stringResource(id = R.string.seleccionar_hora) })
                }
                if (formState.showErrors && formState.horaFin.isBlank()) {
                    Text(stringResource(id = R.string.error_hora_fin), color = Color.Red, style = MaterialTheme.typography.labelSmall)
                }


                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = formState.cumplido,
                        onCheckedChange = { formState = formState.copy(cumplido = it) }
                    )
                    Text(stringResource(id = R.string.cumplido), modifier = Modifier.padding(start = 8.dp))
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(stringResource(id = R.string.audio), style = MaterialTheme.typography.titleSmall)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(id = R.string.agregar_audio))
                }
            }
        }
    )
}


@Composable
fun EditReminderScreen(recordatorioId: Int, onBack: () -> Unit) {
    val viewModel: RecordatoriosViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val context = LocalContext.current
    val activity = context as? MainActivity

    val recordatorio by viewModel.getById(recordatorioId).collectAsState(initial = null)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var mostrarDialogo by remember { mutableStateOf(false) }

    LaunchedEffect(recordatorio) {
        if (recordatorio != null) {
            mostrarDialogo = true
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            recordatorio?.let { recordatorioActual ->
                if (mostrarDialogo) {
                    EditarRecordatorioDialog(
                        recordatorio = recordatorioActual,
                        onDismiss = {
                            mostrarDialogo = false
                            onBack()
                        },
                        onSave = { actualizado ->
                            viewModel.editar(actualizado)
                            activity?.programarNotificacionesPorFechas(actualizado)
                            mostrarDialogo = false
                            scope.launch {
                                snackbarHostState.showSnackbar(context.getString(R.string.recordatorio_editado))
                            }
                            onBack()
                        }
                    )
                }
            }
        }
    }
}