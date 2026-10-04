package com.sergiodev.bingo.domain.game

/**
 * A single winner announcement surfaced to the UI: which board newly
 * completed which pattern.
 */
data class WinAnnouncement(
    val boardId: Long,
    val sequentialNumber: Long,
    val identifier: String,
    val patternId: String,
)
