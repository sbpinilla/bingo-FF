package com.sergiodev.bingo.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sergiodev.bingo.di.AppContainer
import com.sergiodev.bingo.presentation.CreateBoardHolder

/** Two-pane shell: boards on the left (2/3), game setup and play on the right (1/3). */
@Composable
fun AppWindow(container: AppContainer) {
    var createHolder by remember { mutableStateOf<CreateBoardHolder?>(null) }
    var pasteDialogOpen by remember { mutableStateOf(false) }
    val importExport = container.importExportHolder
    val importExportState by importExport.state.collectAsState()

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                AppMenu(
                    onExportToFile = importExport::onExportToFile,
                    onCopyToClipboard = importExport::onCopyToClipboard,
                    onImportFromFile = importExport::onImportFromFile,
                    onImportPasted = {
                        importExport.onPasteDialogReset()
                        pasteDialogOpen = true
                    },
                )
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    BoardsPane(
                        boardsState = container.boardsState,
                        onAddBoard = { createHolder = container.newCreateBoardHolder() },
                        modifier = Modifier.weight(2f).fillMaxHeight(),
                    )
                    PlaceholderPane("Game", Modifier.weight(1f).fillMaxHeight())
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

@Composable
private fun PlaceholderPane(title: String, modifier: Modifier) {
    Box(modifier = modifier.padding(16.dp), contentAlignment = Alignment.Center) {
        Text(title)
    }
}
