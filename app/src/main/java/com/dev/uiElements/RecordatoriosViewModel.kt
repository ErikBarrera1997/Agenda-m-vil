package com.dev.uiElements

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.Dao.Recordatorio
import com.dev.Dao.RecordatoriosRepository
import com.dev.agenda_movil.MainActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.compose.runtime.State
import com.dev.Data.RecordatorioFormState
import com.dev.agenda_movil.R

class RecordatoriosViewModel(
    private val repository: RecordatoriosRepository
) : ViewModel() {

    val recordatorios: StateFlow<List<Recordatorio>> =
        repository.getAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedRecordatorio = mutableStateOf<Recordatorio?>(null)
    val selectedRecordatorio: State<Recordatorio?> = _selectedRecordatorio

    fun selectRecordatorio(recordatorio: Recordatorio?) {
        _selectedRecordatorio.value = recordatorio
    }

    private val _mostrarDialogo = mutableStateOf(false)
    val mostrarDialogo: State<Boolean> = _mostrarDialogo

    private val _snackbarMessage = mutableStateOf<Int?>(null)
    val snackbarMessage: State<Int?> = _snackbarMessage

    fun agregar(recordatorio: Recordatorio) {
        viewModelScope.launch {
            repository.insert(recordatorio)
        }
    }

    private val _formState = mutableStateOf(RecordatorioFormState())
    val formState: State<RecordatorioFormState> = _formState

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
            Recordatorio(
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

    fun limpiarFormulario() {
        _formState.value = RecordatorioFormState()
    }

    //------------------------- Negocio ----------------------------------------------------------------//
    fun editar(recordatorio: Recordatorio) {
        viewModelScope.launch {
            repository.update(recordatorio)
        }
    }

    fun eliminar(recordatorio: Recordatorio) {
        viewModelScope.launch {
            repository.delete(recordatorio)
        }
    }

    fun getById(id: Int): Flow<Recordatorio?> {
        return repository.getById(id)
    }

    // Control de UI
    fun mostrarDialogoAgregar() {
        _mostrarDialogo.value = true
    }

    fun cerrarDialogo() {
        _mostrarDialogo.value = false
    }

    fun mostrarSnackbar(mensaje: Int) {
        _snackbarMessage.value = mensaje
    }

    fun consumirSnackbar() {
        _snackbarMessage.value = null
    }

    fun agregarYNotificar(recordatorio: Recordatorio, context: Context) {
        agregar(recordatorio)
        (context as? MainActivity)?.programarNotificacionesPorFechas(recordatorio)
        cerrarDialogo()
        mostrarSnackbar(R.string.recordatorio_guardado)
    }

    fun editarYNotificar(recordatorio: Recordatorio, context: Context) {
        editar(recordatorio)
        (context as? MainActivity)?.programarNotificacionesPorFechas(recordatorio)
        cerrarDialogo()
        mostrarSnackbar(R.string.recordatorio_editado)
    }
}