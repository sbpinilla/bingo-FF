package com.sergiodev.bingo

import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.sergiodev.bingo.di.AppContainer
import com.sergiodev.bingo.ui.AppWindow
import com.sergiodev.bingo.resources.*
import org.jetbrains.compose.resources.stringResource
import java.awt.Dimension

private const val DEFAULT_WIDTH_DP = 1440
private const val DEFAULT_HEIGHT_DP = 860
private const val MIN_WIDTH_PX = 1200
private const val MIN_HEIGHT_PX = 700

fun main() {
    // Put the window menu in the macOS system menu bar; ignored elsewhere.
    System.setProperty("apple.laf.useScreenMenuBar", "true")
    app()
}

private fun app() = application {
    val container = remember { AppContainer() }
    val state = rememberWindowState(size = DpSize(DEFAULT_WIDTH_DP.dp, DEFAULT_HEIGHT_DP.dp))
    Window(
        onCloseRequest = {
            container.close()
            exitApplication()
        },
        title = stringResource(Res.string.app_name),
        state = state,
    ) {
        window.minimumSize = Dimension(MIN_WIDTH_PX, MIN_HEIGHT_PX)
        container.dialogOwner = window
        AppWindow(container)
    }
}
