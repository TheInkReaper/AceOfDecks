package com.theinkreaper.aceofdecks.data

import com.google.gson.annotations.SerializedName

data class DeckExport(
    @SerializedName("deck_info")
    val deck: DeckEntity,

    @SerializedName("cards")
    val cards: List<CardEntity>
)