package com.sergiodev.bingo.ui

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
        title = { Text("Importar cartones") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = state.jsonText,
                    onValueChange = onTextChange,
                    label = { Text("JSON de cartones") },
                    isError = state.importError != null,
                    supportingText = { state.importError?.let { Text(it.message()) } },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp, max = 400.dp),
                )
            }
        },
        confirmButton = { TextButton(onClick = onSubmit) { Text("Importar") } },
        dismissButton = { TextButton(onClick = onClose) { Text("Cancelar") } },
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
        NoticeDialog("Importación terminada", summary.message(), onSummaryDismissed)
    }
    val error = state.importError
    if (error != null && !pasteDialogOpen) {
        NoticeDialog("No se pudo importar", error.message(), onErrorDismissed)
    }
    state.exportOutcome?.let { outcome ->
        NoticeDialog("Exportar cartones", outcome.message(), onExportDismissed)
    }
}

@Composable
private fun NoticeDialog(title: String, message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Aceptar") } },
    )
}

private fun ImportErrorReason.message(): String = when (this) {
    ImportErrorReason.BlankInput -> "No hay nada que importar: el contenido está vacío"
    ImportErrorReason.InvalidJson -> "El contenido no es un JSON de cartones válido; no se importó ningún cartón"
    ImportErrorReason.UnreadableFile -> "No se pudo leer el archivo seleccionado"
}

private fun ImportResult.message(): String = "Importados: $imported, omitidos: $skipped"

private fun ExportOutcome.message(): String = when (this) {
    is ExportOutcome.Saved -> "Cartones exportados a $path"
    ExportOutcome.Copied -> "Cartones copiados al portapapeles como JSON"
    ExportOutcome.Failed -> "No se pudo escribir el archivo"
}
