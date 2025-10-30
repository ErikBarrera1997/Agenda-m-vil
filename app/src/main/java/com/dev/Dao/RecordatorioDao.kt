package com.dev.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordatorioDao {

    @Query("SELECT * FROM recordatorios ORDER BY id ASC")
    fun getAll(): Flow<List<Recordatorio>>

    @Query("SELECT * FROM recordatorios WHERE id = :id")
    fun getById(id: Int): Flow<Recordatorio?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(recordatorio: Recordatorio)

    @Update
    suspend fun update(recordatorio: Recordatorio)

    @Delete
    suspend fun delete(recordatorio: Recordatorio)
}


