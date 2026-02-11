package com.theinkreaper.aceofdecks.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM decks ORDER BY orderIndex ASC")
    fun getAllDecks(): Flow<List<DeckEntity>>
    @Update
    suspend fun updateDecks(decks: List<DeckEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: DeckEntity): Long

    @Delete
    suspend fun deleteDeck(deck: DeckEntity)

    @Query("SELECT * FROM decks WHERE id = :id LIMIT 1")
    suspend fun getDeckById(id: Long): DeckEntity?

    @Query("SELECT * FROM cards WHERE deckId = :deckId")
    fun getCardsForDeck(deckId: Long): Flow<List<CardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CardEntity)

    @Update
    suspend fun updateCard(card: CardEntity)

    @Delete
    suspend fun deleteCard(card: CardEntity)

    @Query("SELECT * FROM cards WHERE deckId = :deckId AND nextReview <= :currentTime")
    suspend fun getCardsDueForReview(deckId: Long, currentTime: Long): List<CardEntity>

    @Query("SELECT * FROM cards WHERE deckId = :deckId AND type = 'TEST'")
    suspend fun getTestCardsForDeck(deckId: Long): List<CardEntity>

    @Query("SELECT * FROM cards WHERE id = :id LIMIT 1")
    suspend fun getCardById(id: Long): CardEntity?

    @Update
    suspend fun updateDeck(deck: DeckEntity)

    @Query("SELECT * FROM cards WHERE deckId = :deckId")
    suspend fun getCardsSync(deckId: Long): List<CardEntity>
}