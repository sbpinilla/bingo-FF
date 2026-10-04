package com.sergiodev.bingo.ui

import com.sergiodev.bingo.resources.*
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.onClick
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.foundation.PointerMatcher
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.sergiodev.bingo.domain.model.BingoLetter
import com.sergiodev.bingo.domain.model.GameMode
import com.sergiodev.bingo.presentation.GamePlayHolder
import com.sergiodev.bingo.presentation.GamePlayInputError
import com.sergiodev.bingo.presentation.GamePlayUiState
import com.sergiodev.bingo.presentation.GameSessionHolder
import com.sergiodev.bingo.presentation.PlayPaneState
import com.sergiodev.bingo.domain.game.PredictionCandidate
import com.sergiodev.bingo.presentation.sanitizeNumberInput

/**
 * Right pane while a game is active: number input with letter chips, announcements, the per-letter
 * called grid (COLUMNA rows can be dismissed) and the confirmed end-game action. Scrolls vertically
 * so it stays usable at about a third of the window width.
 */
@Composable
fun PlayPane(play: GamePlayHolder, prediction: PlayPaneState, session: GameSessionHolder, modifier: Modifier = Modifier) {
    val state by play.state.collectAsState()
    val possible by prediction.possibleWinners.collectAsState()
    var confirmingEnd by remember { mutableStateOf(false) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    // Keep the number field focused after every action so the operator can keep typing calls.
    val toggle: (BingoLetter) -> Unit = {
        play.onLetterDismissToggled(it)
        focus.requestFocus()
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(Res.string.game_play_title), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(Res.string.game_play_mode_status, state.mode.uiText().resolve(), state.calledCount))
        WinnerAnnouncements(state)
        NumberEntry(state, play, focus)
        HorizontalDivider()
        CalledGrid(state, onToggle = toggle)
        PossibleWinners(possible, onToggle = toggle)
        Button(onClick = { confirmingEnd = true }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.game_play_end_game_button))
        }
    }
    if (confirmingEnd) {
        EndGameDialog(
            onConfirm = {
                confirmingEnd = false
                session.end()
            },
            onDismiss = { confirmingEnd = false },
        )
    }
}

@Composable
private fun WinnerAnnouncements(state: GamePlayUiState) {
    // Newest first so the latest bingo is visible without scrolling.
    state.winners.asReversed().forEach { win ->
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth().border(2.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium),
        ) {
            Text(
                stringResource(Res.string.game_play_bingo_announcement, win.sequentialNumber, win.identifier, patternLabel(win.patternId)),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}

@Composable
private fun NumberEntry(state: GamePlayUiState, play: GamePlayHolder, focus: FocusRequester) {
    val submit: () -> Unit = {
        play.onSubmitCall()
        focus.requestFocus()
    }
    OutlinedTextField(
        value = state.numberInput,
        onValueChange = { play.onNumberInputChanged(sanitizeNumberInput(it)) },
        label = { Text(stringResource(Res.string.game_play_number_label)) },
        singleLine = true,
        isError = state.inputError != null,
        supportingText = { state.inputError?.let { Text(it.uiText().resolve()) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        keyboardActions = KeyboardActions(onDone = { submit() }),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focus)
            .onPreviewKeyEvent { event ->
                val enter = event.key == Key.Enter || event.key == Key.NumPadEnter
                if (enter && event.type == KeyEventType.KeyUp) submit()
                enter
            },
    )
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        BingoLetter.entries.forEach { letter ->
            FilterChip(
                selected = state.selectedLetter == letter,
                onClick = {
                    play.onLetterSelected(letter)
                    focus.requestFocus()
                },
                label = { Text(letter.name) },
            )
        }
    }
    Button(onClick = submit, modifier = Modifier.fillMaxWidth()) { Text(stringResource(Res.string.game_play_submit_call_button)) }
}

/** Five bordered rows B-I-N-G-O; COLUMNA rows get a toggle button and right-click to dismiss. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CalledGrid(state: GamePlayUiState, onToggle: (BingoLetter) -> Unit) {
    val dismissible = state.mode == GameMode.COLUMNA
    Column(Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline)) {
        BingoLetter.entries.forEachIndexed { index, letter ->
            val dismissed = dismissible && letter in state.dismissedLetters
            val calls = state.callsByLetter[letter].orEmpty()
            val rowModifier = if (dismissible) {
                Modifier.onClick(matcher = PointerMatcher.mouse(PointerButton.Secondary)) { onToggle(letter) }
            } else {
                Modifier
            }
            Row(
                modifier = rowModifier.fillMaxWidth().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(Res.string.game_play_letter_calls, letter.name, calls.joinToString(", ")),
                    modifier = Modifier.weight(1f).padding(vertical = 8.dp),
                    textDecoration = if (dismissed) TextDecoration.LineThrough else null,
                    color = if (dismissed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                )
                if (dismissible) {
                    TextButton(onClick = { onToggle(letter) }) { Text(stringResource(if (dismissed) Res.string.game_play_column_reopen else Res.string.game_play_column_close)) }
                }
            }
            if (index < BingoLetter.entries.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        }
    }
}

/**
 * "Posibles ganadores": boards close to winning, fewest missing first. COLUMNA rows name their
 * letter and can be dismissed (button or right-click), which removes every candidate of that letter.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PossibleWinners(candidates: List<PredictionCandidate>, onToggle: (BingoLetter) -> Unit) {
    Text(stringResource(Res.string.game_play_possible_winners_title), style = MaterialTheme.typography.titleMedium)
    if (candidates.isEmpty()) {
        Text(stringResource(Res.string.game_play_possible_winners_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    Column(Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline)) {
        candidates.forEachIndexed { index, candidate ->
            val letter = candidate.letter
            val rowModifier = if (letter != null) {
                Modifier.onClick(matcher = PointerMatcher.mouse(PointerButton.Secondary)) { onToggle(letter) }
            } else {
                Modifier
            }
            Row(
                modifier = rowModifier.fillMaxWidth().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "#${candidate.boardId} ${candidate.identifier}" + (letter?.let { " · ${it.name}" } ?: "") +
                        " · faltan ${candidate.missing}",
                    modifier = Modifier.weight(1f).padding(vertical = 8.dp),
                )
                if (letter != null) {
                    TextButton(onClick = { onToggle(letter) }) { Text(stringResource(Res.string.game_play_column_close)) }
                }
            }
            if (index < candidates.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        }
    }
}
