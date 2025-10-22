package com.dev.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface RecordatorioDao {
    @Query("SELECT * FROM recordatorios ORDER BY id DESC")
    suspend fun getAll(): List<Recordatorio>

    @Insert
    suspend fun insert(recordatorio: Recordatorio)

    @Update
    suspend fun update(recordatorio: Recordatorio)

    @Delete
    suspend fun delete(recordatorio: Recordatorio)
}
