package com.sergiodev.bingo.domain.model

/**
 * A single cell coordinate on a 5x5 bingo grid: [column] is the letter, [row] is 1..5.
 */
data class GridPosition(val column: BingoLetter, val row: Int)
