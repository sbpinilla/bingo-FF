package com.sergiodev.bingo.presentation

import com.sergiodev.bingo.data.json.BoardJsonCodec
import com.sergiodev.bingo.domain.model.BoardCard
import com.sergiodev.bingo.domain.repository.ImportResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ImportExportHolderTest {

    private val boardA = BoardCard(1L, "A", List(24) { it + 1 })
    private val boardC = BoardCard(3L, "C", List(24) { it + 2 })
    private val boardC2 = BoardCard(4L, "C", List(24) { it + 3 })

    private val dialogs = FakeFileDialogs()
    private val files = FakeTextFiles()
    private val clipboard = FakeClipboard()

    private fun TestScope.holder(repo: FakeBoardRepository) =
        ImportExportHolder(repo, dialogs, files, clipboard, backgroundScope)

    private val mixedPayload = BoardJsonCodec.encode(
        listOf(BoardCard(1L, "X", boardA.numbers), BoardCard(2L, "A", boardA.numbers), boardC, boardC2),
    )

    // --- pasted JSON ---

    @Test
    fun blank_paste_exposes_BlankInput_and_imports_nothing() = runTest {
        val repo = FakeBoardRepository()
        val holder = holder(repo)

        holder.onJsonTextChange("   \n")
        holder.onSubmitPasted()
        runCurrent()

        assertEquals(ImportErrorReason.BlankInput, holder.state.value.importError)
        assertEquals(0, repo.importCalls)
    }

    @Test
    fun invalid_json_exposes_InvalidJson_clears_text_and_imports_nothing() = runTest {
        val repo = FakeBoardRepository()
        val holder = holder(repo)

        holder.onJsonTextChange("not valid json {{{")
        holder.onSubmitPasted()
        runCurrent()

        assertEquals(ImportErrorReason.InvalidJson, holder.state.value.importError)
        assertEquals("", holder.state.value.jsonText)
        assertEquals(0, repo.importCalls)
    }

    @Test
    fun one_entry_missing_numbers_rejects_the_valid_ones_too() = runTest {
        val repo = FakeBoardRepository()
        val holder = holder(repo)
        val payload = """[{"id":1,"identifier":"A","numbers":[1]},{"id":2,"identifier":"B"}]"""

        holder.onJsonTextChange(payload)
        holder.onSubmitPasted()
        runCurrent()

        assertEquals(ImportErrorReason.InvalidJson, holder.state.value.importError)
        assertEquals(emptyList(), repo.boards.value)
    }

    @Test
    fun mixed_payload_adds_only_id_3_and_summarises_imported_1_skipped_3() = runTest {
        val repo = FakeBoardRepository(listOf(boardA))
        val holder = holder(repo)

        holder.onJsonTextChange(mixedPayload)
        holder.onSubmitPasted()
        runCurrent()

        assertEquals(ImportResult(imported = 1, skipped = 3), holder.state.value.importSummary)
        assertEquals(listOf(boardA, boardC), repo.boards.value)
        assertEquals("", holder.state.value.jsonText)
        assertNull(holder.state.value.importError)
    }

    @Test
    fun summary_can_be_cleared() = runTest {
        val repo = FakeBoardRepository(listOf(boardA))
        val holder = holder(repo)
        holder.onJsonTextChange(mixedPayload)
        holder.onSubmitPasted()
        runCurrent()

        holder.onSummaryDismissed()

        assertNull(holder.state.value.importSummary)
    }

    @Test
    fun editing_the_text_clears_the_error() = runTest {
        val holder = holder(FakeBoardRepository())
        holder.onJsonTextChange("")
        holder.onSubmitPasted()
        runCurrent()
        assertEquals(ImportErrorReason.BlankInput, holder.state.value.importError)

        holder.onJsonTextChange("[")

        assertNull(holder.state.value.importError)
        assertEquals("[", holder.state.value.jsonText)
    }

    // --- import from file ---

    @Test
    fun file_import_reads_the_picked_file_and_summarises() = runTest {
        val repo = FakeBoardRepository(listOf(boardA))
        val holder = holder(repo)
        dialogs.openResult = "/tmp/in.json"
        files.files["/tmp/in.json"] = mixedPayload

        holder.onImportFromFile()
        runCurrent()

        assertEquals(ImportResult(imported = 1, skipped = 3), holder.state.value.importSummary)
        assertEquals(listOf(boardA, boardC), repo.boards.value)
    }

    @Test
    fun cancelled_open_dialog_does_nothing_and_shows_no_error() = runTest {
        val repo = FakeBoardRepository()
        val holder = holder(repo)
        dialogs.openResult = null

        holder.onImportFromFile()
        runCurrent()

        assertEquals(1, dialogs.openCalls)
        assertEquals(ImportExportState(), holder.state.value)
        assertEquals(0, repo.importCalls)
    }

    @Test
    fun unreadable_file_exposes_UnreadableFile_and_imports_nothing() = runTest {
        val repo = FakeBoardRepository()
        val holder = holder(repo)
        dialogs.openResult = "/tmp/locked.json"
        files.unreadable += "/tmp/locked.json"

        holder.onImportFromFile()
        runCurrent()

        assertEquals(ImportErrorReason.UnreadableFile, holder.state.value.importError)
        assertEquals(0, repo.importCalls)
    }

    @Test
    fun file_with_non_board_content_is_rejected_as_InvalidJson() = runTest {
        val repo = FakeBoardRepository()
        val holder = holder(repo)
        dialogs.openResult = "/tmp/photo.json"
        files.files["/tmp/photo.json"] = "\u0000PNG not json"

        holder.onImportFromFile()
        runCurrent()

        assertEquals(ImportErrorReason.InvalidJson, holder.state.value.importError)
        assertEquals(0, repo.importCalls)
    }

    @Test
    fun blank_file_exposes_BlankInput() = runTest {
        val holder = holder(FakeBoardRepository())
        dialogs.openResult = "/tmp/empty.json"
        files.files["/tmp/empty.json"] = ""

        holder.onImportFromFile()
        runCurrent()

        assertEquals(ImportErrorReason.BlankInput, holder.state.value.importError)
    }

    // --- export ---

    @Test
    fun export_writes_a_bare_array_to_the_picked_path_with_a_default_name() = runTest {
        val repo = FakeBoardRepository(listOf(boardA, boardC))
        val holder = holder(repo)
        dialogs.saveResult = "/tmp/out.json"

        holder.onExportToFile()
        runCurrent()

        assertEquals("boards.json", dialogs.lastSuggestedName)
        assertEquals(BoardJsonCodec.encode(listOf(boardA, boardC)), files.files["/tmp/out.json"])
        assertEquals(ExportOutcome.Saved("/tmp/out.json"), holder.state.value.exportOutcome)
    }

    @Test
    fun cancelled_save_dialog_writes_nothing_and_shows_nothing() = runTest {
        val holder = holder(FakeBoardRepository(listOf(boardA)))
        dialogs.saveResult = null

        holder.onExportToFile()
        runCurrent()

        assertEquals(1, dialogs.saveCalls)
        assertEquals(0, files.writeCalls)
        assertEquals(ImportExportState(), holder.state.value)
    }

    @Test
    fun failed_write_exposes_ExportFailed() = runTest {
        val holder = holder(FakeBoardRepository(listOf(boardA)))
        dialogs.saveResult = "/tmp/out.json"
        files.failWrites = true

        holder.onExportToFile()
        runCurrent()

        assertEquals(ExportOutcome.Failed, holder.state.value.exportOutcome)
    }

    @Test
    fun copy_puts_the_same_json_on_the_clipboard() = runTest {
        val holder = holder(FakeBoardRepository(listOf(boardA, boardC)))

        holder.onCopyToClipboard()
        runCurrent()

        assertEquals(BoardJsonCodec.encode(listOf(boardA, boardC)), clipboard.copied)
        assertEquals(ExportOutcome.Copied, holder.state.value.exportOutcome)
    }

    @Test
    fun export_outcome_can_be_cleared() = runTest {
        val holder = holder(FakeBoardRepository(listOf(boardA)))
        holder.onCopyToClipboard()
        runCurrent()

        holder.onExportOutcomeDismissed()

        assertNull(holder.state.value.exportOutcome)
    }

    @Test
    fun exported_file_imports_into_an_empty_repository_with_identical_boards() = runTest {
        val source = FakeBoardRepository(listOf(boardA, boardC))
        dialogs.saveResult = "/tmp/rt.json"
        holder(source).onExportToFile()
        runCurrent()

        val target = FakeBoardRepository()
        dialogs.openResult = "/tmp/rt.json"
        val importing = holder(target)
        importing.onImportFromFile()
        runCurrent()

        assertEquals(listOf(boardA, boardC), target.boards.value)
        assertEquals(ImportResult(imported = 2, skipped = 0), importing.state.value.importSummary)
    }
}
