package com.example.barriovivo.data.database

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Conversores de tipos para Room.
 *
 * Room no soporta nativamente los tipos de fecha de Java 8.
 * Esta clase proporciona conversiones bidireccionales entre
 * String (almacenamiento) y LocalDate/LocalDateTime (uso en codigo).
 *
 * Formato utilizado: ISO-8601 (yyyy-MM-dd y yyyy-MM-ddTHH:mm:ss)
 */
class DateTimeConverters {
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.format(dateTimeFormatter)
    }

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, dateTimeFormatter) }
    }

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? {
        return value?.format(dateFormatter)
    }

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it, dateFormatter) }
    }
}

