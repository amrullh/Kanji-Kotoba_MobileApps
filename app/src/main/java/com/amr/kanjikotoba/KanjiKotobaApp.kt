package com.amr.kanjikotoba

import android.app.Application
import com.amr.kanjikotoba.tts.JapaneseSpeaker

class KanjiKotobaApp : Application() {

    lateinit var speaker: JapaneseSpeaker
        private set

    override fun onCreate() {
        super.onCreate()
        speaker = JapaneseSpeaker(this)
    }
}
