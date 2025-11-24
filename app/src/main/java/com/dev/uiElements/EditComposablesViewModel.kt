package com.dev.uiElements

import android.content.Context
import android.net.Uri
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
import com.dev.Camara.ImagenHelper

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

    // ✅ Inicializar formulario con datos del recordatorio
    fun inicializarFormulario(recordatorio: Recordatorio) {
        _formState.value = RecordatorioFormState(
            titulo = recordatorio.titulo,
            descripcion = recordatorio.descripcion,
            fechaInicio = recordatorio.fechaInicio ?: "",
            horaInicio = recordatorio.horaInicio ?: "",
            fechaFin = recordatorio.fechaFin ?: "",
            horaFin = recordatorio.horaFin ?: "",
            cumplido = recordatorio.cumplido,
            imagenUri = recordatorio.imagenUri
        )
        _mostrarDialogo.value = true
    }

    // ✅ Actualizar campos del formulario
    fun actualizarCampo(update: RecordatorioFormState.() -> RecordatorioFormState) {
        _formState.value = _formState.value.update()
    }

    // ✅ Validar y construir objeto Recordatorio
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
                cumplido = state.cumplido,
                imagenUri = state.imagenUri
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

    fun eliminarImagen(context: Context) {
        formState.value.imagenUri?.let { uri ->
            ImagenHelper.eliminarImagen(context, uri) // borra archivo físico y notifica
            _formState.value = _formState.value.copy(imagenUri = null)
        }
    }

    fun cambiarImagen(context: Context, nuevaUri: Uri) {
        formState.value.imagenUri?.let { uri ->
            ImagenHelper.eliminarImagen(context, uri) // borra la vieja
        }
        _formState.value = _formState.value.copy(imagenUri = nuevaUri.toString())
    }

    fun cerrarDialogo() {
        _mostrarDialogo.value = false
    }

    fun consumirSnackbar() {
        _snackbarMessage.value = null
    }
}
