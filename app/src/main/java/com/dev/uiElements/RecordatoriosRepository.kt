package com.dev.uiElements

import android.content.Context
import androidx.room.Room
import com.dev.Dao.AppDatabase
import com.dev.Dao.Recordatorio
import com.dev.Dao.RecordatorioDao

class RecordatoriosRepository(context: Context) {
    private val dao: RecordatorioDao = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "agenda-db"
    ).build().recordatorioDao()

    suspend fun getAll(): List<Recordatorio> = dao.getAll()
    suspend fun insert(recordatorio: Recordatorio) = dao.insert(recordatorio)
    suspend fun update(recordatorio: Recordatorio) = dao.update(recordatorio)
    suspend fun delete(recordatorio: Recordatorio) = dao.delete(recordatorio)

}
