package com.harichselvamc.seetime.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.harichselvamc.seetime.MainActivity
import com.harichselvamc.seetime.data.SettingsRepository
import com.harichselvamc.seetime.data.TimeRepository
import com.harichselvamc.seetime.util.TimeMath

/**
 * Small home-screen widget showing the first saved time pair. Android
 * enforces a minimum auto-refresh interval (see time_widget_info.xml), so
 * this is a periodic snapshot rather than a live tick — opening the app
 * or editing pairs also pushes an immediate refresh (see
 * WidgetUpdater.requestUpdate()).
 */
class TimeWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = TimeRepository.getInstance(context)
        val settingsRepo = SettingsRepository.getInstance(context)

        val pairs = repo.getPairs()
        val firstPair = pairs.firstOrNull()
        val use24Hour = settingsRepo.use24HourFormat.value

        val fromLabel = firstPair?.fromZone
        val toLabel = firstPair?.toZone
        var fromTime: String? = null
        var toTime: String? = null
        var diffText: String? = null

        if (firstPair != null) {
            val nowUtc = System.currentTimeMillis()
            val fromCache = repo.getZoneCache(firstPair.fromZone)
            val toCache = repo.getZoneCache(firstPair.toZone)
            fromTime = TimeMath.formatDateTime(nowUtc, fromCache, use24Hour)
            toTime = TimeMath.formatDateTime(nowUtc, toCache, use24Hour)
            diffText = TimeMath.buildDiffText(fromCache, toCache)
        }

        provideContent {
            WidgetContent(
                fromLabel = fromLabel,
                toLabel = toLabel,
                fromTime = fromTime,
                toTime = toTime,
                diffText = diffText
            )
        }
    }
}

@Composable
private fun WidgetContent(
    fromLabel: String?,
    toLabel: String?,
    fromTime: String?,
    toTime: String?,
    diffText: String?
) {
    val white = ColorProvider(Color.White)
    val context = LocalContext.current

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFF6750A4))
            .padding(12.dp)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
    ) {
        Text(
            text = "See Time",
            style = TextStyle(color = white, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        )

        if (fromLabel == null || toLabel == null) {
            Text(
                text = "Add a time pair to get started",
                style = TextStyle(color = white, fontSize = 13.sp)
            )
        } else {
            Row(modifier = GlanceModifier.padding(top = 6.dp)) {
                Column(modifier = GlanceModifier.padding(end = 12.dp)) {
                    Text(
                        text = shortZoneName(fromLabel),
                        style = TextStyle(color = white, fontSize = 11.sp)
                    )
                    Text(
                        text = fromTime.orEmpty(),
                        style = TextStyle(color = white, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    )
                }
                Column {
                    Text(
                        text = shortZoneName(toLabel),
                        style = TextStyle(color = white, fontSize = 11.sp)
                    )
                    Text(
                        text = toTime.orEmpty(),
                        style = TextStyle(color = white, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }
            if (diffText != null) {
                Text(text = diffText, style = TextStyle(color = white, fontSize = 10.sp))
            }
        }
    }
}

/** "Asia/Kolkata" -> "Kolkata" — the city segment reads better at widget size. */
private fun shortZoneName(zoneId: String): String =
    zoneId.substringAfterLast('/').replace('_', ' ')
