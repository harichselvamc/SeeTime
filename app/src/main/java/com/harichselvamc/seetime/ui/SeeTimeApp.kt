@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.harichselvamc.seetime.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SettingsApplications
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class NavTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val tabs = listOf(
    NavTab("Home",      Icons.Filled.Home,          Icons.Outlined.Home),
    NavTab("Insights",  Icons.Outlined.Analytics,   Icons.Outlined.Analytics),
    NavTab("Reminders", Icons.Filled.Notifications, Icons.Outlined.NotificationsNone),
    NavTab("Settings",  Icons.Filled.Settings,      Icons.Outlined.SettingsApplications)
)

@Composable
fun SeeTimeApp(
    viewModel: TimeViewModel,
    startWithAddDialog: Boolean = false
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val timeOffset by viewModel.timeOffsetMinutes.collectAsState()
    val use24Hour  by viewModel.use24HourFormat.collectAsState()
    val showSecs   by viewModel.showSeconds.collectAsState()
    var showTimeTravelSheet by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Planning banner — shown across all tabs when time offset active
            AnimatedVisibility(
                visible = timeOffset != 0,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit  = slideOutVertically(targetOffsetY  = { -it }) + fadeOut()
            ) {
                PlanningBanner(
                    offsetMinutes = timeOffset,
                    onOpenSheet = { showTimeTravelSheet = true },
                    onGoLive    = { viewModel.resetToLive() }
                )
            }

            // Tab content
            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        fadeIn(tween(220)) togetherWith fadeOut(tween(180))
                    },
                    label = "tab_transition"
                ) { tab ->
                    when (tab) {
                        0 -> HomeScreen(
                            viewModel = viewModel,
                            startWithAddDialog = startWithAddDialog,
                            onOpenTimeTravelSheet = { showTimeTravelSheet = true }
                        )
                        1 -> InsightsScreen(viewModel = viewModel)
                        2 -> RemindersScreen()
                        3 -> SettingsScreen(
                            use24HourFormat     = use24Hour,
                            onToggle24HourFormat = viewModel::setUse24HourFormat,
                            showSeconds          = showSecs,
                            onToggleShowSeconds  = viewModel::setShowSeconds
                        )
                    }
                }
            }

            // Bottom navigation bar
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                tabs.forEachIndexed { index, tab ->
                    val selected = selectedTab == index
                    NavigationBarItem(
                        selected  = selected,
                        onClick   = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = MaterialTheme.colorScheme.primary,
                            selectedTextColor   = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor      = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }

        // Time Travel Sheet — overlays everything
        if (showTimeTravelSheet) {
            TimeTravelSheet(
                currentOffsetMinutes = timeOffset,
                onApply   = { viewModel.setTimeOffset(it) },
                onGoLive  = { viewModel.resetToLive() },
                onDismiss = { showTimeTravelSheet = false }
            )
        }
    }
}

// ── Planning Banner ───────────────────────────────────────────────────

@Composable
private fun PlanningBanner(
    offsetMinutes: Int,
    onOpenSheet: () -> Unit,
    onGoLive: () -> Unit
) {
    val absMin = Math.abs(offsetMinutes)
    val h = absMin / 60
    val m = absMin % 60
    val direction = if (offsetMinutes > 0) "ahead" else "behind"
    val label = when {
        h > 0 && m > 0 -> "${h}h ${m}m $direction"
        h > 0          -> "${h}h $direction"
        else           -> "${m}m $direction"
    }

    Surface(
        modifier  = Modifier.fillMaxWidth(),
        color     = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
        onClick   = onOpenSheet
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.error)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Time Travel — $label  ·  Tap to adjust",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
            Surface(
                onClick = onGoLive,
                shape   = RoundedCornerShape(8.dp),
                color   = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
            ) {
                Text(
                    "Go Live",
                    style    = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color    = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}
