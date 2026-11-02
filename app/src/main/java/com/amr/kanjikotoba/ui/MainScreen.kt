package com.amr.kanjikotoba.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amr.kanjikotoba.KanjiKotobaApp
import com.amr.kanjikotoba.data.KanjiEntry
import com.amr.kanjikotoba.data.Word
import kotlinx.coroutines.launch

enum class Screen(val label: String) {
    KANJI("Kanji"),
    KOTOBA("Kotoba"),
    ANKI("Anki")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val speaker = remember { (context.applicationContext as KanjiKotobaApp).speaker }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var current by remember { mutableStateOf(Screen.KOTOBA) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Kotoba & Kanji",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                Divider()
                Screen.entries.forEach { screen ->
                    NavigationDrawerItem(
                        label = { Text(screen.label) },
                        selected = current == screen,
                        onClick = {
                            current = screen
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(current.label) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Navy,
                        titleContentColor = androidx.compose.ui.graphics.Color.White,
                        navigationIconContentColor = androidx.compose.ui.graphics.Color.White
                    )
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                when (current) {
                    Screen.KANJI -> KanjiListScreen(onSpeak = { speaker.speak(it) })
                    Screen.KOTOBA -> KotobaListScreen(onSpeak = { speaker.speak(it) })
                    Screen.ANKI -> AnkiScreen(onSpeak = { speaker.speak(it) })
                }
            }
        }
    }
}

@Composable
fun SpeakButton(text: String, onSpeak: (String) -> Unit) {
    IconButton(onClick = { onSpeak(text) }) {
        Icon(
            Icons.Default.VolumeUp,
            contentDescription = "Dengarkan",
            tint = Navy,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun WordRow(word: Word, onSpeak: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = word.surface, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text(
                text = word.reading,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(text = word.meaning, fontSize = 14.sp, color = Accent)
        }
        SpeakButton(word.surface, onSpeak)
    }
}

@Composable
fun KanjiRow(entry: KanjiEntry, onSpeak: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = entry.kanji,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = Navy,
            modifier = Modifier.width(56.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = entry.meaning, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            val readings = (entry.on.map { "On: $it" } + entry.kun.map { "Kun: $it" }).joinToString("  ")
            Text(
                text = readings,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Text(
            text = entry.level,
            fontSize = 11.sp,
            color = Accent,
            modifier = Modifier
                .padding(end = 4.dp)
        )
        SpeakButton(entry.kanji, onSpeak)
    }
}

@Composable
fun SectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Navy)
        Text(
            text = "$count kata",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}
