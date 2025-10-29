package com.dev.uiElements

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RecordatoriosViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass == RecordatoriosViewModel::class.java) {
            val repository = RecordatoriosRepository(context)
            @Suppress("UNCHECKED_CAST")
            return RecordatoriosViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
