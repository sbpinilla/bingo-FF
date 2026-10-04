package com.sergiodev.bingo.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.FrameWindowScope
import com.sergiodev.bingo.di.AppContainer
import com.sergiodev.bingo.presentation.CreateBoardHolder
import com.sergiodev.bingo.presentation.RightPaneDestination
import com.sergiodev.bingo.presentation.ShellLayout
import com.sergiodev.bingo.presentation.rightPaneDestination
import com.sergiodev.bingo.ui.theme.BingoTheme

/** Two-pane shell: boards on the left (2/3), game setup and play on the right (1/3). */
@Composable
fun FrameWindowScope.AppWindow(container: AppContainer) {
    var createHolder by remember { mutableStateOf<CreateBoardHolder?>(null) }
    var pasteDialogOpen by remember { mutableStateOf(false) }
    val importExport = container.importExportHolder
    val importExportState by importExport.state.collectAsState()

    val themeMode by container.themeHolder.mode.collectAsState()

    AppMenuBar(
        onExportToFile = importExport::onExportToFile,
        onCopyToClipboard = importExport::onCopyToClipboard,
        onImportFromFile = importExport::onImportFromFile,
        onImportPasted = {
            importExport.onPasteDialogReset()
            pasteDialogOpen = true
        },
        themeMode = themeMode,
        onThemeChange = container.themeHolder::set,
    )

    BingoTheme(themeMode) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    BoardsPane(
                        boardsState = container.boardsState,
                        paneState = container.boardsPaneState,
                        onAddBoard = { createHolder = container.newCreateBoardHolder() },
                        modifier = Modifier.weight(ShellLayout.LEFT_WEIGHT).fillMaxHeight(),
                    )
                    RightPane(container, Modifier.weight(ShellLayout.RIGHT_WEIGHT).fillMaxHeight())
                }
            }
            createHolder?.let { holder ->
                CreateBoardDialog(holder = holder, onClose = { createHolder = null })
            }
            if (pasteDialogOpen) {
                LaunchedEffect(importExportState.importSummary) {
                    if (importExportState.importSummary != null) pasteDialogOpen = false
                }
                ImportDialog(
                    state = importExportState,
                    onTextChange = importExport::onJsonTextChange,
                    onSubmit = importExport::onSubmitPasted,
                    onClose = {
                        importExport.onPasteDialogReset()
                        pasteDialogOpen = false
                    },
                )
            }
            ImportExportNotices(
                state = importExportState,
                pasteDialogOpen = pasteDialogOpen,
                onSummaryDismissed = importExport::onSummaryDismissed,
                onErrorDismissed = importExport::onPasteDialogReset,
                onExportDismissed = importExport::onExportOutcomeDismissed,
            )
        }
    }
}

/** Right pane: Setup or Play according to [rightPaneDestination]; blank until the stored game has been read. */
@Composable
private fun RightPane(container: AppContainer, modifier: Modifier) {
    val loaded by container.gameSession.loaded.collectAsState()
    // Derived from the same `active` flow as `loaded` is gated on, so a restored game never shows Setup first.
    val active by container.gameSession.active.collectAsState()
    val destination = rightPaneDestination(active)
    Box(modifier = modifier) {
        if (loaded) {
            when (destination) {
                RightPaneDestination.Setup -> SetupPane(container.gameSession)
                RightPaneDestination.Play -> PlayPane(container.gamePlay, container.playPaneState, container.gameSession)
            }
        }
    }
}
