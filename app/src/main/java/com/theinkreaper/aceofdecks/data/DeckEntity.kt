package com.theinkreaper.aceofdecks.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "decks")
data class DeckEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),

    val studyLimitPerSession: Int = 0,
    val quizLimitPerSession: Int = 20,

    val orderIndex: Int = 0
)