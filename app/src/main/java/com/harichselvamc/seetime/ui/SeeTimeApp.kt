@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.harichselvamc.seetime.ui

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AirplaneTicket
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Anchor
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AirplaneTicket
import androidx.compose.material.icons.outlined.AirplanemodeActive
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Anchor
import androidx.compose.material.icons.outlined.AvTimer
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Emergency
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.FlightTakeoff
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.MoreTime
import androidx.compose.material.icons.outlined.Mosque
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.SatelliteAlt
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SettingsApplications
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.WbTwilight
import com.harichselvamc.seetime.ui.ActivityReviewScreen
import com.harichselvamc.seetime.ui.ClockChimeScreen
import com.harichselvamc.seetime.ui.DateTimeCalculatorScreen
import com.harichselvamc.seetime.ui.IssPassScreen
import com.harichselvamc.seetime.ui.OfflineFinanceScreen
import com.harichselvamc.seetime.ui.SolarOrreryScreen
import com.harichselvamc.seetime.ui.SoundscapeMixerScreen
import com.harichselvamc.seetime.ui.TimeAnalyticsScreen
import com.harichselvamc.seetime.ui.TripTimelineScreen
import com.harichselvamc.seetime.ui.TwilightHorizonScreen
import com.harichselvamc.seetime.ui.WorldTimeWallScreen
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


private data class NavTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val tabs = listOf(
    NavTab("Reminders", Icons.Filled.Home,              Icons.Outlined.Home),
    NavTab("Overlap",   Icons.Outlined.GridOn,          Icons.Outlined.GridOn),
    NavTab("Settings",  Icons.Filled.Settings,          Icons.Outlined.SettingsApplications)
)

