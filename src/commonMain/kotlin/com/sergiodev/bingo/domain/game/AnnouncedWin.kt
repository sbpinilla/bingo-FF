package com.sergiodev.bingo.domain.game

/**
 * Identifies a (board, pattern) pair that has already been announced as a
 * winner in the current session, so it is never re-announced.
 */
data class AnnouncedWin(val boardId: Long, val patternId: String)
