package com.sergiodev.bingo.domain.game

import com.sergiodev.bingo.domain.model.GameMode

/**
 * Immutable session state for one active game. [announced] and
 * [winners] are never persisted: [replay] rebuilds them deterministically from
 * the called numbers (and, in COLUMNA, the dismissed letters that void their
 * column wins).
 */
data class GameSession(
    val mode: GameMode,
    val calledNumbers: List<Int> = emptyList(),
    val announced: Set<AnnouncedWin> = emptySet(),
    val winners: List<WinAnnouncement> = emptyList(),
)