@Composable
fun SeeTimeApp(
    viewModel: TimeViewModel,
    startWithAddDialog: Boolean = false,
    updateDownloaded: Boolean = false,
    onRestartForUpdate: () -> Unit = {},
    showWhatsNew: Boolean = false,
    onWhatsNewDismissed: () -> Unit = {}
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var activeHubScreen by rememberSaveable { mutableStateOf<String?>(null) }
    val timeOffset by viewModel.timeOffsetMinutes.collectAsState()
    val use24Hour  by viewModel.use24HourFormat.collectAsState()
    val showSecs   by viewModel.showSeconds.collectAsState()
    var showTimeTravelSheet by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(updateDownloaded) {
        if (updateDownloaded) {
            val result = snackbarHostState.showSnackbar(
                message = "An update has just been downloaded.",
                actionLabel = "RESTART",
                duration = SnackbarDuration.Indefinite
            )
            if (result == SnackbarResult.ActionPerformed) {
                onRestartForUpdate()
            }
        }
    }


    if (showWhatsNew) {
        AlertDialog(
            onDismissRequest = onWhatsNewDismissed,
            shape = MaterialTheme.shapes.large,
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("What's New") },
            text = { Text("Enjoy the latest features and bug fixes!") },
            confirmButton = {
                TextButton(onClick = onWhatsNewDismissed) {
                    Text("Awesome")
                }
            }
        )
    }

    val configuration = LocalConfiguration.current
    val isTabletOrExpanded = configuration.screenWidthDp >= 600
    val hapticFeedback = LocalHapticFeedback.current

    BackHandler(enabled = activeHubScreen != null) {
        activeHubScreen = null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isTabletOrExpanded) {
            // Adaptive Tablet / Foldable Landscape Layout with NavigationRail
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            val selected = selectedTab == index
                            NavigationRailItem(
                                selected  = selected,
                                onClick   = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    activeHubScreen = null
                                    selectedTab = index
                                },
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
                                colors = NavigationRailItemDefaults.colors(
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

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    AnimatedVisibility(
                        visible = timeOffset != 0,
                        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                        exit  = slideOutVertically(targetOffsetY  = { -it }) + fadeOut()
                    ) {
                        PlanningBanner(
                            offsetMinutes = timeOffset,
                            onOpenSheet = remember { { showTimeTravelSheet = true } },
                            onGoLive    = remember { { viewModel.resetToLive() } }
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        MainTabContent(
                            selectedTab = selectedTab,
                            activeHubScreen = activeHubScreen,
                            onFeatureSelected = { featureKey ->
                                activeHubScreen = featureKey
                                selectedTab = 2
                            },
                            viewModel = viewModel,
                            startWithAddDialog = startWithAddDialog,
                            onOpenTimeTravelSheet = { showTimeTravelSheet = true },
                            use24Hour = use24Hour,
                            showSecs = showSecs
                        )
                    }
                }
            }
        } else {
            // Compact Phone Portrait Layout with Bottom NavigationBar
            Column(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visible = timeOffset != 0,
                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                    exit  = slideOutVertically(targetOffsetY  = { -it }) + fadeOut()
                ) {
                    PlanningBanner(
                        offsetMinutes = timeOffset,
                        onOpenSheet = remember { { showTimeTravelSheet = true } },
                        onGoLive    = remember { { viewModel.resetToLive() } }
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    MainTabContent(
                        selectedTab = selectedTab,
                        activeHubScreen = activeHubScreen,
                        onFeatureSelected = { featureKey ->
                            activeHubScreen = featureKey
                            selectedTab = 2
                        },
                        viewModel = viewModel,
                        startWithAddDialog = startWithAddDialog,
                        onOpenTimeTravelSheet = { showTimeTravelSheet = true },
                        use24Hour = use24Hour,
                        showSecs = showSecs
                    )
                }

                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    tabs.forEachIndexed { index, tab ->
                        val selected = selectedTab == index
                        NavigationBarItem(
                            selected  = selected,
                            onClick   = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                activeHubScreen = null
                                selectedTab = index
                            },
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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun MainTabContent(
    selectedTab: Int,
    activeHubScreen: String?,
    onFeatureSelected: (String) -> Unit,
    viewModel: TimeViewModel,
    startWithAddDialog: Boolean,
    onOpenTimeTravelSheet: () -> Unit,
    use24Hour: Boolean,
    showSecs: Boolean
) {
    AnimatedContent(
        targetState = selectedTab,
        transitionSpec = {
            if (targetState > initialState) {
                (slideInHorizontally(
                    animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                    initialOffsetX = { fullWidth -> (fullWidth * 0.20f).toInt() }
                ) + fadeIn(animationSpec = tween(240))).togetherWith(
                    slideOutHorizontally(
                        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                        targetOffsetX = { fullWidth -> (-fullWidth * 0.20f).toInt() }
                    ) + fadeOut(animationSpec = tween(180))
                )
            } else {
                (slideInHorizontally(
                    animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                    initialOffsetX = { fullWidth -> (-fullWidth * 0.20f).toInt() }
                ) + fadeIn(animationSpec = tween(240))).togetherWith(
                    slideOutHorizontally(
                        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                        targetOffsetX = { fullWidth -> (fullWidth * 0.20f).toInt() }
                    ) + fadeOut(animationSpec = tween(180))
                )
            }
        },
        label = "tab_transition"
    ) { tab ->
        when (tab) {
            0 -> HomeScreen(
                viewModel = viewModel,
                startWithAddDialog = startWithAddDialog,
                onOpenTimeTravelSheet = onOpenTimeTravelSheet
            )
            1 -> MeetingOverlapScreen(viewModel = viewModel)
            2 -> SettingsScreen(
                use24HourFormat     = use24Hour,
                onToggle24HourFormat = viewModel::setUse24HourFormat,
                showSeconds          = showSecs,
                onToggleShowSeconds  = viewModel::setShowSeconds
            )
            else -> HomeScreen(
                viewModel = viewModel,
                startWithAddDialog = startWithAddDialog,
                onOpenTimeTravelSheet = onOpenTimeTravelSheet
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
