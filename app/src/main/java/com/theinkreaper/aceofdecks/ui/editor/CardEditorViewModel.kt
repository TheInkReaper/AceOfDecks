package com.theinkreaper.aceofdecks.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theinkreaper.aceofdecks.data.AppRepository
import com.theinkreaper.aceofdecks.data.CardEntity
import com.theinkreaper.aceofdecks.data.CardType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CardEditorViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private var editingCardId: Long? = null
    private var originalCard: CardEntity? = null

    private val _type = MutableStateFlow(CardType.FLASHCARD)
    val type = _type.asStateFlow()

    private val _question = MutableStateFlow("")
    val question = _question.asStateFlow()

    private val _answer = MutableStateFlow("")
    val answer = _answer.asStateFlow()

    private val _explanation = MutableStateFlow("")
    val explanation = _explanation.asStateFlow()

    private val _distractors = MutableStateFlow<List<String>>(emptyList())
    val distractors = _distractors.asStateFlow()

    fun setType(newType: CardType) { _type.value = newType }
    fun setQuestion(text: String) { _question.value = text }
    fun setAnswer(text: String) { _answer.value = text }
    fun setExplanation(text: String) { _explanation.value = text }

    fun clearEditor() {
        editingCardId = null
        originalCard = null
        _type.value = CardType.FLASHCARD
        _question.value = ""
        _answer.value = ""
        _explanation.value = ""
        _distractors.value = emptyList()
    }

    fun loadCardToEdit(cardId: Long) {
        if (editingCardId == cardId) return

        editingCardId = cardId
        viewModelScope.launch {
            val card = repository.getCardById(cardId)
            if (card != null) {
                originalCard = card
                _type.value = card.type
                _question.value = card.question
                _answer.value = card.answer
                _explanation.value = card.explanation
                _distractors.value = card.wrongAnswers
            }
        }
    }

    fun addDistractor(value: String) {
        if (value.isNotBlank()) {
            _distractors.value = _distractors.value + value
        }
    }

    fun removeDistractor(index: Int) {
        val current = _distractors.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _distractors.value = current
        }
    }

    fun saveCard(deckId: Long, onSaved: () -> Unit) {
        val q = _question.value
        val a = _answer.value

        if (q.isBlank() || a.isBlank()) return

        viewModelScope.launch {
            if (editingCardId != null && originalCard != null) {
                val updatedCard = originalCard!!.copy(
                    type = _type.value,
                    question = q,
                    answer = a,
                    explanation = _explanation.value,
                    wrongAnswers = _distractors.value
                )
                repository.saveCard(updatedCard)
            } else {
                val newCard = CardEntity(
                    deckId = deckId,
                    type = _type.value,
                    question = q,
                    answer = a,
                    explanation = _explanation.value,
                    wrongAnswers = _distractors.value
                )
                repository.saveCard(newCard)
            }
            onSaved()
        }
    }
}