package com.amr.kanjikotoba.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.text.style.TextAlign
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(visible.size, key = { visible[it].kanji }) { index ->
                val entry = visible[index]
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
            item(key = "grid_${theme.title}") {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(((theme.words.size + 2) / 3 * 108).dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    userScrollEnabled = false
                ) {
                    items(theme.words.size, key = { theme.words[it].surface }) { index ->
                        val word = theme.words[index]
                        WordCard(word = word, onSpeak = onSpeak, onOpen = onOpen)
                    }
                }
            }
            item(key = "space_${theme.title}") {
                Spacer(modifier = Modifier.height(10.dp))
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
            .aspectRatio(0.85f)
            .clickable { onOpen(word) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = word.surface,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 6.dp),
                maxLines = 2
            )
        }
    }
}

@Composable
fun KanjiCard(entry: KanjiEntry, onSpeak: (String) -> Unit, onOpen: (KanjiEntry) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clickable { onOpen(entry) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = entry.kanji,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}
