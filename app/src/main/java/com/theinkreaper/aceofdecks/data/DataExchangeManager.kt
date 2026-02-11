package com.theinkreaper.aceofdecks.data

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class DataExchangeManager(
    private val context: Context,
    private val repository: AppRepository
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun exportDeck(deckId: Long, uri: Uri) = withContext(Dispatchers.IO) {
        val deck = repository.getDeckById(deckId) ?: return@withContext
        val cards = repository.getCardsForDeckSync(deckId)

        val exportData = DeckExport(deck, cards)

        val jsonString = gson.toJson(exportData)

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(jsonString.toByteArray())
        }
    }

    suspend fun importDeck(uri: Uri) = withContext(Dispatchers.IO) {
        val stringBuilder = StringBuilder()

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    line = reader.readLine()
                }
            }
        }

        val jsonString = stringBuilder.toString()

        val importData = gson.fromJson(jsonString, DeckExport::class.java)

        val newDeckId = repository.createDeck(importData.deck.name)

        importData.cards.forEach { card ->
            val newCard = card.copy(
                id = 0,
                deckId = newDeckId
            )
            repository.saveCard(newCard)
        }

        val configDeck = importData.deck.copy(
            id = newDeckId,
            createdAt = System.currentTimeMillis()
        )
        repository.updateDeck(configDeck)
    }
}