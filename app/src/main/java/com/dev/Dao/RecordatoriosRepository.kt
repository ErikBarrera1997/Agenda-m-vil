package com.dev.Dao

import kotlinx.coroutines.flow.Flow

interface RecordatoriosRepository {
    fun getAll(): Flow<List<Recordatorio>>
    fun getById(id: Int): Flow<Recordatorio?>
    suspend fun insert(recordatorio: Recordatorio)
    suspend fun update(recordatorio: Recordatorio)
    suspend fun delete(recordatorio: Recordatorio)
}
