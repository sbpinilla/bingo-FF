package com.sergiodev.bingo.presentation

import com.sergiodev.bingo.data.json.BoardJsonCodec
import com.sergiodev.bingo.domain.repository.BoardRepository
import com.sergiodev.bingo.domain.repository.ImportResult
import com.sergiodev.bingo.platform.Clipboard
import com.sergiodev.bingo.platform.FileDialogs
import com.sergiodev.bingo.platform.TextFiles
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Reasons an import can be blocked. The UI layer maps each case to display text. */
sealed interface ImportErrorReason {
    data object BlankInput : ImportErrorReason
    data object InvalidJson : ImportErrorReason
    data object UnreadableFile : ImportErrorReason
}

/** What happened on the last export action. */
sealed interface ExportOutcome {
    data class Saved(val path: String) : ExportOutcome
    data object Copied : ExportOutcome
    data object Failed : ExportOutcome
}

/**
 * [jsonText] is the pasted import text, [importError] the last blocking reason,
 * [importSummary] the clearable post-import counts, [exportOutcome] the clearable export result.
 */
data class ImportExportState(
    val jsonText: String = "",
    val importError: ImportErrorReason? = null,
    val importSummary: ImportResult? = null,
    val exportOutcome: ExportOutcome? = null,
)

/**
 * Holder for board export/import. Import is all-or-nothing on malformed input and delegates the
 * dedup rules to [BoardRepository.importBoards]. A cancelled dialog changes nothing.
 */
class ImportExportHolder(
    private val repository: BoardRepository,
    private val dialogs: FileDialogs,
    private val files: TextFiles,
    private val clipboard: Clipboard,
    private val scope: CoroutineScope,
) {
    private val _state = MutableStateFlow(ImportExportState())
    val state: StateFlow<ImportExportState> = _state.asStateFlow()

    fun onJsonTextChange(text: String) {
        _state.update { it.copy(jsonText = text, importError = null) }
    }

    fun onSubmitPasted() {
        val text = _state.value.jsonText
        _state.update { it.copy(jsonText = "") }
        scope.launch { importText(text) }
    }

    fun onImportFromFile() {
        scope.launch {
            val path = dialogs.pickOpen() ?: return@launch
            val text = attempt { files.read(path) }
            if (text == null) {
                _state.update { it.copy(importError = ImportErrorReason.UnreadableFile) }
                return@launch
            }
            importText(text)
        }
    }

    fun onExportToFile() {
        scope.launch {
            val json = BoardJsonCodec.encode(repository.observeBoards().first())
            val path = dialogs.pickSave(DEFAULT_EXPORT_NAME) ?: return@launch
            val outcome = if (attempt { files.write(path, json) } != null) ExportOutcome.Saved(path) else ExportOutcome.Failed
            _state.update { it.copy(exportOutcome = outcome) }
        }
    }

    fun onCopyToClipboard() {
        scope.launch {
            clipboard.copy(BoardJsonCodec.encode(repository.observeBoards().first()))
            _state.update { it.copy(exportOutcome = ExportOutcome.Copied) }
        }
    }

    fun onSummaryDismissed() {
        _state.update { it.copy(importSummary = null) }
    }

    fun onExportOutcomeDismissed() {
        _state.update { it.copy(exportOutcome = null) }
    }

    /** Clears pasted text and any import error when the paste dialog is opened or closed. */
    fun onPasteDialogReset() {
        _state.update { it.copy(jsonText = "", importError = null) }
    }

    private suspend fun importText(text: String) {
        if (text.isBlank()) {
            _state.update { it.copy(importError = ImportErrorReason.BlankInput) }
            return
        }
        val boards = attempt { BoardJsonCodec.decode(text) }
        if (boards == null) {
            _state.update { it.copy(importError = ImportErrorReason.InvalidJson) }
            return
        }
        val result = repository.importBoards(boards)
        _state.update { it.copy(importError = null, importSummary = result) }
    }

    /** Runs [block], returning `null` on any failure except cancellation, which propagates. */
    private suspend fun <T : Any> attempt(block: suspend () -> T): T? = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        null
    }

    private companion object {
        const val DEFAULT_EXPORT_NAME = "boards.json"
    }
}
