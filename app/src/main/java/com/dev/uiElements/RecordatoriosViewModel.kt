package com.dev.uiElements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.Dao.Recordatorio
import com.dev.Dao.RecordatoriosRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecordatoriosViewModel(
    private val repository: RecordatoriosRepository
) : ViewModel() {

    val recordatorios: StateFlow<List<Recordatorio>> =
        repository.getAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun agregar(recordatorio: Recordatorio) {
        viewModelScope.launch {
            repository.insert(recordatorio)
        }
    }

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

}


