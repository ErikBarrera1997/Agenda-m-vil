package com.dev.uiElements

import android.content.Context
import com.dev.Dao.AppDatabase
import com.dev.Dao.RecordatoriosRepository
import com.dev.Dao.RecordatoriosRepositoryImpl

object RecordatoriosRepositoryProvider {
    fun provide(context: Context): RecordatoriosRepository {
        val dao = AppDatabase.getInstance(context).recordatorioDao()
        return RecordatoriosRepositoryImpl(dao)
    }
}
