package com.sergiodev.bingo.domain.model

/**
 * A fixed set of grid coordinates that, when all satisfied, constitutes a win
 * for a given [GameMode]. [id] is a stable identifier (e.g. "COLUMN_B"),
 * persisted as part of [com.sergiodev.bingo.domain.game.AnnouncedWin].
 */
data class WinPattern(val id: String, val cells: Set<GridPosition>)
