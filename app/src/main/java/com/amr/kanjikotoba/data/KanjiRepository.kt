package com.amr.kanjikotoba.data

import android.content.Context
import kotlinx.serialization.json.Json

/** Loads the bundled JFT datasets and owns the SRS scheduling state. */
object KanjiRepository {

    private val json = Json { ignoreUnknownKeys = true }

    @Volatile
    private var kotobaCache: KotobaDataset? = null

    @Volatile
    private var kanjiCache: KanjiDataset? = null

    fun kotoba(context: Context): KotobaDataset =
        kotobaCache ?: synchronized(this) {
            kotobaCache ?: json.decodeFromString<KotobaDataset>(
                context.assets.open("kotoba.json").bufferedReader().readText()
            ).also { kotobaCache = it }
        }

    fun kanji(context: Context): KanjiDataset =
        kanjiCache ?: synchronized(this) {
            kanjiCache ?: json.decodeFromString<KanjiDataset>(
                context.assets.open("kanji.json").bufferedReader().readText()
            ).also { kanjiCache = it }
        }

    fun allWords(context: Context): List<Word> =
        kotoba(context).themes.flatMap { theme -> theme.words }

    fun findKanji(context: Context, kanji: String): KanjiEntry? =
        kanji(context).kanji.firstOrNull { it.kanji == kanji }

    fun findWord(context: Context, surface: String): Word? =
        allWords(context).firstOrNull { it.surface == surface }
}
