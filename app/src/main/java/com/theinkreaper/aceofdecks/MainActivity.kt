package com.theinkreaper.aceofdecks

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.theinkreaper.aceofdecks.ui.deck.DeckDetailScreen
import com.theinkreaper.aceofdecks.ui.editor.CardEditorScreen
import com.theinkreaper.aceofdecks.ui.home.HomeScreen
import com.theinkreaper.aceofdecks.ui.quiz.QuizScreen
import com.theinkreaper.aceofdecks.ui.study.StudyScreen
import com.theinkreaper.aceofdecks.ui.theme.AceOfDecksTheme
import org.koin.androidx.compose.KoinAndroidContext

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KoinAndroidContext {
                val sharedPref = getPreferences(Context.MODE_PRIVATE)
                val systemDark = isSystemInDarkTheme()
                val isDarkStored = remember { sharedPref.getBoolean("DARK_MODE", systemDark) }

                var isDarkTheme by remember { mutableStateOf(isDarkStored) }

                val toggleTheme = {
                    val newMode = !isDarkTheme
                    isDarkTheme = newMode
                    with(sharedPref.edit()) {
                        putBoolean("DARK_MODE", newMode)
                        apply()
                    }
                }

                AceOfDecksTheme(darkTheme = isDarkTheme, dynamicColor = false) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation(
                            isDarkTheme = isDarkTheme,
                            onToggleTheme = toggleTheme
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    BackHandler(enabled = currentScreen !is Screen.Home) {
        currentScreen = when (currentScreen) {
            is Screen.CardEditor -> Screen.DeckDetail((currentScreen as Screen.CardEditor).deckId)
            is Screen.Study -> Screen.DeckDetail((currentScreen as Screen.Study).deckId)
            is Screen.Quiz -> Screen.DeckDetail((currentScreen as Screen.Quiz).deckId)
            is Screen.DeckDetail -> Screen.Home
            else -> Screen.Home
        }
    }

    when (val screen = currentScreen) {
        is Screen.Home -> {
            HomeScreen(
                onDeckClick = { deckId -> currentScreen = Screen.DeckDetail(deckId) },
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )
        }
        is Screen.DeckDetail -> {
            DeckDetailScreen(
                deckId = screen.deckId,
                onBack = { currentScreen = Screen.Home },
                onAddCard = { deckId -> currentScreen = Screen.CardEditor(deckId, null) },
                onEditCard = { cardId -> currentScreen = Screen.CardEditor(screen.deckId, cardId) },
                onStudy = { deckId -> currentScreen = Screen.Study(deckId) },
                onQuiz = { deckId -> currentScreen = Screen.Quiz(deckId) }
            )
        }
        is Screen.CardEditor -> {
            CardEditorScreen(
                deckId = screen.deckId,
                cardIdToEdit = screen.cardIdToEdit,
                onBack = { currentScreen = Screen.DeckDetail(screen.deckId) }
            )
        }
        is Screen.Study -> {
            StudyScreen(
                deckId = screen.deckId,
                onFinish = { currentScreen = Screen.DeckDetail(screen.deckId) }
            )
        }
        is Screen.Quiz -> {
            QuizScreen(
                deckId = screen.deckId,
                onFinish = { currentScreen = Screen.DeckDetail(screen.deckId) }
            )
        }
    }
}

sealed class Screen {
    object Home : Screen()
    data class DeckDetail(val deckId: Long) : Screen()
    data class CardEditor(val deckId: Long, val cardIdToEdit: Long? = null) : Screen()
    data class Study(val deckId: Long) : Screen()
    data class Quiz(val deckId: Long) : Screen()
}