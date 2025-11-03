package com.dev.agenda_movil

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dev.uiElements.RecordatoriosViewModel

object AppViewModelProvider {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AgendaApplication
            RecordatoriosViewModel(app.appContainer.recordatoriosRepository)
        }
    }
}