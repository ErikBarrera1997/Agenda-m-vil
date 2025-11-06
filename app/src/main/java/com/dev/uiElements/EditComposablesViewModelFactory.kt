package com.dev.uiElements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dev.Dao.RecordatoriosRepository

class EditComposablesViewModelFactory(
    private val repository: RecordatoriosRepository,
    private val recordatorioId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditComposablesViewModel(repository, recordatorioId) as T
    }
}
