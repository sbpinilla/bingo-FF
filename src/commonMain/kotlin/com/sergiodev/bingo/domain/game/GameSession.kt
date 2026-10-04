package com.sergiodev.bingo.domain.game

import com.sergiodev.bingo.domain.model.GameMode

/**
 * Immutable session state for one active game. Only [mode] and
 * [calledNumbers] are persisted (by the active-game repository); [announced] and
 * [winners] are rebuilt deterministically by replaying [calledNumbers] in
 * order through [BingoWinChecker.newWins].
 */
data class GameSession(
    val mode: GameMode,
    val calledNumbers: List<Int> = emptyList(),
    val announced: Set<AnnouncedWin> = emptySet(),
    val winners: List<WinAnnouncement> = emptyList(),
)
