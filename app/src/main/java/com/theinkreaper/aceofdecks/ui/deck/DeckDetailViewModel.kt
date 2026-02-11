package com.theinkreaper.aceofdecks.ui.deck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theinkreaper.aceofdecks.data.AppRepository
import com.theinkreaper.aceofdecks.data.CardEntity
import com.theinkreaper.aceofdecks.data.DeckEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DeckDetailViewModel(
    private val repository: AppRepository
) : ViewModel() {
    private val _currentDeck = MutableStateFlow<DeckEntity?>(null)
    val currentDeck: StateFlow<DeckEntity?> = _currentDeck.asStateFlow()

    private val _cards = MutableStateFlow<List<CardEntity>>(emptyList())
    val cards: StateFlow<List<CardEntity>> = _cards.asStateFlow()

    fun loadDeck(deckId: Long) {
        viewModelScope.launch {
            _currentDeck.value = repository.getDeckById(deckId)

            repository.getCardsForDeck(deckId).collect { loadedCards ->
                _cards.value = loadedCards
            }
        }
    }

    fun deleteCard(card: CardEntity) {
        viewModelScope.launch {
            repository.deleteCard(card)
        }
    }

    fun updateDeckSettings(studyLimit: Int, quizLimit: Int) {
        val current = _currentDeck.value ?: return

        viewModelScope.launch {
            val updatedDeck = current.copy(
                studyLimitPerSession = studyLimit,
                quizLimitPerSession = quizLimit
            )
            repository.updateDeck(updatedDeck)

            _currentDeck.value = updatedDeck
        }
    }
}