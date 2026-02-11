package com.theinkreaper.aceofdecks.ui.deck

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.theinkreaper.aceofdecks.R
import com.theinkreaper.aceofdecks.data.CardEntity
import com.theinkreaper.aceofdecks.data.CardType
import com.theinkreaper.aceofdecks.data.DeckEntity
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckDetailScreen(
    deckId: Long,
    onBack: () -> Unit,
    onAddCard: (Long) -> Unit,
    onEditCard: (Long) -> Unit,
    onStudy: (Long) -> Unit,
    onQuiz: (Long) -> Unit,
    viewModel: DeckDetailViewModel = koinViewModel()
) {
    LaunchedEffect(deckId) { viewModel.loadDeck(deckId) }

    val deck by viewModel.currentDeck.collectAsState()
    val cards by viewModel.cards.collectAsState()

    var cardToDelete by remember { mutableStateOf<CardEntity?>(null) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(deck?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_cancel))
                    }
                },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.action_settings))
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAddCard(deckId) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.action_add)) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (cards.isNotEmpty()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (cards.any { it.type == CardType.FLASHCARD }) {
                        Button(
                            onClick = { onStudy(deckId) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.action_study))
                        }
                    }
                    if (cards.any { it.type == CardType.TEST }) {
                        OutlinedButton(
                            onClick = { onQuiz(deckId) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.study_mode_quiz))
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_cards_in_deck), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(cards) { card ->
                    CardItemRow(
                        card = card,
                        onEdit = { onEditCard(card.id) },
                        onDelete = { cardToDelete = card }
                    )
                }
            }
        }

        if (showSettingsDialog && deck != null) {
            DeckSettingsDialog(
                deck = deck!!,
                onDismiss = { showSettingsDialog = false },
                onSave = { studyLimit, quizLimit ->
                    viewModel.updateDeckSettings(studyLimit, quizLimit)
                    showSettingsDialog = false
                }
            )
        }

        cardToDelete?.let { card ->
            AlertDialog(
                onDismissRequest = { cardToDelete = null },
                title = { Text(stringResource(R.string.confirm_delete_title)) },
                text = { Text(stringResource(R.string.confirm_delete_message)) },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.deleteCard(card); cardToDelete = null },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text(stringResource(R.string.action_delete)) }
                },
                dismissButton = {
                    TextButton(onClick = { cardToDelete = null }) { Text(stringResource(R.string.action_cancel)) }
                }
            )
        }
    }
}

@Composable
fun DeckSettingsDialog(
    deck: DeckEntity,
    onDismiss: () -> Unit,
    onSave: (Int, Int) -> Unit
) {
    val options = listOf(0, 5, 10, 15, 20, 30, 50, 100)

    var studyLimit by remember { mutableIntStateOf(deck.studyLimitPerSession) }
    var quizLimit by remember { mutableIntStateOf(deck.quizLimitPerSession) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_title)) },
        text = {
            Column {
                LimitDropdown(
                    label = stringResource(R.string.settings_study_limit),
                    currentValue = studyLimit,
                    options = options,
                    onValueChange = { studyLimit = it }
                )

                Spacer(modifier = Modifier.height(24.dp))

                LimitDropdown(
                    label = stringResource(R.string.settings_quiz_limit),
                    currentValue = quizLimit,
                    options = options,
                    onValueChange = { quizLimit = it }
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.settings_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(studyLimit, quizLimit) }) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LimitDropdown(
    label: String,
    currentValue: Int,
    options: List<Int>,
    onValueChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val displayText = if (currentValue == 0) stringResource(R.string.settings_limit_none) else stringResource(R.string.settings_cards_format, currentValue)

    Column {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))

        Box {
            OutlinedTextField(
                value = displayText,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )
            Surface(
                modifier = Modifier.matchParentSize(),
                color = androidx.compose.ui.graphics.Color.Transparent,
                onClick = { expanded = true }
            ) {}

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            val text = if (option == 0) stringResource(R.string.settings_limit_none) else stringResource(R.string.settings_cards_format, option)
                            Text(text)
                        },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CardItemRow(card: CardEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = card.question, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(
                    text = if (card.type == CardType.TEST) stringResource(R.string.type_test) else stringResource(R.string.type_flashcard),
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary
                )
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
        }
    }
}