package com.amr.kanjikotoba.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amr.kanjikotoba.data.KanjiRepository
import com.amr.kanjikotoba.data.SrsRating
import com.amr.kanjikotoba.data.SrsStore

@Composable
fun KanjiListScreen(onSpeak: (String) -> Unit) {
    val context = LocalContext.current
    val entries = remember { KanjiRepository.kanji(context).kanji }
    var levelFilter by remember { mutableStateOf<String?>(null) }
    val visible = remember(levelFilter) {
        entries.filter { levelFilter == null || it.level == levelFilter }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LevelChip("Semua", levelFilter == null) { levelFilter = null }
            LevelChip("N5", levelFilter == "N5") { levelFilter = "N5" }
            LevelChip("N4", levelFilter == "N4") { levelFilter = "N4" }
        }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(visible) { entry -> KanjiRow(entry, onSpeak) }
        }
    }
}

@Composable
fun KotobaListScreen(onSpeak: (String) -> Unit) {
    val context = LocalContext.current
    val themes = remember { KanjiRepository.kotoba(context).themes }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        themes.forEach { theme ->
            item(key = "header_${theme.title}") {
                SectionHeader(theme.title, theme.words.size)
            }
            items(theme.words, key = { it.surface }) { word ->
                WordCard(word = word, onSpeak = onSpeak)
            }
        }
    }
}

@Composable
fun LevelChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Navy else SoftBlue
        )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) androidx.compose.ui.graphics.Color.White else Navy
        )
    }
}

@Composable
fun WordCard(word: com.amr.kanjikotoba.data.Word, onSpeak: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(word.surface, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        word.reading,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                SpeakButton(word.surface, onSpeak)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(word.meaning, fontSize = 14.sp, color = Accent)
            if (word.sentence.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(word.sentence, fontSize = 15.sp)
                        if (word.sentenceReading.isNotBlank()) {
                            Text(
                                word.sentenceReading,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                            )
                        }
                    }
                    SpeakButton(word.sentence, onSpeak)
                }
            }
        }
    }
}
