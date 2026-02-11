package com.theinkreaper.aceofdecks.ui.quiz

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.theinkreaper.aceofdecks.R
import org.koin.androidx.compose.koinViewModel
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    deckId: Long,
    onFinish: () -> Unit,
    viewModel: QuizViewModel = koinViewModel()
) {
    LaunchedEffect(deckId) {
        viewModel.loadQuiz(deckId)
    }

    val currentCard by viewModel.currentCard.collectAsState()
    val options by viewModel.options.collectAsState()
    val isFinished by viewModel.isFinished.collectAsState()
    val score by viewModel.score.collectAsState()
    val total by viewModel.totalQuestions.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.study_mode_quiz)) },
                navigationIcon = {
                    IconButton(onClick = onFinish) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_cancel))
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isFinished) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    val percentage = if (total > 0) (score.toFloat() / total.toFloat()) * 100 else 0f
                    val isPassed = percentage >= 50

                    val color = if (isPassed) Color(0xFF4CAF50) else Color(0xFFE57373)
                    val iconText = if (isPassed) "🏆" else "📚"

                    val message = if (isPassed) stringResource(R.string.quiz_passed) else stringResource(R.string.quiz_failed)

                    Text(text = iconText, fontSize = 64.sp)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = message,
                        style = MaterialTheme.typography.headlineMedium,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.quiz_score_format, score, total),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = stringResource(R.string.quiz_percentage_format, percentage.toInt()),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(onClick = onFinish) {
                        Text(stringResource(R.string.btn_back_to_deck))
                    }
                }
            } else {
                currentCard?.let { card ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.quiz_points, score),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.align(Alignment.End)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = card.question,
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(48.dp))

                        options.forEach { option ->
                            Button(
                                onClick = { viewModel.submitAnswer(option) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(option, fontSize = 18.sp, modifier = Modifier.padding(8.dp))
                            }
                        }
                    }
                } ?: Text(stringResource(R.string.loading))
            }
        }
    }
}