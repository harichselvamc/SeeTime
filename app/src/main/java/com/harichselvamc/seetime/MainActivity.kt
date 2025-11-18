package com.harichselvamc.seetime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.harichselvamc.seetime.ui.HomeScreen
import com.harichselvamc.seetime.ui.TimeViewModel
import com.harichselvamc.seetime.ui.theme.SeeTimeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SeeTimeTheme {
                // Default factory can create AndroidViewModel using application
                val vm: TimeViewModel = viewModel()
                HomeScreen(viewModel = vm)
            }
        }
    }
}
