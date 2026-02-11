package com.theinkreaper.aceofdecks.ui.study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theinkreaper.aceofdecks.data.AppRepository
import com.theinkreaper.aceofdecks.data.CardEntity
import com.theinkreaper.aceofdecks.domain.UserGrade
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StudyViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private var studyQueue = listOf<CardEntity>()

    private val _currentCard = MutableStateFlow<CardEntity?>(null)
    val currentCard = _currentCard.asStateFlow()

    private val _isFinished = MutableStateFlow(false)
    val isFinished = _isFinished.asStateFlow()

    private val _showAnswer = MutableStateFlow(false)
    val showAnswer = _showAnswer.asStateFlow()

    fun loadSession(deckId: Long) {
        _isFinished.value = false
        _currentCard.value = null
        _showAnswer.value = false

        viewModelScope.launch {
            val deck = repository.getDeckById(deckId)
            val limit = deck?.studyLimitPerSession ?: 0

            var dueCards = repository.getCardsDue(deckId)

            if (limit > 0) {
                dueCards = dueCards.take(limit)
            }

            studyQueue = dueCards
            nextCard()
        }
    }

    private fun nextCard() {
        if (studyQueue.isNotEmpty()) {
            _currentCard.value = studyQueue[0]
            _showAnswer.value = false
        } else {
            _currentCard.value = null
            _isFinished.value = true
        }
    }

    fun toggleAnswer() {
        _showAnswer.value = true
    }

    fun gradeCard(grade: UserGrade) {
        val card = _currentCard.value ?: return

        viewModelScope.launch {
            repository.processCardReview(card, grade)

            studyQueue = studyQueue.drop(1)

            nextCard()
        }
    }
}