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

fun main() = application {
    val container = remember { AppContainer() }
    val state = rememberWindowState(size = DpSize(1440.dp, 860.dp))
    Window(
        onCloseRequest = {
            container.close()
            exitApplication()
        },
        title = stringResource(Res.string.app_name),
        state = state,
    ) {
        window.minimumSize = Dimension(1200, 700)
        container.dialogOwner = window
        AppWindow(container)
    }
}
