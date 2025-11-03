package com.dev.Dao

import kotlinx.coroutines.flow.Flow

class OfflineRecordatoriosRepository(
    private val dao: RecordatorioDao
) : RecordatoriosRepository {

    override fun getAll(): Flow<List<Recordatorio>> = dao.getAll()
    override fun getById(id: Int): Flow<Recordatorio?> = dao.getById(id)
    override suspend fun insert(recordatorio: Recordatorio) = dao.insert(recordatorio)
    override suspend fun update(recordatorio: Recordatorio) = dao.update(recordatorio)
    override suspend fun delete(recordatorio: Recordatorio) = dao.delete(recordatorio)
}
