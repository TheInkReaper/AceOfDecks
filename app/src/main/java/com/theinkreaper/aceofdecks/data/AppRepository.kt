package com.theinkreaper.aceofdecks.data

import com.theinkreaper.aceofdecks.domain.SrsAlgorithm
import com.theinkreaper.aceofdecks.domain.UserGrade
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val dao: AppDao,
    private val srsAlgorithm: SrsAlgorithm
) {

    fun getAllDecks(): Flow<List<DeckEntity>> = dao.getAllDecks()

    suspend fun createDeck(name: String): Long {
        return dao.insertDeck(DeckEntity(name = name))
    }

    suspend fun deleteDeck(deck: DeckEntity) = dao.deleteDeck(deck)

    suspend fun getDeckById(id: Long) = dao.getDeckById(id)


    fun getCardsForDeck(deckId: Long): Flow<List<CardEntity>> = dao.getCardsForDeck(deckId)

    suspend fun saveCard(card: CardEntity) = dao.insertCard(card)

    suspend fun deleteCard(card: CardEntity) = dao.deleteCard(card)

    suspend fun getTestCards(deckId: Long) = dao.getTestCardsForDeck(deckId)

    suspend fun getCardsDue(deckId: Long): List<CardEntity> {
        val now = System.currentTimeMillis()
        return dao.getCardsDueForReview(deckId, now)
    }

    suspend fun processCardReview(card: CardEntity, grade: UserGrade) {
        val result = srsAlgorithm.calculateNextReview(
            currentInterval = card.interval,
            currentEaseFactor = card.easeFactor,
            grade = grade
        )

        val updatedCard = card.copy(
            nextReview = result.nextReview,
            interval = result.interval,
            easeFactor = result.easeFactor,
            reviewCount = card.reviewCount + 1
        )

        dao.updateCard(updatedCard)
    }

    suspend fun getCardById(id: Long): CardEntity? = dao.getCardById(id)

    suspend fun updateDeck(deck: DeckEntity) = dao.updateDeck(deck)

    suspend fun getCardsForDeckSync(deckId: Long): List<CardEntity> = dao.getCardsSync(deckId)

    suspend fun updateDecksOrder(decks: List<DeckEntity>) = dao.updateDecks(decks)
}