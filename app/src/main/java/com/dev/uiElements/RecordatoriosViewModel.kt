package com.dev.uiElements

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.Dao.Recordatorio
import kotlinx.coroutines.launch

class RecordatoriosViewModel(private val repository: RecordatoriosRepository) : ViewModel() {
    private val _recordatorios = mutableStateListOf<Recordatorio>()
    val recordatorios: List<Recordatorio> get() = _recordatorios

    init {
        viewModelScope.launch {
            _recordatorios.clear()
            _recordatorios.addAll(repository.getAll())
        }
    }

    fun agregarRecordatorio(recordatorio: Recordatorio) {
        viewModelScope.launch {
            repository.insert(recordatorio)
            _recordatorios.clear()
            _recordatorios.addAll(repository.getAll())
        }
    }

    fun editarRecordatorio(actualizado: Recordatorio) {
        viewModelScope.launch {
            repository.update(actualizado)
            _recordatorios.clear()
            _recordatorios.addAll(repository.getAll())
        }
    }
}