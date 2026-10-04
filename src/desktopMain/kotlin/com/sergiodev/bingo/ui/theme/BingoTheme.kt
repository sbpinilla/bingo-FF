package com.sergiodev.bingo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.sergiodev.bingo.domain.repository.ThemeMode
import com.sergiodev.bingo.presentation.isDark

private val Purple80 = Color(0xFFD0BCFF)
private val PurpleGrey80 = Color(0xFFCCC2DC)
private val Pink80 = Color(0xFFEFB8C8)
private val Purple40 = Color(0xFF6650A4)
private val PurpleGrey40 = Color(0xFF625B71)
private val Pink40 = Color(0xFF7D5260)

/** Palettes ported from the Android app (no dynamic colour on desktop). Other roles use Material defaults. */
val BingoDarkColorScheme = darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)
val BingoLightColorScheme = lightColorScheme(primary = Purple40, secondary = PurpleGrey40, tertiary = Pink40)

/** App theme: [ThemeMode.SYSTEM] follows the OS and re-themes live when it changes. */
@Composable
fun BingoTheme(mode: ThemeMode, content: @Composable () -> Unit) {
    val dark = mode.isDark(isSystemInDarkTheme())
    MaterialTheme(
        colorScheme = if (dark) BingoDarkColorScheme else BingoLightColorScheme,
        content = content,
    )
}
