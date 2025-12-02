package com.dev.uiElements

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import com.dev.Camara.AudiosHelper
import com.dev.Camara.ImagenHelper
import com.dev.Camara.VideosHelper
import com.dev.Dao.SubNotificacion
import com.dev.notifications.SubNotificacionReceiver

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

    // Inicializar formulario con datos del recordatorio
    fun inicializarFormulario(recordatorio: Recordatorio) {
        _formState.value = RecordatorioFormState(
            titulo = recordatorio.titulo,
            descripcion = recordatorio.descripcion,
            fechaInicio = recordatorio.fechaInicio ?: "",
            horaInicio = recordatorio.horaInicio ?: "",
            fechaFin = recordatorio.fechaFin ?: "",
            horaFin = recordatorio.horaFin ?: "",
            cumplido = recordatorio.cumplido,
            imagenesUri = recordatorio.imagenesUri,
            videosUri = recordatorio.videosUri,
            audiosUri = recordatorio.audiosUri,
            showErrors = false
        )
        _mostrarDialogo.value = true
    }

    //Actualizar campos del formulario
    fun actualizarCampo(update: RecordatorioFormState.() -> RecordatorioFormState) {
        _formState.value = _formState.value.update()
    }

    //Validar y construir objeto Recordatorio
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
                imagenesUri = state.imagenesUri,
                videosUri = state.videosUri,
                audiosUri = state.audiosUri,
                subnotificaciones = state.subnotificaciones
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
                (context as? MainActivity)?.programarSubnotificaciones(context, actualizado)
                _snackbarMessage.value = "Recordatorio editado"
                _mostrarDialogo.value = false
                onSave(actualizado)
            }
        }
    }




    fun updateFormState(transform: RecordatorioFormState.() -> RecordatorioFormState) {
        _formState.value = _formState.value.transform()
    }

    fun agregarSubnotificacion(sub: SubNotificacion) {
        updateFormState { copy(subnotificaciones = subnotificaciones + sub) }
    }

    fun eliminarSubnotificacion(context: Context, sub: SubNotificacion) {
        // Actualizar el formulario
        updateFormState { copy(subnotificaciones = subnotificaciones - sub) }

        //Cancelar la alarma en el sistema
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, SubNotificacionReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            sub.hashCode(), // mismo requestCode que usaste al programar
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)

        //Actualizar BD si el recordatorio ya existe
        recordatorio.value?.let { actual ->
            val actualizado = actual.copy(subnotificaciones = actual.subnotificaciones - sub)
            viewModelScope.launch { repository.update(actualizado) }
        }
    }

    // Manejo de imágenes
    fun eliminarImagen(context: Context, uri: String) {
        ImagenHelper.eliminarImagen(context, uri)
        _formState.value = _formState.value.copy(
            imagenesUri = _formState.value.imagenesUri - uri
        )
    }

    fun agregarImagen(context: Context, nuevaUri: Uri) {
        _formState.value = _formState.value.copy(
            imagenesUri = _formState.value.imagenesUri + nuevaUri.toString()
        )
    }

    // Manejo de videos
    fun eliminarVideo(context: Context, uri: String) {
        val eliminado = VideosHelper.eliminarVideo(context, uri)
        if (eliminado) {
            _formState.value = _formState.value.copy(
                videosUri = _formState.value.videosUri - uri
            )
            recordatorio.value?.let { actual ->
                val actualizado = actual.copy(videosUri = actual.videosUri - uri)
                viewModelScope.launch { repository.update(actualizado) }
            }
        }
    }

    fun agregarVideo(context: Context, nuevaUri: Uri) {
        val fd = context.contentResolver.openFileDescriptor(nuevaUri, "r")
        if (fd != null) {
            _formState.value = _formState.value.copy(
                videosUri = _formState.value.videosUri + nuevaUri.toString()
            )
            recordatorio.value?.let { actual ->
                val actualizado = actual.copy(videosUri = actual.videosUri + nuevaUri.toString())
                viewModelScope.launch { repository.update(actualizado) }
            }
        } else {
            Toast.makeText(context, "El nuevo video no es válido", Toast.LENGTH_SHORT).show()
        }
    }

    fun agregarAudio(context: Context, nuevaUri: Uri) {
        _formState.value = _formState.value.copy(
            audiosUri = _formState.value.audiosUri + nuevaUri.toString()
        )
        recordatorio.value?.let { actual ->
            val actualizado = actual.copy(audiosUri = actual.audiosUri + nuevaUri.toString())
            viewModelScope.launch { repository.update(actualizado) }
        }
    }

    fun eliminarAudio(context: Context, uri: String) {
        AudiosHelper.eliminarAudio(context, uri) // 👈 helper para borrar archivo físico
        _formState.value = _formState.value.copy(
            audiosUri = _formState.value.audiosUri - uri
        )
        recordatorio.value?.let { actual ->
            val actualizado = actual.copy(audiosUri = actual.audiosUri - uri)
            viewModelScope.launch { repository.update(actualizado) }
        }
    }

    fun cerrarDialogo() {
        _mostrarDialogo.value = false
    }

    fun consumirSnackbar() {
        _snackbarMessage.value = null
    }
}
