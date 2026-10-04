package com.sergiodev.bingo.presentation

import com.sergiodev.bingo.domain.model.BingoLetter
import com.sergiodev.bingo.domain.repository.BoardRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 24 manual number entry fields grouped by column (5 each for B/I/G/O, 4 for N,
 * since the FREE cell is never entered).
 */
data class CreateBoardState(
    val identifier: String = "",
    val numbers: Map<BingoLetter, List<String>> = defaultNumbers(),
    val fieldErrors: Map<BingoLetter, List<String?>> = emptyMap(),
    val identifierError: CreateBoardErrorReason? = null,
    val submitSuccess: Boolean = false,
) {
    companion object {
        fun defaultNumbers(): Map<BingoLetter, List<String>> =
            BingoLetter.entries.associateWith { letter ->
                List(if (letter == BingoLetter.N) 4 else 5) { "" }
            }
    }
}

/** Reasons submit can be blocked. The UI layer maps each case to display text. */
sealed interface CreateBoardErrorReason {
    data object BlankIdentifier : CreateBoardErrorReason
    data object DuplicateIdentifier : CreateBoardErrorReason
}

/** State holder for the add-board dialog (ported from Android's CreateBoardViewModel). */
class CreateBoardHolder(
    private val repository: BoardRepository,
    private val scope: CoroutineScope,
) {
    private val _state = MutableStateFlow(CreateBoardState())
    val state: StateFlow<CreateBoardState> = _state.asStateFlow()

    fun onIdentifierChange(value: String) {
        _state.update { it.copy(identifier = value, identifierError = null) }
    }

    fun onNumberChange(letter: BingoLetter, index: Int, value: String) {
        _state.update { state ->
            val column = state.numbers.getValue(letter).toMutableList().also { it[index] = value }
            val newNumbers = state.numbers + (letter to column)
            state.copy(
                numbers = newNumbers,
                fieldErrors = if (state.fieldErrors.isEmpty()) {
                    emptyMap()
                } else {
                    computeFieldErrors(newNumbers, flagBlank = false)
                },
            )
        }
    }

    fun onSubmit() {
        val state = _state.value

        if (state.identifier.isBlank()) {
            _state.update { it.copy(identifierError = CreateBoardErrorReason.BlankIdentifier) }
            return
        }

        val fieldErrors = computeFieldErrors(state.numbers, flagBlank = true)
        if (fieldErrors.values.any { errors -> errors.any { it != null } }) {
            _state.update { it.copy(fieldErrors = fieldErrors) }
            return
        }

        val orderedNumbers = BingoLetter.entries.flatMap { letter ->
            state.numbers.getValue(letter).map(String::toInt)
        }

        scope.launch {
            repository.addBoard(state.identifier, orderedNumbers)
                .onSuccess { _state.update { it.copy(submitSuccess = true, fieldErrors = emptyMap()) } }
                .onFailure { _state.update { it.copy(identifierError = CreateBoardErrorReason.DuplicateIdentifier) } }
        }
    }
}

/**
 * Pure computation of per-field validity markers. Range is checked before duplicates, and
 * duplicate detection only considers values already in range. The returned strings are
 * markers only (`invalid`, `out_of_range`, `duplicate`); the UI reads nullness for `isError`.
 */
internal fun computeFieldErrors(
    numbers: Map<BingoLetter, List<String>>,
    flagBlank: Boolean,
): Map<BingoLetter, List<String?>> {
    val inRange = BingoLetter.entries.associateWith { letter ->
        numbers.getValue(letter).map { raw -> raw.toIntOrNull()?.takeIf { it in letter.range } }
    }
    val seen = mutableSetOf<Int>()
    val duplicates = mutableSetOf<Int>()
    inRange.values.flatten().filterNotNull().forEach { if (!seen.add(it)) duplicates += it }

    return BingoLetter.entries.associateWith { letter ->
        numbers.getValue(letter).map { raw ->
            val n = raw.toIntOrNull()
            when {
                n == null -> if (raw.isBlank() && !flagBlank) null else "invalid"
                n !in letter.range -> "out_of_range"
                n in duplicates -> "duplicate"
                else -> null
            }
        }
    }
}
