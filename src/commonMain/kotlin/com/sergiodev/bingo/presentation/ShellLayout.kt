package com.sergiodev.bingo.presentation

import kotlin.math.floor
import kotlin.math.max

/**
 * Pure sizing rules of the two-pane shell, in dp. The UI uses the same constants, so the
 * tests describe what is actually rendered: pane weights, adaptive column count and how
 * many compact cards fit in the window.
 */
object ShellLayout {
    const val LEFT_WEIGHT = 2f
    const val RIGHT_WEIGHT = 1f

    const val PANE_PADDING = 16f
    const val CARD_GAP = 8f
    const val MIN_CARD_WIDTH = 220f

    /** Padding inside a card, each side. */
    const val CARD_PADDING = 8f

    /** Cells are wider than tall so a 5x5 card stays short enough for three rows on 1080p. */
    const val CELL_ASPECT = 1.3f

    /** Header row, letters row and the gaps between card rows, plus the card padding. */
    const val CARD_CHROME = 72f

    /** Window title bar, pane title row and the vertical pane padding above the grid. */
    const val WINDOW_CHROME_ABOVE_GRID = 120f

    fun leftPaneWidth(windowWidth: Float): Float =
        windowWidth * LEFT_WEIGHT / (LEFT_WEIGHT + RIGHT_WEIGHT)

    fun gridWidth(windowWidth: Float): Float = leftPaneWidth(windowWidth) - 2 * PANE_PADDING

    fun gridHeight(windowHeight: Float): Float = windowHeight - WINDOW_CHROME_ABOVE_GRID

    /** Same rule as Compose `GridCells.Adaptive(MIN_CARD_WIDTH)`: at least one column. */
    fun columnCount(gridWidth: Float): Int =
        max(1, floor((gridWidth + CARD_GAP) / (MIN_CARD_WIDTH + CARD_GAP)).toInt())

    fun cardWidth(gridWidth: Float): Float {
        val columns = columnCount(gridWidth)
        return (gridWidth - (columns - 1) * CARD_GAP) / columns
    }

    fun cardHeight(cardWidth: Float): Float {
        val cellWidth = (cardWidth - 2 * CARD_PADDING) / 5f
        return CARD_CHROME + 5f * (cellWidth / CELL_ASPECT)
    }

    /** Whole card rows that fit in [gridHeight] for cards [cardWidth] wide. */
    fun visibleRows(gridHeight: Float, cardWidth: Float): Int =
        max(0, floor((gridHeight + CARD_GAP) / (cardHeight(cardWidth) + CARD_GAP)).toInt())
}
