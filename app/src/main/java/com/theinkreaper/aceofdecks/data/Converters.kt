package com.theinkreaper.aceofdecks.data

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromCardType(value: CardType): String {
        return value.name
    }

    @TypeConverter
    fun toCardType(value: String): CardType {
        return CardType.valueOf(value)
    }

    @TypeConverter
    fun fromList(value: List<String>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toList(value: String): List<String> {
        return try {
            Json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
}