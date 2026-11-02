package com.amr.kanjikotoba.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.action.clickable
import androidx.glance.unit.ColorProvider
import com.amr.kanjikotoba.R
import com.amr.kanjikotoba.data.KanjiRepository

/** Widget content: alternates between a kanji and a kotoba for the current 4-hour window. */
class KotobaGlanceWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entry = currentEntry(context)
        provideContent {
            GlanceTheme {
                WidgetContent(entry)
            }
        }
    }

    private fun currentEntry(context: Context): WidgetEntry {
        val idx = KanjiRepository.rotationIndex()
        val useKanji = idx % 2 == 0
        val kanjiList = KanjiRepository.kanji(context).kanji
        val wordList = KanjiRepository.allWords(context)
        return if (useKanji && kanjiList.isNotEmpty()) {
            val k = kanjiList[(idx % kanjiList.size + kanjiList.size) % kanjiList.size]
            WidgetEntry(
                surface = k.kanji,
                reading = k.on.firstOrNull() ?: k.kun.firstOrNull() ?: "",
                meaning = k.meaning,
                tag = k.level,
                isKanji = true,
            )
        } else if (wordList.isNotEmpty()) {
            val w = wordList[(idx % wordList.size + wordList.size) % wordList.size]
            WidgetEntry(
                surface = w.surface,
                reading = w.reading,
                meaning = w.meaning,
                tag = "kotoba",
                isKanji = false,
            )
        } else {
            WidgetEntry("", "", "", "", true)
        }
    }
}

data class WidgetEntry(
    val surface: String,
    val reading: String,
    val meaning: String,
    val tag: String,
    val isKanji: Boolean,
)

private val BG = ColorProvider(Color(0xFF171310))
private val SURFACE_HIGH = ColorProvider(Color(0xFF2C2620))
private val CREAM = ColorProvider(Color(0xFFF2EBE2))
private val ACCENT = ColorProvider(Color(0xFFE07856))
private val ACCENT_SOFT = ColorProvider(Color(0xFFF2C094))
private val MUTED = ColorProvider(Color(0xFFA89F94))

@Composable
private fun WidgetContent(entry: WidgetEntry) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(BG)
            .cornerRadius(24.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (entry.surface.isEmpty()) {
            Text(
                text = "Buka aplikasi untuk memuat data",
                style = TextStyle(fontSize = 13.sp, color = MUTED)
            )
            return@Box
        }
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = GlanceModifier.width(96.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = entry.surface,
                    style = TextStyle(fontSize = 58.sp, fontWeight = FontWeight.Bold, color = CREAM),
                    maxLines = 1
                )
                Spacer(GlanceModifier.height(2.dp))
                Text(
                    text = entry.tag,
                    style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, color = ACCENT),
                    maxLines = 1
                )
            }

            Spacer(GlanceModifier.width(12.dp))

            Column(
                modifier = GlanceModifier.defaultWeight(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = entry.reading,
                    style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Medium, color = ACCENT_SOFT),
                    maxLines = 1
                )
                Spacer(GlanceModifier.height(4.dp))
                Text(
                    text = entry.meaning,
                    style = TextStyle(fontSize = 14.sp, color = MUTED),
                    maxLines = 2
                )
            }

            Spacer(GlanceModifier.width(8.dp))

            Box(
                modifier = GlanceModifier
                    .size(40.dp)
                    .cornerRadius(20.dp)
                    .background(SURFACE_HIGH)
                    .clickable(actionRunCallback<RefreshWidgetAction>()),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_refresh),
                    contentDescription = "Tampilkan yang lain",
                    modifier = GlanceModifier.size(20.dp)
                )
            }
        }
    }
}
