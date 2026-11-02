package com.amr.kanjikotoba.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll

/** Advances the widget to the next entry on demand. */
class RefreshWidgetAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        com.amr.kanjikotoba.data.KanjiRepository.advanceWindow(context)
        KotobaGlanceWidget().updateAll(context)
    }
}
