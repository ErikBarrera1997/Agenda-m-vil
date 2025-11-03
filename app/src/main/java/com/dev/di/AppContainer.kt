package com.dev.di

import android.content.Context
import com.dev.Dao.AppDatabase
import com.dev.Dao.OfflineRecordatoriosRepository
import com.dev.Dao.RecordatoriosRepository

class AppContainer(context : Context) {

    val recordatoriosRepository: RecordatoriosRepository by lazy {
        OfflineRecordatoriosRepository(AppDatabase.getDatabase(context).recordatorioDao())
    }

}