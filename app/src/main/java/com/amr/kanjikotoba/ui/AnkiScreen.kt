package com.amr.kanjikotoba.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amr.kanjikotoba.data.KanjiEntry
import com.amr.kanjikotoba.data.KanjiRepository
import com.amr.kanjikotoba.data.SrsRating
import com.amr.kanjikotoba.data.SrsStore
import com.amr.kanjikotoba.data.Word

private sealed interface Flashcard {
    data class KanjiCard(val entry: KanjiEntry) : Flashcard
    data class WordCard(val word: Word) : Flashcard
}

@Composable
fun AnkiScreen(onSpeak: (String) -> Unit) {
    val context = LocalContext.current
    val store = remember { SrsStore(context) }
    val kanji = remember { KanjiRepository.kanji(context).kanji }
    val words = remember { KanjiRepository.kotoba(context).themes.flatMap { it.words } }

    val allCards = remember {
        (kanji.map { Flashcard.KanjiCard(it) } + words.map { Flashcard.WordCard(it) })
    }
    var index by remember { mutableIntStateOf(0) }
    var revealed by remember { mutableStateOf(false) }
    var sessionReviewed by remember { mutableIntStateOf(0) }
    var cardId by remember { mutableStateOf("") }

    val card = allCards.getOrNull(index)
    val title = when (card) {
        is Flashcard.KanjiCard -> card.entry.kanji
        is Flashcard.WordCard -> card.word.surface
        null -> ""
    }
    val reading = when (card) {
        is Flashcard.KanjiCard -> card.entry.kun.firstOrNull() ?: card.entry.on.firstOrNull() ?: ""
        is Flashcard.WordCard -> card.word.reading
        null -> ""
    }
    val meaning = when (card) {
        is Flashcard.KanjiCard -> card.entry.meaning
        is Flashcard.WordCard -> card.word.meaning
        null -> ""
    }
    val sentence = when (card) {
        is Flashcard.KanjiCard -> card.entry.sentence
        is Flashcard.WordCard -> card.word.sentence
        null -> ""
    }
    val sentenceReading = when (card) {
        is Flashcard.KanjiCard -> card.entry.sentenceReading
        is Flashcard.WordCard -> card.word.sentenceReading
        null -> ""
    }

    if (card != null && cardId != (index.toString() + title)) {
        cardId = "$index$title"
        revealed = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kartu ${index + 1} dari ${allCards.size}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "Sesi: $sessionReviewed",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { if (allCards.isEmpty()) 0f else (index + 1f) / allCards.size },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outlineVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                if (revealed) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(reading, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        meaning,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                    if (sentence.isNotBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            sentence,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                        if (sentenceReading.isNotBlank()) {
                            Text(
                                sentenceReading,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Tekan 'Tampilkan' untuk melihat arti",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            OutlinedButton(onClick = { onSpeak(title) }) { Text("Dengar kata") }
            if (revealed && sentence.isNotBlank()) {
                OutlinedButton(onClick = { onSpeak(sentence) }) { Text("Dengar kalimat") }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (!revealed) {
            Button(
                onClick = { revealed = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Tampilkan jawaban", fontSize = 16.sp)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RatingButton("Again", SrsRating.AGAIN, Color(0xFFC62828)) {
                    store.review(cardId, SrsRating.AGAIN)
                    sessionReviewed++
                    revealed = false
                    index = (index + 1) % allCards.size
                }
                RatingButton("Hard", SrsRating.HARD, Color(0xFFEF6C00)) {
                    store.review(cardId, SrsRating.HARD)
                    sessionReviewed++
                    revealed = false
                    index = (index + 1) % allCards.size
                }
                RatingButton("Good", SrsRating.GOOD, Color(0xFF2E7D32)) {
                    store.review(cardId, SrsRating.GOOD)
                    sessionReviewed++
                    revealed = false
                    index = (index + 1) % allCards.size
                }
                RatingButton("Easy", SrsRating.EASY, Color(0xFF1565C0)) {
                    store.review(cardId, SrsRating.EASY)
                    sessionReviewed++
                    revealed = false
                    index = (index + 1) % allCards.size
                }
            }
        }
    }
}

@Composable
private fun RowScope.RatingButton(
    label: String,
    rating: SrsRating,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
