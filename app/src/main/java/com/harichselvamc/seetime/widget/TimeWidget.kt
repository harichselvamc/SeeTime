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
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider as DayNightColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.harichselvamc.seetime.MainActivity
import com.harichselvamc.seetime.data.SettingsRepository
import com.harichselvamc.seetime.data.TimeRepository
import com.harichselvamc.seetime.data.local.TimePair
import com.harichselvamc.seetime.util.TimeMath

private data class WidgetTimePair(
    val fromZone: String,
    val toZone: String,
    val fromDate: String,
    val fromTime: String,
    val toDate: String,
    val toTime: String,
    val diffText: String
)

class TimeWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = TimeRepository.getInstance(context)
        val settingsRepo = SettingsRepository.getInstance(context)
        val pairs = repo.getPairs()
        val use24Hour = settingsRepo.use24HourFormat.value
        val showExtraWidgetPairs = settingsRepo.showExtraWidgetPairs.value
        val widgetPairLimit = if (showExtraWidgetPairs) 6 else 1

        val widgetPairs = if (pairs.isEmpty()) {
            emptyList()
        } else {
            repo.refreshAllZones()
            pairs.take(widgetPairLimit).map { pair ->
                pair.toWidgetTimePair(
                    repo = repo,
                    use24Hour = use24Hour,
                    nowUtc = System.currentTimeMillis()
                )
            }
        }

        provideContent {
            WidgetContent(
                pairs = widgetPairs,
                totalPairCount = if (showExtraWidgetPairs) pairs.size else widgetPairs.size,
                showExtraWidgetPairs = showExtraWidgetPairs
            )
        }
    }
}

