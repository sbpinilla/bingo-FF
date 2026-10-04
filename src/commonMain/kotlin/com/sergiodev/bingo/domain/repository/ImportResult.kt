package com.sergiodev.bingo.domain.repository

/** Result of [BoardRepository.importBoards]: how many entries were inserted vs. skipped. */
data class ImportResult(val imported: Int, val skipped: Int)
