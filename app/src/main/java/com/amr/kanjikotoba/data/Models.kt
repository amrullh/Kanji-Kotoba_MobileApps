package com.amr.kanjikotoba.data

import kotlinx.serialization.Serializable

@Serializable
data class Theme(
    val title: String,
    val words: List<Word>
)

@Serializable
data class Word(
    val surface: String,
    val reading: String,
    val meaning: String,
    val sentence: String,
    @kotlinx.serialization.SerialName("sentence_reading")
    val sentenceReading: String = "",
    val sentences: List<Sentence> = emptyList()
)

@Serializable
data class Sentence(
    val jp: String,
    val reading: String,
    val id: String
)

@Serializable
data class KotobaDataset(
    val themes: List<Theme>
)

@Serializable
data class KanjiEntry(
    val kanji: String,
    val on: List<String> = emptyList(),
    val kun: List<String> = emptyList(),
    val meaning: String,
    val words: List<KanjiWord> = emptyList(),
    val sentence: String,
    @kotlinx.serialization.SerialName("sentence_reading")
    val sentenceReading: String = "",
    val sentences: List<Sentence> = emptyList(),
    val level: String
)

@Serializable
data class KanjiWord(
    val surface: String,
    val reading: String,
    val meaning: String
)

@Serializable
data class KanjiDataset(
    val kanji: List<KanjiEntry>
)
