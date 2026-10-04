package com.sergiodev.bingo.ui

import com.sergiodev.bingo.resources.*
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sergiodev.bingo.domain.repository.ImportResult
import com.sergiodev.bingo.presentation.ExportOutcome
import com.sergiodev.bingo.presentation.ImportErrorReason
import com.sergiodev.bingo.presentation.ImportExportState

/** Modal dialog to paste board JSON; same title/body/buttons layout as the add-board dialog. */
@Composable
fun ImportDialog(
    state: ImportExportState,
    onTextChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onClose: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(stringResource(Res.string.import_boards_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = state.jsonText,
                    onValueChange = onTextChange,
                    label = { Text(stringResource(Res.string.import_boards_json_label)) },
                    isError = state.importError != null,
                    supportingText = { state.importError?.let { Text(it.uiText().resolve()) } },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp, max = 400.dp),
                )
            }
        },
        confirmButton = { TextButton(onClick = onSubmit) { Text(stringResource(Res.string.import_boards_submit_button)) } },
        dismissButton = { TextButton(onClick = onClose) { Text(stringResource(Res.string.common_cancel)) } },
    )
}

/** Notice for results that have no dialog of their own: import summary, file errors, export outcome. */
@Composable
fun ImportExportNotices(
    state: ImportExportState,
    pasteDialogOpen: Boolean,
    onSummaryDismissed: () -> Unit,
    onErrorDismissed: () -> Unit,
    onExportDismissed: () -> Unit,
) {
    state.importSummary?.let { summary ->
        NoticeDialog(stringResource(Res.string.import_result_title), summary.uiText().resolve(), onSummaryDismissed)
    }
    val error = state.importError
    if (error != null && !pasteDialogOpen) {
        NoticeDialog(stringResource(Res.string.import_error_title), error.uiText().resolve(), onErrorDismissed)
    }
    state.exportOutcome?.let { outcome ->
        NoticeDialog(stringResource(Res.string.export_title), outcome.uiText().resolve(), onExportDismissed)
    }
}

@Composable
private fun NoticeDialog(title: String, message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.common_accept)) } },
    )
}
