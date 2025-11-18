package com.harichselvamc.seetime.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Purple40,
    onPrimary = White,
    secondary = Teal40,
    onSecondary = White,
    background = LightBg,
    onBackground = Black,
    surface = LightSurface,
    onSurface = Black
)

private val DarkColors = darkColorScheme(
    primary = Purple80,
    onPrimary = Black,
    secondary = Teal80,
    onSecondary = Black,
    background = DarkBg,
    onBackground = White,
    surface = DarkSurface,
    onSurface = White
)

@Composable
fun SeeTimeTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),   // 👈 follows device
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val colorScheme =
        if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (useDarkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        } else {
            if (useDarkTheme) DarkColors else LightColors
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SeeTimeTypography,
        shapes = SeeTimeShapes,
        content = content
    )
}
