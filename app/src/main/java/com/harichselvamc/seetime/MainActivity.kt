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
import androidx.compose.runtime.setValue
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
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
    private lateinit var appUpdateManager: AppUpdateManager
    private val updateRequestCode = 100
    private var updateDownloaded = mutableStateOf(false)

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            updateDownloaded.value = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateManager.registerListener(installStateUpdatedListener)
        checkForAppUpdate()

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
                
                val currentVersionCode = BuildConfig.VERSION_CODE
                val lastOpenedVersionCode = androidx.compose.runtime.remember { settingsRepo.getLastOpenedVersionCode() }
                var showWhatsNew by androidx.compose.runtime.remember { 
                    androidx.compose.runtime.mutableStateOf(currentVersionCode > lastOpenedVersionCode) 
                }

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
                        startWithAddDialog = launchWithAddDialog,
                        updateDownloaded   = updateDownloaded.value,
                        onRestartForUpdate = { appUpdateManager.completeUpdate() },
                        showWhatsNew       = showWhatsNew,
                        onWhatsNewDismissed = {
                            settingsRepo.setLastOpenedVersionCode(currentVersionCode)
                            showWhatsNew = false
                        }
                    )
                }
            }
        }
    }

    private fun checkForAppUpdate() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.FLEXIBLE,
                    this,
                    updateRequestCode
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                updateDownloaded.value = true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        appUpdateManager.unregisterListener(installStateUpdatedListener)
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
