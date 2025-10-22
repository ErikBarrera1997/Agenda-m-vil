package com.dev.Dao

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Recordatorio::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recordatorioDao(): RecordatorioDao
}
