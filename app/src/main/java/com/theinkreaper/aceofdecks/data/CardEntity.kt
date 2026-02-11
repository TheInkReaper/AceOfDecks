package com.theinkreaper.aceofdecks.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class CardType {
    FLASHCARD,
    TEST
}

@Entity(
    tableName = "cards",
    foreignKeys = [
        ForeignKey(
            entity = DeckEntity::class,
            parentColumns = ["id"],
            childColumns = ["deckId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["deckId"])]
)
data class CardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val deckId: Long,
    val type: CardType,

    val question: String,
    val answer: String,
    val wrongAnswers: List<String> = emptyList(),
    val explanation: String = "",
    val nextReview: Long = System.currentTimeMillis(),
    val interval: Int = 0,
    val easeFactor: Float = 2.5f,
    val reviewCount: Int = 0
)