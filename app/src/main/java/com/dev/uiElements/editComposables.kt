package com.dev.uiElements

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dev.Data.RecordatorioFormState
import com.dev.agenda_movil.R
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EditarRecordatorioDialog(
    formState: RecordatorioFormState,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onUpdate: (RecordatorioFormState.() -> RecordatorioFormState) -> Unit
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
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
            Column {
                OutlinedTextField(
                    value = formState.titulo,
                    onValueChange = { onUpdate { copy(titulo = it) } },
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
                    onValueChange = { onUpdate { copy(descripcion = it) } },
                    label = { Text(stringResource(id = R.string.descripcion)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
                if (formState.showErrors && formState.descripcion.isBlank()) {
                    Text(stringResource(id = R.string.error_descripcion_vacia), color = Color.Red, style = MaterialTheme.typography.labelSmall)
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
                    Text(stringResource(id = R.string.error_fecha_fin), color = Color.Red, style = MaterialTheme.typography.labelSmall)
                }

                Text(stringResource(id = R.string.hora_fin), style = MaterialTheme.typography.labelSmall)
                OutlinedButton(
                    onClick = { showTimePicker { onUpdate { copy(horaFin = it) } } },
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
                        onCheckedChange = { onUpdate { copy(cumplido = it) } }
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
                    onUpdate = { viewModel.actualizarCampo(it) }
                )
            }
        }
    }
}