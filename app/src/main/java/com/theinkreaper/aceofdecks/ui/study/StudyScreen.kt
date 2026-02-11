package com.theinkreaper.aceofdecks.ui.study

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.theinkreaper.aceofdecks.R
import com.theinkreaper.aceofdecks.domain.UserGrade
import org.koin.androidx.compose.koinViewModel
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    deckId: Long,
    onFinish: () -> Unit,
    viewModel: StudyViewModel = koinViewModel()
) {
    LaunchedEffect(deckId) { viewModel.loadSession(deckId) }

    val currentCard by viewModel.currentCard.collectAsState()
    val showAnswer by viewModel.showAnswer.collectAsState()
    val isFinished by viewModel.isFinished.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.study_mode_flashcards)) },
                navigationIcon = {
                    IconButton(onClick = onFinish) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_cancel))
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), contentAlignment = Alignment.Center) {
            if (isFinished) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🎉", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.study_no_more_cards),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onFinish) {
                        Text(stringResource(R.string.action_return))
                    }
                }
            } else {
                currentCard?.let { card ->
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                        Card(modifier = Modifier.weight(1f).fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp)
                                    .verticalScroll(rememberScrollState()),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = card.question,
                                    style = MaterialTheme.typography.headlineMedium,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold
                                )

                                if (showAnswer) {
                                    Spacer(modifier = Modifier.height(32.dp))
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(32.dp))

                                    Text(
                                        text = card.answer,
                                        style = MaterialTheme.typography.headlineSmall,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    if (card.explanation.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = card.explanation,
                                            style = MaterialTheme.typography.bodyLarge,
                                            textAlign = TextAlign.Center,
                                            fontStyle = FontStyle.Italic,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (!showAnswer) {
                            Button(onClick = { viewModel.toggleAnswer() }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                                Text(stringResource(R.string.btn_show_answer))
                            }
                        } else {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SrsButton(stringResource(R.string.grade_again), Color(0xFFE57373), Modifier.weight(1f)) { viewModel.gradeCard(UserGrade.AGAIN) }
                                SrsButton(stringResource(R.string.grade_hard), Color(0xFFFFB74D), Modifier.weight(1f)) { viewModel.gradeCard(UserGrade.HARD) }
                                SrsButton(stringResource(R.string.grade_good), Color(0xFF81C784), Modifier.weight(1f)) { viewModel.gradeCard(UserGrade.GOOD) }
                                SrsButton(stringResource(R.string.grade_easy), Color(0xFF64B5F6), Modifier.weight(1f)) { viewModel.gradeCard(UserGrade.EASY) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SrsButton(text: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text, fontSize = 12.sp, color = Color.White)
    }
}