@Composable
private fun WidgetContent(
    pairs: List<WidgetTimePair>,
    totalPairCount: Int,
    showExtraWidgetPairs: Boolean
) {
    val context = LocalContext.current
    val size = LocalSize.current
    val isVeryCompact = size.height < 105.dp
    val visibleCount = if (showExtraWidgetPairs) {
        when {
            size.height < 105.dp -> 1
            size.height < 155.dp -> 2
            size.height < 215.dp -> 3
            else -> 4
        }.coerceAtMost(pairs.size)
    } else {
        pairs.size.coerceAtMost(1)
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(WidgetColors.background)
            .cornerRadius(24.dp)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        WidgetHeader(totalPairCount = totalPairCount)

        Spacer(modifier = GlanceModifier.height(if (isVeryCompact) 6.dp else 8.dp))

        if (pairs.isEmpty()) {
            EmptyWidgetContent()
            return@Column
        }

        if (visibleCount == 1 && !isVeryCompact) {
            TimePairRow(
                pair = pairs.first(),
                compact = false,
                expanded = true,
                modifier = GlanceModifier.defaultWeight()
            )
        } else {
            pairs.take(visibleCount).forEachIndexed { index, pair ->
                if (index > 0) {
                    Spacer(modifier = GlanceModifier.height(if (isVeryCompact) 6.dp else 8.dp))
                }
                TimePairRow(pair = pair, compact = isVeryCompact)
            }
        }

        val hiddenCount = totalPairCount - visibleCount
        if (showExtraWidgetPairs && hiddenCount > 0 && !isVeryCompact) {
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text(
                text = "+$hiddenCount more in See Time",
                style = TextStyle(
                    color = WidgetColors.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
private fun WidgetHeader(totalPairCount: Int) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "See Time",
            modifier = GlanceModifier.defaultWeight(),
            style = TextStyle(
                color = WidgetColors.onSurface,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        )
        if (totalPairCount > 1) {
            Text(
                text = "$totalPairCount pairs",
                style = TextStyle(
                    color = WidgetColors.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
private fun TimePairRow(
    pair: WidgetTimePair,
    compact: Boolean,
    expanded: Boolean = false,
    modifier: GlanceModifier = GlanceModifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(if (compact) WidgetColors.background else WidgetColors.secondarySurface)
            .cornerRadius(if (compact) 0.dp else 16.dp)
            .padding(
                horizontal = if (compact) 0.dp else 10.dp,
                vertical = if (compact) 0.dp else 8.dp
            ),
        verticalAlignment = if (expanded) Alignment.CenterVertically else Alignment.Top
    ) {
        Text(
            text = "${pair.fromZone} -> ${pair.toZone}",
            style = TextStyle(
                color = WidgetColors.onSurfaceVariant,
                fontSize = if (compact) 10.sp else 11.sp,
                fontWeight = FontWeight.Medium
            )
        )
        Spacer(modifier = GlanceModifier.height(3.dp))
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimeCell(
                zone = pair.fromZone,
                time = pair.fromTime,
                date = pair.fromDate,
                compact = compact,
                modifier = GlanceModifier.defaultWeight()
            )
            Spacer(modifier = GlanceModifier.width(10.dp))
            TimeCell(
                zone = pair.toZone,
                time = pair.toTime,
                date = pair.toDate,
                compact = compact,
                modifier = GlanceModifier.defaultWeight()
            )
        }
        if (!compact) {
            Spacer(modifier = GlanceModifier.height(3.dp))
            Text(
                text = pair.diffText,
                style = TextStyle(
                    color = WidgetColors.onSurfaceVariant,
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
private fun TimeCell(
    zone: String,
    time: String,
    date: String,
    compact: Boolean,
    modifier: GlanceModifier = GlanceModifier
) {
    Column(modifier = modifier) {
        if (!compact) {
            Text(
                text = zone,
                style = TextStyle(
                    color = WidgetColors.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
        Text(
            text = time,
            style = TextStyle(
                color = WidgetColors.onSurface,
                fontSize = if (compact) 14.sp else 15.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = date,
            style = TextStyle(
                color = WidgetColors.onSurfaceVariant,
                fontSize = 9.sp
            )
        )
    }
}

@Composable
private fun EmptyWidgetContent() {
    Column(
        modifier = GlanceModifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Add a time pair",
            style = TextStyle(
                color = WidgetColors.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = "Tap to open See Time",
            style = TextStyle(
                color = WidgetColors.onSurfaceVariant,
                fontSize = 11.sp
            )
        )
    }
}

private object WidgetColors {
    val background = DayNightColorProvider(
        day = Color(0xFFFFFBFE),
        night = Color(0xFF1D1B20)
    )
    val secondarySurface = DayNightColorProvider(
        day = Color(0xFFF1ECF4),
        night = Color(0xFF2B2930)
    )
    val onSurface = DayNightColorProvider(
        day = Color(0xFF1D1B20),
        night = Color(0xFFE6E1E5)
    )
    val onSurfaceVariant = DayNightColorProvider(
        day = Color(0xFF625B71),
        night = Color(0xFFCAC4D0)
    )
}

private suspend fun TimePair.toWidgetTimePair(
    repo: TimeRepository,
    use24Hour: Boolean,
    nowUtc: Long
): WidgetTimePair {
    val fromCache = repo.getZoneCache(fromZone)
    val toCache = repo.getZoneCache(toZone)
    val fromParts = splitWidgetDateTime(
        TimeMath.formatDateTime(nowUtc, fromCache, use24Hour, showSeconds = false)
    )
    val toParts = splitWidgetDateTime(
        TimeMath.formatDateTime(nowUtc, toCache, use24Hour, showSeconds = false)
    )

    return WidgetTimePair(
        fromZone = shortZoneName(fromZone),
        toZone = shortZoneName(toZone),
        fromDate = fromParts.first,
        fromTime = fromParts.second,
        toDate = toParts.first,
        toTime = toParts.second,
        diffText = TimeMath.buildDiffText(fromCache, toCache)
            .removePrefix("Time difference: ")
    )
}

private fun splitWidgetDateTime(value: String): Pair<String, String> {
    val parts = value.split(",", limit = 2)
    val date = parts.getOrNull(0)?.trim().orEmpty()
    val time = parts.getOrNull(1)?.trim().orEmpty()
    return date to time.ifEmpty { value }
}

private fun shortZoneName(zoneId: String): String =
    zoneId.substringAfterLast('/').replace('_', ' ')
