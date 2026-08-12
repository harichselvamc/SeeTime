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
    primary = Cobalt60,
    onPrimary = White,
    primaryContainer = Cobalt80,
    onPrimaryContainer = Cobalt20,
    secondary = Purple60,
    onSecondary = White,
    secondaryContainer = Purple80,
    onSecondaryContainer = Purple40,
    tertiary = Cyan60,
    onTertiary = White,
    tertiaryContainer = LightSurfaceVar,
    onTertiaryContainer = SlateText,
    background = LightBg,
    onBackground = SlateText,
    surface = LightSurface,
    onSurface = SlateText,
    surfaceVariant = LightSurfaceVar,
    onSurfaceVariant = SlateText,
    outline = LightOutline,
    error = ErrorCoral,
    onError = White
)

private val DarkColors = darkColorScheme(
    primary = Cobalt60,
    onPrimary = White,
    primaryContainer = Cobalt20,
    onPrimaryContainer = Cobalt80,
    secondary = Purple60,
    onSecondary = White,
    secondaryContainer = DarkSurfaceVar,
    onSecondaryContainer = Purple80,
    tertiary = Cyan60,
    onTertiary = Black,
    tertiaryContainer = DarkSurfaceVar,
    onTertiaryContainer = Cyan60,
    background = DarkBg,
    onBackground = White,
    surface = DarkSurface,
    onSurface = White,
    surfaceVariant = DarkSurfaceVar,
    onSurfaceVariant = SlateTextLight,
    outline = DarkOutline,
    error = ErrorCoral,
    onError = White
)

@Composable
fun SeeTimeTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
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
