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
import com.harichselvamc.seetime.data.SettingsRepository
import com.harichselvamc.seetime.ui.OnboardingScreen
import com.harichselvamc.seetime.ui.SeeTimeApp
import com.harichselvamc.seetime.ui.TimeViewModel
import com.harichselvamc.seetime.ui.theme.SeeTimeTheme

/** Extra key set by the app shortcuts declared in res/xml/shortcuts.xml. */
const val EXTRA_SHORTCUT_ACTION       = "shortcut_action"
const val SHORTCUT_ACTION_QUICK_ADD   = "quick_add"
const val SHORTCUT_ACTION_REPORT_ISSUE = "report_issue"

private const val DEVELOPER_EMAIL = "harichselvamc@gmail.com"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val shortcutAction      = intent?.getStringExtra(EXTRA_SHORTCUT_ACTION)
        val launchWithAddDialog = shortcutAction == SHORTCUT_ACTION_QUICK_ADD

        if (shortcutAction == SHORTCUT_ACTION_REPORT_ISSUE) {
            openReportIssueEmail()
        }

        setContent {
            SeeTimeTheme {
                val vm: TimeViewModel = viewModel()
                val settingsRepo = androidx.compose.runtime.remember {
                    SettingsRepository.getInstance(applicationContext)
                }
                val onboardingCompleted by settingsRepo.onboardingCompleted.collectAsState(initial = false)

                if (!onboardingCompleted) {
                    OnboardingScreen(
                        onFinished = {
                            settingsRepo.setOnboardingCompleted(true)
                        }
                    )
                } else {
                    // Main app shell with bottom navigation
                    SeeTimeApp(
                        viewModel          = vm,
                        startWithAddDialog = launchWithAddDialog
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
            // No mail app available — user lands on home screen normally.
        }
    }
}
