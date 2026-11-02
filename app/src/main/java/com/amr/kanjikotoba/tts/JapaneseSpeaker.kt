package com.amr.kanjikotoba.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/** Japanese TTS helper with graceful fallback when no voice data is installed. */
class JapaneseSpeaker(context: Context) {

    private var tts: TextToSpeech? = null
    private var ready = false
    private var japaneseAvailable = false
    var onStatusChanged: ((Boolean) -> Unit)? = null

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.JAPANESE)
                japaneseAvailable = result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED
                tts?.setSpeechRate(0.85f)
                ready = true
                onStatusChanged?.invoke(japaneseAvailable)
            } else {
                ready = false
                onStatusChanged?.invoke(false)
            }
        }
    }

    fun speak(text: String) {
        if (!ready || text.isBlank()) return
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utt_${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
    }

    fun isJapaneseAvailable(): Boolean = ready && japaneseAvailable

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        ready = false
    }
}
