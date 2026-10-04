package com.sergiodev.bingo.ui

import com.sergiodev.bingo.domain.model.GameMode
import com.sergiodev.bingo.domain.repository.ImportResult
import com.sergiodev.bingo.domain.repository.ThemeMode
import com.sergiodev.bingo.presentation.CreateBoardErrorReason
import com.sergiodev.bingo.presentation.ExportOutcome
import com.sergiodev.bingo.presentation.GamePlayInputError
import com.sergiodev.bingo.presentation.ImportErrorReason
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.getSystemResourceEnvironment

/** Reason types from the holders become localized text only here, through the real resource files. */
class ReasonMappingTest {
    private val original = Locale.getDefault()

    @AfterTest
    fun restoreLocale() = Locale.setDefault(original)

    private fun text(tag: String, uiText: UiText): String {
        Locale.setDefault(Locale.forLanguageTag(tag))
        return runBlocking { getString(getSystemResourceEnvironment(), uiText.res, *uiText.args.toTypedArray()) }
    }

    @Test
    fun duplicate_call_message_includes_the_number_in_both_languages() {
        assertContains(text("en-US", GamePlayInputError.DuplicateCall(47).uiText()), "47")
        assertEquals("Number already called: 47", text("en-US", GamePlayInputError.DuplicateCall(47).uiText()))
        assertEquals("Número ya cantado: 12", text("fr-FR", GamePlayInputError.DuplicateCall(12).uiText()))
    }

    @Test
    fun play_input_errors_map_to_distinct_messages() {
        val messages = listOf(
            GamePlayInputError.InvalidNumber,
            GamePlayInputError.LetterMismatch,
            GamePlayInputError.DuplicateCall(1),
        ).map { text("en-US", it.uiText()) }
        assertEquals(3, messages.toSet().size)
        assertEquals("Invalid number (1-75)", messages[0])
    }

    @Test
    fun create_board_reasons_map_in_both_languages() {
        assertEquals("The identifier cannot be blank", text("en-US", CreateBoardErrorReason.BlankIdentifier.uiText()))
        assertEquals("Ya existe un cartón con ese identificador", text("fr-FR", CreateBoardErrorReason.DuplicateIdentifier.uiText()))
    }

    @Test
    fun import_reasons_and_counts_map_with_their_arguments() {
        assertEquals("The content is not valid boards JSON; no board was imported", text("en-US", ImportErrorReason.InvalidJson.uiText()))
        assertNotEquals(text("en-US", ImportErrorReason.BlankInput.uiText()), text("en-US", ImportErrorReason.UnreadableFile.uiText()))
        assertEquals("1 imported, 3 skipped", text("en-US", ImportResult(imported = 1, skipped = 3).uiText()))
        assertEquals("Importados: 1, omitidos: 3", text("fr-FR", ImportResult(imported = 1, skipped = 3).uiText()))
    }

    @Test
    fun export_outcomes_map_and_saved_includes_the_path() {
        assertContains(text("en-US", ExportOutcome.Saved("/tmp/boards.json").uiText()), "/tmp/boards.json")
        assertNotEquals(text("en-US", ExportOutcome.Copied.uiText()), text("en-US", ExportOutcome.Failed.uiText()))
    }

    @Test
    fun pattern_labels_name_the_column_letter_and_full_card() {
        assertEquals("Column G", text("en-US", patternUiText("COLUMN_G")))
        assertEquals("Columna G", text("fr-FR", patternUiText("COLUMN_G")))
        assertEquals("Full card", text("en-US", patternUiText("FULL_CARD")))
        assertEquals("L", text("en-US", patternUiText("L")))
        assertEquals("O", text("fr-FR", patternUiText("O")))
        assertEquals("I", text("en-US", patternUiText("I")))
        assertEquals("X7", text("en-US", patternUiText("X7")))
    }

    @Test
    fun modes_and_themes_have_localized_labels() {
        assertEquals(listOf("Column", "O", "L", "I", "Full card"), GameMode.entries.map { text("en-US", it.uiText()) })
        assertEquals(listOf("Claro", "Oscuro", "Sistema"), ThemeMode.entries.map { text("fr-FR", it.uiText()) })
    }
}
