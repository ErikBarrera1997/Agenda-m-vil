package com.dev.uiElements

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.Dao.Recordatorio
import com.dev.Dao.RecordatoriosRepository
import com.dev.Data.RecordatorioFormState
import com.dev.agenda_movil.MainActivity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.compose.runtime.State

class EditComposablesViewModel(
    private val repository: RecordatoriosRepository,
    private val recordatorioId: Int
) : ViewModel() {

    val recordatorio: StateFlow<Recordatorio?> =
        repository.getById(recordatorioId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _formState = mutableStateOf(RecordatorioFormState())
    val formState: State<RecordatorioFormState> = _formState

    private val _mostrarDialogo = mutableStateOf(false)
    val mostrarDialogo: State<Boolean> = _mostrarDialogo

    private val _snackbarMessage = mutableStateOf<String?>(null)
    val snackbarMessage: State<String?> = _snackbarMessage

    fun inicializarFormulario(recordatorio: Recordatorio) {
        _formState.value = RecordatorioFormState(
            titulo = recordatorio.titulo,
            descripcion = recordatorio.descripcion,
            fechaInicio = recordatorio.fechaInicio ?: "",
            horaInicio = recordatorio.horaInicio ?: "",
            fechaFin = recordatorio.fechaFin ?: "",
            horaFin = recordatorio.horaFin ?: "",
            cumplido = recordatorio.cumplido
        )
        _mostrarDialogo.value = true
    }

    fun actualizarCampo(update: RecordatorioFormState.() -> RecordatorioFormState) {
        _formState.value = _formState.value.update()
    }

    fun validarYConstruir(): Recordatorio? {
        val state = _formState.value
        val camposValidos = state.titulo.isNotBlank() &&
                state.descripcion.isNotBlank() &&
                state.fechaFin.isNotBlank() &&
                state.horaFin.isNotBlank()

        return if (camposValidos) {
            recordatorio.value?.copy(
                titulo = state.titulo,
                descripcion = state.descripcion,
                fechaInicio = state.fechaInicio.ifBlank { null },
                horaInicio = state.horaInicio.ifBlank { null },
                fechaFin = state.fechaFin.ifBlank { null },
                horaFin = state.horaFin.ifBlank { null },
                cumplido = state.cumplido
            )
        } else {
            _formState.value = state.copy(showErrors = true)
            null
        }
    }

    fun guardar(context: Context, onSave: (Recordatorio) -> Unit) {
        validarYConstruir()?.let { actualizado ->
            viewModelScope.launch {
                repository.update(actualizado)
                (context as? MainActivity)?.programarNotificacionesPorFechas(actualizado)
                _snackbarMessage.value = "Recordatorio editado"
                _mostrarDialogo.value = false
                onSave(actualizado)
            }
        }
    }

    fun cerrarDialogo() {
        _mostrarDialogo.value = false
    }

    fun consumirSnackbar() {
        _snackbarMessage.value = null
    }
}