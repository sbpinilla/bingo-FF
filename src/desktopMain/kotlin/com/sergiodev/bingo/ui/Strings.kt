package com.sergiodev.bingo.ui

import androidx.compose.runtime.Composable
import com.sergiodev.bingo.domain.model.GameMode
import com.sergiodev.bingo.domain.repository.ImportResult
import com.sergiodev.bingo.domain.repository.ThemeMode
import com.sergiodev.bingo.presentation.CreateBoardErrorReason
import com.sergiodev.bingo.presentation.ExportOutcome
import com.sergiodev.bingo.presentation.GamePlayInputError
import com.sergiodev.bingo.presentation.ImportErrorReason
import com.sergiodev.bingo.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * A localized message: a string resource plus its format arguments. Holders expose reason types only;
 * this is the single place where they are mapped to text, resolved against the JVM default locale.
 */
class UiText(val res: StringResource, val args: List<Any> = emptyList())

@Composable
fun UiText.resolve(): String = stringResource(res, *args.toTypedArray())

internal fun GamePlayInputError.uiText(): UiText = when (this) {
    GamePlayInputError.InvalidNumber -> UiText(Res.string.game_play_error_invalid_number)
    GamePlayInputError.LetterMismatch -> UiText(Res.string.game_play_error_letter_mismatch)
    is GamePlayInputError.DuplicateCall -> UiText(Res.string.game_play_error_duplicate_call, listOf(number))
}

internal fun CreateBoardErrorReason.uiText(): UiText = when (this) {
    CreateBoardErrorReason.BlankIdentifier -> UiText(Res.string.create_board_error_blank_identifier)
    CreateBoardErrorReason.DuplicateIdentifier -> UiText(Res.string.create_board_error_duplicate_identifier)
}

internal fun ImportErrorReason.uiText(): UiText = when (this) {
    ImportErrorReason.BlankInput -> UiText(Res.string.import_boards_error_blank_input)
    ImportErrorReason.InvalidJson -> UiText(Res.string.import_boards_error_invalid_json)
    ImportErrorReason.UnreadableFile -> UiText(Res.string.import_boards_error_unreadable_file)
}

internal fun ImportResult.uiText(): UiText = UiText(Res.string.import_result_message, listOf(imported, skipped))

internal fun ExportOutcome.uiText(): UiText = when (this) {
    is ExportOutcome.Saved -> UiText(Res.string.export_saved_message, listOf(path))
    ExportOutcome.Copied -> UiText(Res.string.export_copied_message)
    ExportOutcome.Failed -> UiText(Res.string.export_failed_message)
}

internal fun patternUiText(patternId: String): UiText = when {
    patternId.startsWith("COLUMN_") -> UiText(Res.string.pattern_column, listOf(patternId.removePrefix("COLUMN_")))
    patternId == "FULL_CARD" -> UiText(Res.string.pattern_full_card)
    patternId == "O" -> UiText(Res.string.pattern_o)
    patternId == "L" -> UiText(Res.string.pattern_l)
    patternId == "I" -> UiText(Res.string.pattern_i)
    else -> UiText(Res.string.pattern_other, listOf(patternId))
}

internal fun GameMode.uiText(): UiText = UiText(
    when (this) {
        GameMode.COLUMNA -> Res.string.game_setup_mode_columna
        GameMode.O -> Res.string.game_setup_mode_o
        GameMode.L -> Res.string.game_setup_mode_l
        GameMode.I -> Res.string.game_setup_mode_i
        GameMode.CARTON_COMPLETO -> Res.string.game_setup_mode_carton_completo
    },
)

internal fun ThemeMode.uiText(): UiText = UiText(
    when (this) {
        ThemeMode.LIGHT -> Res.string.theme_light_option
        ThemeMode.DARK -> Res.string.theme_dark_option
        ThemeMode.SYSTEM -> Res.string.theme_system_option
    },
)
