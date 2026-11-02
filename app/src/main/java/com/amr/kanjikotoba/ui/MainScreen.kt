package com.amr.kanjikotoba.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amr.kanjikotoba.KanjiKotobaApp
import com.amr.kanjikotoba.data.KanjiEntry
import com.amr.kanjikotoba.data.Word
import kotlinx.coroutines.launch

enum class Screen(val label: String) {
    KOTOBA("Kotoba"),
    KANJI("Kanji"),
    ANKI("Anki")
}

private sealed interface Route {
    data object Home : Route
    data class WordDetail(val word: Word) : Route
    data class KanjiDetail(val entry: KanjiEntry) : Route
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val speaker = remember { (context.applicationContext as KanjiKotobaApp).speaker }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var current by remember { mutableStateOf(Screen.KOTOBA) }
    var route by remember { mutableStateOf<Route>(Route.Home) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Kotoba & Kanji",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                Screen.entries.forEach { screen ->
                    NavigationDrawerItem(
                        label = { Text(screen.label, fontSize = 15.sp) },
                        selected = current == screen,
                        onClick = {
                            current = screen
                            route = Route.Home
                            scope.launch { drawerState.close() }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (val r = route) {
                                is Route.WordDetail -> r.word.surface
                                is Route.KanjiDetail -> r.entry.kanji
                                Route.Home -> current.label
                            },
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (route is Route.Home) {
                                scope.launch { drawerState.open() }
                            } else {
                                route = Route.Home
                            }
                        }) {
                            Icon(
                                imageVector = if (route is Route.Home) {
                                    Icons.Default.Menu
                                } else {
                                    Icons.AutoMirrored.Default.ArrowBack
                                },
                                contentDescription = if (route is Route.Home) "Menu" else "Kembali"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                when (val r = route) {
                    Route.Home -> when (current) {
                        Screen.KANJI -> KanjiListScreen(
                            onSpeak = { speaker.speak(it) },
                            onOpen = { route = Route.KanjiDetail(it) }
                        )
                        Screen.KOTOBA -> KotobaListScreen(
                            onSpeak = { speaker.speak(it) },
                            onOpen = { route = Route.WordDetail(it) }
                        )
                        Screen.ANKI -> AnkiScreen(onSpeak = { speaker.speak(it) })
                    }

                    is Route.WordDetail -> DetailScreen(
                        title = r.word.surface,
                        subtitle = r.word.reading,
                        badge = null,
                        sentences = r.word.sentences.ifEmpty {
                            listOf(
                                com.amr.kanjikotoba.data.Sentence(
                                    jp = r.word.sentence,
                                    reading = r.word.sentenceReading,
                                    id = ""
                                )
                            )
                        },
                        extra = { WordDetailContent(r.word) },
                        onSpeak = { speaker.speak(it) },
                        onBack = { route = Route.Home }
                    )

                    is Route.KanjiDetail -> DetailScreen(
                        title = r.entry.kanji,
                        subtitle = r.entry.meaning,
                        badge = r.entry.level,
                        sentences = r.entry.sentences.ifEmpty {
                            listOf(
                                com.amr.kanjikotoba.data.Sentence(
                                    jp = r.entry.sentence,
                                    reading = r.entry.sentenceReading,
                                    id = ""
                                )
                            )
                        },
                        extra = { KanjiDetailContent(r.entry) },
                        onSpeak = { speaker.speak(it) },
                        onBack = { route = Route.Home }
                    )
                }
            }
        }
    }
}

/** Soft rounded TTS button: muted fill, no harsh white. */
@Composable
fun SpeakButton(text: String, onSpeak: (String) -> Unit) {
    IconButton(
        onClick = { onSpeak(text) },
        modifier = Modifier
            .size(40.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Dengarkan",
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "$count kata",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
