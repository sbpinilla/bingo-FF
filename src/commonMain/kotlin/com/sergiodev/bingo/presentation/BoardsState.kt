package com.sergiodev.bingo.presentation

import com.sergiodev.bingo.domain.model.BoardCard
import com.sergiodev.bingo.domain.repository.BoardRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** List projection of the boards plus the board awaiting delete confirmation, if any. */
data class BoardsUiState(
    val boards: List<BoardCard> = emptyList(),
    val pendingDelete: BoardCard? = null,
)

/** Holder for the left-pane board list: observes the repository and drives delete confirmation. */
class BoardsState(
    private val repository: BoardRepository,
    private val scope: CoroutineScope,
) {
    private val pendingDelete = MutableStateFlow<BoardCard?>(null)

    val state: StateFlow<BoardsUiState> = combine(repository.observeBoards(), pendingDelete) { boards, pending ->
        BoardsUiState(boards = boards, pendingDelete = pending)
    }.stateIn(scope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), BoardsUiState())

    fun onDeleteRequested(board: BoardCard) {
        pendingDelete.value = board
    }

    fun onDeleteConfirmed() {
        val board = pendingDelete.value ?: return
        pendingDelete.value = null
        scope.launch { repository.deleteBoard(board.id) }
    }

    fun onDeleteDismissed() {
        pendingDelete.value = null
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
