package com.theinkreaper.aceofdecks.ui.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.theinkreaper.aceofdecks.R
import com.theinkreaper.aceofdecks.data.CardType
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardEditorScreen(
    deckId: Long,
    cardIdToEdit: Long? = null,
    onBack: () -> Unit,
    viewModel: CardEditorViewModel = koinViewModel()
) {
    LaunchedEffect(cardIdToEdit) {
        if (cardIdToEdit != null) {
            viewModel.loadCardToEdit(cardIdToEdit)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearEditor()
        }
    }

    val type by viewModel.type.collectAsState()
    val question by viewModel.question.collectAsState()
    val answer by viewModel.answer.collectAsState()
    val explanation by viewModel.explanation.collectAsState()
    val distractors by viewModel.distractors.collectAsState()

    var newDistractor by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (cardIdToEdit != null) R.string.editor_title_edit else R.string.editor_title_new)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_cancel))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveCard(deckId, onSaved = onBack) }) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.action_save))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.label_card_type), style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.width(16.dp))
                FilterChip(
                    selected = type == CardType.FLASHCARD,
                    onClick = { viewModel.setType(CardType.FLASHCARD) },
                    label = { Text(stringResource(R.string.chip_flashcard)) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = type == CardType.TEST,
                    onClick = { viewModel.setType(CardType.TEST) },
                    label = { Text(stringResource(R.string.chip_test)) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = question,
                onValueChange = { viewModel.setQuestion(it) },
                label = { Text(stringResource(R.string.label_front)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = answer,
                onValueChange = { viewModel.setAnswer(it) },
                label = { Text(stringResource(R.string.label_back)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = explanation,
                onValueChange = { viewModel.setExplanation(it) },
                label = { Text(stringResource(R.string.label_explanation)) },
                placeholder = { Text(stringResource(R.string.hint_explanation)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            if (type == CardType.TEST) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(stringResource(R.string.label_distractors), style = MaterialTheme.typography.titleMedium)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newDistractor,
                        onValueChange = { newDistractor = it },
                        label = { Text(stringResource(R.string.hint_distractor)) },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        viewModel.addDistractor(newDistractor)
                        newDistractor = ""
                    }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.action_add))
                    }
                }

                distractors.forEachIndexed { index, item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("• $item", modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.removeDistractor(index) }) {
                            Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.action_delete), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}