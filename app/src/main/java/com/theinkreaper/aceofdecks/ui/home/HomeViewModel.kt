package com.theinkreaper.aceofdecks.ui.home

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theinkreaper.aceofdecks.R
import com.theinkreaper.aceofdecks.data.AppRepository
import com.theinkreaper.aceofdecks.data.CardEntity
import com.theinkreaper.aceofdecks.data.CardType
import com.theinkreaper.aceofdecks.data.DataExchangeManager
import com.theinkreaper.aceofdecks.data.DeckEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: AppRepository,
    private val dataExchange: DataExchangeManager,
    private val context: Context
) : ViewModel() {

    private val _allDecks = MutableStateFlow<List<DeckEntity>>(emptyList())
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    val decks = _searchText.combine(_allDecks) { text, decks ->
        if (text.isBlank()) decks else decks.filter { it.name.contains(text, ignoreCase = true) }
    }

    var deckPendingExport: DeckEntity? = null

    init {
        loadDecks()
    }

    private fun loadDecks() {
        viewModelScope.launch {
            repository.getAllDecks().collect { deckList ->
                _allDecks.value = deckList
                if (deckList.isEmpty()) {
                    createTutorialDeck()
                }
            }
        }
    }

    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

    private fun createTutorialDeck() {
        viewModelScope.launch {
            val deckId = repository.createDeck(context.getString(R.string.tutorial_deck_name))

            repository.saveCard(CardEntity(
                deckId = deckId,
                type = CardType.FLASHCARD,
                question = context.getString(R.string.tut_c1_q),
                answer = context.getString(R.string.tut_c1_a),
                explanation = context.getString(R.string.tut_c1_expl)
            ))

            repository.saveCard(CardEntity(
                deckId = deckId,
                type = CardType.TEST,
                question = context.getString(R.string.tut_c2_q),
                answer = context.getString(R.string.tut_c2_a),
                wrongAnswers = listOf(
                    context.getString(R.string.tut_c2_dist1),
                    context.getString(R.string.tut_c2_dist2)
                )
            ))

            repository.saveCard(CardEntity(
                deckId = deckId,
                type = CardType.FLASHCARD,
                question = context.getString(R.string.tut_c3_q),
                answer = context.getString(R.string.tut_c3_a)
            ))

            repository.saveCard(CardEntity(
                deckId = deckId,
                type = CardType.FLASHCARD,
                question = context.getString(R.string.tut_c4_q),
                answer = context.getString(R.string.tut_c4_a)
            ))

            repository.saveCard(CardEntity(
                deckId = deckId,
                type = CardType.FLASHCARD,
                question = context.getString(R.string.tut_c5_q),
                answer = context.getString(R.string.tut_c5_a),
                explanation = context.getString(R.string.tut_c5_expl)
            ))
        }
    }

    fun createDeck(name: String) { viewModelScope.launch { repository.createDeck(name) } }

    fun renameDeck(deck: DeckEntity, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch { repository.updateDeck(deck.copy(name = newName)) }
    }

    fun deleteDeck(deck: DeckEntity) { viewModelScope.launch { repository.deleteDeck(deck) } }

    fun importDeck(uri: Uri) {
        viewModelScope.launch {
            try { dataExchange.importDeck(uri) } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun exportDeck(uri: Uri) {
        val deck = deckPendingExport ?: return
        viewModelScope.launch {
            try {
                dataExchange.exportDeck(deck.id, uri)
                deckPendingExport = null
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun updateDeckOrder(newOrder: List<DeckEntity>) {
        viewModelScope.launch {
            val updatedDecks = newOrder.mapIndexed { index, deck ->
                deck.copy(orderIndex = index)
            }
            repository.updateDecksOrder(updatedDecks)
        }
    }
}