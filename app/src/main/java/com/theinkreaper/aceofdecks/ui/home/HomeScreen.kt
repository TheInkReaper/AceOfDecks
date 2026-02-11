package com.theinkreaper.aceofdecks.ui.home

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.theinkreaper.aceofdecks.R
import com.theinkreaper.aceofdecks.data.DeckEntity
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onDeckClick: (Long) -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val decks by viewModel.decks.collectAsState(initial = emptyList())
    val searchText by viewModel.searchText.collectAsState()
    val context = LocalContext.current

    var localDecks by remember(decks) { mutableStateOf(decks) }
    LaunchedEffect(decks) { localDecks = decks }

    var showCreateDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var deckToRename by remember { mutableStateOf<DeckEntity?>(null) }
    var deckToDelete by remember { mutableStateOf<DeckEntity?>(null) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { viewModel.importDeck(it) } }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let { viewModel.exportDeck(it); Toast.makeText(context, context.getString(R.string.export_success), Toast.LENGTH_SHORT).show() } }

    val reorderState = rememberReorderableLazyListState(
        onMove = { from, to ->
            localDecks = localDecks.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
        },
        onDragEnd = { _, _ -> viewModel.updateDeckOrder(localDecks) }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title)) },
                actions = {
                    TextButton(onClick = { importLauncher.launch(arrayOf("application/json")) }) {
                        Text(stringResource(R.string.action_import))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.action_add))
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { viewModel.onSearchTextChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text(stringResource(R.string.search_hint)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchText.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchTextChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Borrar")
                            }
                        }
                    },
                    singleLine = true
                )

                val isSearching = searchText.isNotEmpty()

                LazyColumn(
                    state = reorderState.listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .then(if (!isSearching) Modifier.reorderable(reorderState) else Modifier),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    if (localDecks.isEmpty() && searchText.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                Text(stringResource(R.string.home_empty))
                            }
                        }
                    } else {
                        items(localDecks, key = { it.id }) { deck ->
                            ReorderableItem(reorderableState = reorderState, key = deck.id) { isDragging ->
                                val elevation = animateDpAsState(if (isDragging) 8.dp else 0.dp, label = "elevation")
                                DeckItem(
                                    deck = deck,
                                    isDragging = isDragging,
                                    modifier = Modifier
                                        .shadow(elevation.value)
                                        .then(if (!isSearching) Modifier.detectReorderAfterLongPress(reorderState) else Modifier),
                                    onClick = { if (!isDragging) onDeckClick(deck.id) },
                                    onExport = {
                                        viewModel.deckPendingExport = deck
                                        exportLauncher.launch("${deck.name}.json")
                                    },
                                    onRename = { deckToRename = deck },
                                    onDelete = { deckToDelete = deck }
                                )
                            }
                        }
                    }
                }
            }

            TextButton(
                onClick = { showAboutDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = "©",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val currentLang = AppCompatDelegate.getApplicationLocales().get(0)?.language ?: "es"
                val displayLang = if (currentLang == "es") "ES" else "EN"

                FilledTonalButton(
                    onClick = {
                        val newLang = if (currentLang == "es") "en" else "es"
                        val appLocale = LocaleListCompat.forLanguageTags(newLang)
                        AppCompatDelegate.setApplicationLocales(appLocale)
                    }
                ) {
                    Text(text = displayLang, fontWeight = FontWeight.Bold)
                }

                val icon = if (isDarkTheme) "☀️" else "🌙"

                FilledTonalButton(
                    onClick = onToggleTheme
                ) {
                    Text(text = icon, fontSize = 18.sp)
                }
            }
        }

        if (showAboutDialog) {
            val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

            val urlTwitter = stringResource(R.string.url_twitter)
            val urlGithub = stringResource(R.string.url_github)
            val urlKofi = stringResource(R.string.url_kofi)

            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                icon = { Icon(Icons.Default.Info, contentDescription = null) },
                title = { Text(stringResource(R.string.about_title)) },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.about_version),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(R.string.about_slogan),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { uriHandler.openUri(urlTwitter) }) {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(R.drawable.ic_twitter),
                                    contentDescription = "Twitter",
                                    modifier = Modifier.size(28.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            IconButton(onClick = { uriHandler.openUri(urlGithub) }) {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(R.drawable.ic_github),
                                    contentDescription = "GitHub",
                                    modifier = Modifier.size(28.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            IconButton(onClick = { uriHandler.openUri(urlKofi) }) {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(R.drawable.ic_kofi),
                                    contentDescription = "Ko-Fi",
                                    modifier = Modifier.size(28.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAboutDialog = false }) {
                        Text(stringResource(R.string.action_close))
                    }
                }
            )
        }
        if (showCreateDialog) {
            InputDeckDialog(
                title = stringResource(R.string.dialog_create_deck_title),
                initialName = "",
                onDismiss = { showCreateDialog = false },
                onConfirm = { name -> viewModel.createDeck(name); showCreateDialog = false }
            )
        }
        deckToRename?.let { deck ->
            InputDeckDialog(
                title = stringResource(R.string.rename_deck_title),
                initialName = deck.name,
                onDismiss = { deckToRename = null },
                onConfirm = { newName -> viewModel.renameDeck(deck, newName); deckToRename = null }
            )
        }
        deckToDelete?.let { deck ->
            AlertDialog(
                onDismissRequest = { deckToDelete = null },
                title = { Text(stringResource(R.string.confirm_delete_title)) },
                text = { Text(stringResource(R.string.confirm_delete_message)) },
                confirmButton = { TextButton(onClick = { viewModel.deleteDeck(deck); deckToDelete = null }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text(stringResource(R.string.action_delete)) } },
                dismissButton = { TextButton(onClick = { deckToDelete = null }) { Text(stringResource(R.string.action_cancel)) } }
            )
        }
    }
}

@Composable
fun DeckItem(
    deck: DeckEntity,
    isDragging: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onExport: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val containerColor = if (isDragging) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(if (isDragging) 8.dp else 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.padding(end = 16.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = deck.name, style = MaterialTheme.typography.titleMedium)
            }
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.menu_options))
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.action_rename)) }, onClick = { expanded = false; onRename() })
                    DropdownMenuItem(text = { Text(stringResource(R.string.action_export)) }, onClick = { expanded = false; onExport() })
                    DropdownMenuItem(text = { Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error) }, onClick = { expanded = false; onDelete() })
                }
            }
        }
    }
}

@Composable
fun InputDeckDialog(title: String, initialName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text(stringResource(R.string.deck_name_hint)) }, singleLine = true) },
        confirmButton = { Button(onClick = { if (text.isNotBlank()) onConfirm(text) }) { Text(stringResource(R.string.action_save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}