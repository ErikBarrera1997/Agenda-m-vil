package com.dev.Data

import androidx.room.TypeConverter
import com.dev.Dao.SubNotificacion
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromList(list: List<String>?): String? = list?.joinToString(",")

    @TypeConverter
    fun toList(data: String?): List<String> =
        data?.split(",")?.filter { it.isNotBlank() } ?: emptyList()

    @TypeConverter
    fun fromSubNotificaciones(value: List<SubNotificacion>): String = Gson().toJson(value)

    @TypeConverter
    fun toSubNotificaciones(value: String): List<SubNotificacion> {
        val type = object : TypeToken<List<SubNotificacion>>() {}.type
        return Gson().fromJson(value, type)
    }


}