package com.harichselvamc.seetime

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.harichselvamc.seetime.ui.HomeScreen
import com.harichselvamc.seetime.ui.SettingsScreen
import com.harichselvamc.seetime.ui.TimeViewModel
import com.harichselvamc.seetime.ui.theme.SeeTimeTheme

/** Extra key set by the app shortcuts declared in res/xml/shortcuts.xml. */
const val EXTRA_SHORTCUT_ACTION = "shortcut_action"
const val SHORTCUT_ACTION_QUICK_ADD = "quick_add"
const val SHORTCUT_ACTION_REPORT_ISSUE = "report_issue"

private const val DEVELOPER_EMAIL = "harichselvamc@gmail.com"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val shortcutAction = intent?.getStringExtra(EXTRA_SHORTCUT_ACTION)
        val launchWithAddDialog = shortcutAction == SHORTCUT_ACTION_QUICK_ADD

        if (shortcutAction == SHORTCUT_ACTION_REPORT_ISSUE) {
            openReportIssueEmail()
        }

        setContent {
            SeeTimeTheme {
                val vm: TimeViewModel = viewModel()
                var showSettings by remember { mutableStateOf(false) }

                if (showSettings) {
                    val use24Hour by vm.use24HourFormat.collectAsState()
                    SettingsScreen(
                        use24HourFormat = use24Hour,
                        onToggle24HourFormat = vm::setUse24HourFormat,
                        onBack = { showSettings = false }
                    )
                } else {
                    HomeScreen(
                        viewModel = vm,
                        startWithAddDialog = launchWithAddDialog,
                        onOpenSettings = { showSettings = true }
                    )
                }
            }
        }
    }

    private fun openReportIssueEmail() {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$DEVELOPER_EMAIL")).apply {
            putExtra(Intent.EXTRA_SUBJECT, "SeeTime - Issue report")
        }
        try {
            startActivity(intent)
        } catch (_: Exception) {
            // No mail app available — user still lands on the home screen normally.
        }
    }
}
