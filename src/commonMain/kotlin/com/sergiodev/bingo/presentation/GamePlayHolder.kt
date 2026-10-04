package com.sergiodev.bingo.presentation

import androidx.compose.runtime.Immutable
import com.sergiodev.bingo.domain.game.WinAnnouncement
import com.sergiodev.bingo.domain.model.BingoLetter
import com.sergiodev.bingo.domain.model.GameMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Why a call could not be confirmed. The UI resolves each case to text. */
sealed interface GamePlayInputError {
    data object InvalidNumber : GamePlayInputError
    data object LetterMismatch : GamePlayInputError
    data class DuplicateCall(val number: Int) : GamePlayInputError
}

/** Projection for the Play pane. [callsByLetter] always has the five letters, in B-I-N-G-O order. */
@Immutable
data class GamePlayUiState(
    val numberInput: String = "",
    val selectedLetter: BingoLetter? = null,
    val letterOverridden: Boolean = false,
    val inputError: GamePlayInputError? = null,
    val callsByLetter: Map<BingoLetter, List<Int>> = groupByLetter(emptyList()),
    val winners: List<WinAnnouncement> = emptyList(),
    val mode: GameMode = GameMode.COLUMNA,
    val calledCount: Int = 0,
    val dismissedLetters: Set<BingoLetter> = emptySet(),
)

private data class PendingEntry(
    val numberInput: String = "",
    val selectedLetter: BingoLetter? = null,
    val overridden: Boolean = false,
    val error: GamePlayInputError? = null,
)

private fun groupByLetter(called: List<Int>): Map<BingoLetter, List<Int>> =
    BingoLetter.entries.associateWith { letter -> called.filter { it in letter.range } }

/**
 * Number entry and call projection for the Play pane (port of the Android `GamePlayViewModel`).
 * Calls and dismissed letters live in [GameSessionHolder] (persisted); only the half-typed entry
 * lives here. Winners come from the session's replay, so they survive restarts.
 */
class GamePlayHolder(
    private val session: GameSessionHolder,
    scope: CoroutineScope,
) {
    private val pending = MutableStateFlow(PendingEntry())

    val state: StateFlow<GamePlayUiState> = combine(session.active, session.session, pending) { game, replayed, entry ->
        if (game == null) {
            GamePlayUiState()
        } else {
            GamePlayUiState(
                numberInput = entry.numberInput,
                selectedLetter = entry.selectedLetter,
                letterOverridden = entry.overridden,
                inputError = entry.error,
                callsByLetter = groupByLetter(game.calledNumbers),
                winners = replayed?.winners.orEmpty(),
                mode = game.mode,
                calledCount = game.calledNumbers.size,
                dismissedLetters = game.dismissedLetters,
            )
        }
    }.stateIn(scope, SharingStarted.Eagerly, GamePlayUiState())

    init {
        // A finished game must not leak a half-typed entry into the next one.
        scope.launch { session.active.collect { if (it == null) pending.value = PendingEntry() } }
    }

    fun onNumberInputChanged(value: String) {
        pending.update { current ->
            if (current.overridden) {
                current.copy(numberInput = value, error = null)
            } else {
                val derived = value.toIntOrNull()?.let(BingoLetter::fromNumber)
                current.copy(numberInput = value, selectedLetter = derived, error = null)
            }
        }
    }

    fun onLetterSelected(letter: BingoLetter) {
        pending.update { it.copy(selectedLetter = letter, overridden = true, error = null) }
    }

    /** Toggles a COLUMNA letter as dismissed: filters its predictions and voids its column win. */
    fun onLetterDismissToggled(letter: BingoLetter) {
        session.toggleDismiss(letter)
    }

    /**
     * Confirms the pending call. A manual letter override is never trusted blindly: it must match
     * the number's real letter. A duplicate clears the field so the next number can be typed at once.
     */
    fun onSubmitCall() {
        if (session.active.value == null) return
        val current = pending.value
        val number = current.numberInput.toIntOrNull()
        val derived = number?.let(BingoLetter::fromNumber)

        if (number == null || derived == null) {
            pending.update { it.copy(error = GamePlayInputError.InvalidNumber) }
            return
        }
        if ((current.selectedLetter ?: derived) != derived) {
            pending.update { it.copy(error = GamePlayInputError.LetterMismatch) }
            return
        }
        if (!session.call(number)) {
            pending.value = PendingEntry(error = GamePlayInputError.DuplicateCall(number))
            return
        }
        pending.value = PendingEntry()
    }
}
