package com.theinkreaper.aceofdecks.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theinkreaper.aceofdecks.data.AppRepository
import com.theinkreaper.aceofdecks.data.CardEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuizViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val _currentCard = MutableStateFlow<CardEntity?>(null)
    val currentCard = _currentCard.asStateFlow()

    private val _options = MutableStateFlow<List<String>>(emptyList())
    val options = _options.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score = _score.asStateFlow()

    private val _totalQuestions = MutableStateFlow(0)
    val totalQuestions = _totalQuestions.asStateFlow()

    private val _isFinished = MutableStateFlow(false)
    val isFinished = _isFinished.asStateFlow()

    private var quizQueue = listOf<CardEntity>()

    fun loadQuiz(deckId: Long) {
        viewModelScope.launch {
            _score.value = 0
            _isFinished.value = false

            val deck = repository.getDeckById(deckId)
            val limit = deck?.quizLimitPerSession ?: 20

            val allTestCards = repository.getTestCards(deckId).shuffled()

            val limitedCards = allTestCards.take(limit)

            _totalQuestions.value = limitedCards.size
            quizQueue = limitedCards
            nextQuestion()
        }
    }

    private fun nextQuestion() {
        if (quizQueue.isNotEmpty()) {
            val card = quizQueue[0]
            _currentCard.value = card

            val allOptions = (card.wrongAnswers + card.answer).shuffled()
            _options.value = allOptions

            quizQueue = quizQueue.drop(1)
        } else {
            _isFinished.value = true
        }
    }

    fun submitAnswer(selectedAnswer: String) {
        val current = _currentCard.value ?: return

        if (selectedAnswer == current.answer) {
            _score.value += 1
        }

        nextQuestion()
    }
}