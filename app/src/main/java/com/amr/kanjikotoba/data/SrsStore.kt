package com.amr.kanjikotoba.data

import android.content.Context
import org.json.JSONObject

/** Anki-style SM-2 scheduling state persisted in SharedPreferences. */
data class SrsCard(
    val id: String,
    val ease: Float = 2.5f,
    val intervalDays: Int = 0,
    val dueAt: Long = 0L,
    val reviews: Int = 0
)

enum class SrsRating(val label: String, val quality: Int) {
    AGAIN("Again", 0),
    HARD("Hard", 3),
    GOOD("Good", 4),
    EASY("Easy", 5)
}

class SrsStore(context: Context) {

    private val prefs = context.getSharedPreferences("srs_state", Context.MODE_PRIVATE)

    fun card(id: String): SrsCard {
        val raw = prefs.getString(id, null) ?: return SrsCard(id = id, dueAt = 0L)
        val obj = JSONObject(raw)
        return SrsCard(
            id = id,
            ease = obj.optDouble("ease", 2.5).toFloat(),
            intervalDays = obj.optInt("intervalDays", 0),
            dueAt = obj.optLong("dueAt", 0L),
            reviews = obj.optInt("reviews", 0)
        )
    }

    fun review(id: String, rating: SrsRating, now: Long = System.currentTimeMillis()): SrsCard {
        val current = card(id)
        val updated = schedule(current, rating, now)
        prefs.edit().putString(
            id,
            JSONObject().apply {
                put("ease", updated.ease.toDouble())
                put("intervalDays", updated.intervalDays)
                put("dueAt", updated.dueAt)
                put("reviews", updated.reviews)
            }.toString()
        ).apply()
        return updated
    }

    /** SM-2 update: quality below 3 resets the interval, otherwise it grows by ease. */
    private fun schedule(card: SrsCard, rating: SrsRating, now: Long): SrsCard {
        val q = rating.quality
        val ease = when {
            q < 3 -> (card.ease - 0.20f).coerceAtLeast(1.3f)
            q == 3 -> (card.ease - 0.15f).coerceAtLeast(1.3f)
            q == 5 -> card.ease + 0.15f
            else -> card.ease
        }
        val intervalDays = when {
            q < 3 -> 0
            card.reviews == 0 -> 1
            card.reviews == 1 -> 6
            else -> maxOf(1, Math.round(card.intervalDays * ease))
        }
        val day = 24L * 60 * 60 * 1000
        return SrsCard(
            id = card.id,
            ease = ease,
            intervalDays = intervalDays,
            dueAt = now + intervalDays * day,
            reviews = card.reviews + 1
        )
    }

    fun dueCount(ids: List<String>, now: Long = System.currentTimeMillis()): Int =
        ids.count { card(it).dueAt <= now }
}
