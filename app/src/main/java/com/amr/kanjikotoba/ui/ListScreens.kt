package com.amr.kanjikotoba.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.amr.kanjikotoba.data.KanjiEntry
import com.amr.kanjikotoba.data.KanjiRepository
import com.amr.kanjikotoba.data.Word

@Composable
fun KanjiListScreen(onSpeak: (String) -> Unit, onOpen: (KanjiEntry) -> Unit) {
    val context = LocalContext.current
    val entries = remember { KanjiRepository.kanji(context).kanji }
    var levelFilter by remember { mutableStateOf<String?>(null) }
    val visible = remember(levelFilter) {
        entries.filter { levelFilter == null || it.level == levelFilter }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LevelChip("Semua", levelFilter == null) { levelFilter = null }
            LevelChip("N5", levelFilter == "N5") { levelFilter = "N5" }
            LevelChip("N4", levelFilter == "N4") { levelFilter = "N4" }
        }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(visible, key = { it.kanji }) { entry ->
                KanjiCard(entry = entry, onSpeak = onSpeak, onOpen = onOpen)
            }
        }
    }
}

@Composable
fun KotobaListScreen(onSpeak: (String) -> Unit, onOpen: (Word) -> Unit) {
    val context = LocalContext.current
    val themes = remember { KanjiRepository.kotoba(context).themes }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        themes.forEach { theme ->
            item(key = "header_${theme.title}") {
                SectionHeader(theme.title, theme.words.size)
            }
            items(theme.words, key = { it.surface }) { word ->
                WordCard(word = word, onSpeak = onSpeak, onOpen = onOpen)
            }
        }
    }
}

@Composable
fun LevelChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        label = { Text(label, fontSize = 13.sp) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        border = null
    )
}

@Composable
fun WordCard(word: Word, onSpeak: (String) -> Unit, onOpen: (Word) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable { onOpen(word) },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = word.surface,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = word.reading,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = word.meaning,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            SpeakButton(word.surface, onSpeak)
        }
    }
}

@Composable
fun KanjiCard(entry: KanjiEntry, onSpeak: (String) -> Unit, onOpen: (KanjiEntry) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable { onOpen(entry) },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = entry.kanji,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.width(60.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.meaning,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                val readings = (entry.on.map { "On: $it" } + entry.kun.map { "Kun: $it" })
                    .joinToString("  ")
                Text(
                    text = readings,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = entry.level,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.secondary
            )
            SpeakButton(entry.kanji, onSpeak)
        }
    }
}
