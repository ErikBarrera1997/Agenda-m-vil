package com.dev.Dao

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Recordatorio::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recordatorioDao(): RecordatorioDao
}

