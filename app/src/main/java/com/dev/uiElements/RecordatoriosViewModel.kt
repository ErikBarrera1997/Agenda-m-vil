package com.dev.uiElements

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
import com.dev.Camara.ImagenHelper
import com.dev.Camara.VideosHelper
import com.dev.Data.RecordatorioFormState
import com.dev.agenda_movil.R
import com.dev.notifications.SubNotificacionReceiver

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


    fun updateFormState(transform: RecordatorioFormState.() -> RecordatorioFormState) {
        _formState.value = _formState.value.transform()
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

    fun guardar(context: Context, onSuccess: () -> Unit) {
        val recordatorio = validarYConstruir()
        if (recordatorio != null) {
            viewModelScope.launch {
                repository.insert(recordatorio)
                programarSubnotificaciones(context, recordatorio)
                onSuccess()
            }
        }
    }

    fun programarSubnotificaciones(context: Context, recordatorio: Recordatorio) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        recordatorio.subnotificaciones.forEach { sub ->
            val intent = Intent(context, SubNotificacionReceiver::class.java).apply {
                putExtra("titulo", recordatorio.titulo)
                putExtra("descripcion", recordatorio.descripcion)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                sub.hashCode(), // id único para cada subnotificación
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val intervaloMillis = sub.intervaloMinutos * 60 * 1000L
            val horaInicio = System.currentTimeMillis()

            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                horaInicio,
                intervaloMillis,
                pendingIntent
            )
        }
    }


    private val _formState = mutableStateOf(RecordatorioFormState())
    val formState: State<RecordatorioFormState> = _formState

    fun actualizarCampo(update: RecordatorioFormState.() -> RecordatorioFormState) {
        _formState.value = _formState.value.update()
    }

    //AQUI SE CONSTRUYE EL RECORDATORIO
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

    fun limpiarFormulario() {
        _formState.value = RecordatorioFormState() // ya inicializa listas vacías
    }

    //------------------------- Negocio ----------------------------------------------------------------//
    fun editar(recordatorio: Recordatorio) {
        viewModelScope.launch {
            repository.update(recordatorio)
        }
    }

    fun eliminar(context: Context, recordatorio: Recordatorio) {
        viewModelScope.launch {
            // Cancelar subnotificaciones
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            recordatorio.subnotificaciones.forEach { sub ->
                val intent = Intent(context, SubNotificacionReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    sub.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pendingIntent) //cancela la alarma
            }

            // Borrar archivos asociados
            recordatorio.imagenesUri.forEach { uri -> ImagenHelper.eliminarImagen(context, uri) }
            recordatorio.videosUri.forEach { uri -> VideosHelper.eliminarVideo(context, uri) }
            //recordatorio.audiosUri.forEach { uri -> AudiosHelper.eliminarAudio(context, uri) }

            repository.delete(recordatorio)
        }
    }


    fun getById(id: Int): Flow<Recordatorio?> {
        return repository.getById(id)
    }

    // Control de UI
    fun mostrarDialogoAgregar() { _mostrarDialogo.value = true }
    fun cerrarDialogo() { _mostrarDialogo.value = false }
    fun mostrarSnackbar(mensaje: Int) { _snackbarMessage.value = mensaje }
    fun consumirSnackbar() { _snackbarMessage.value = null }

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