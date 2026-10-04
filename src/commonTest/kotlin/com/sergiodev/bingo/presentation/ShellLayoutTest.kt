package com.sergiodev.bingo.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShellLayoutTest {

    @Test
    fun pane_weights_are_two_to_one() {
        assertEquals(2f, ShellLayout.LEFT_WEIGHT)
        assertEquals(1f, ShellLayout.RIGHT_WEIGHT)
    }

    @Test
    fun left_pane_takes_two_thirds_of_the_window() {
        assertEquals(1280f, ShellLayout.leftPaneWidth(1920f), 0.01f)
        assertEquals(800f, ShellLayout.leftPaneWidth(1200f), 0.01f)
    }

    @Test
    fun grid_width_is_the_left_pane_minus_its_padding() {
        assertEquals(1280f - 2 * ShellLayout.PANE_PADDING, ShellLayout.gridWidth(1920f), 0.01f)
    }

    @Test
    fun column_count_adapts_to_the_available_width() {
        // Grid width as handed to the adaptive grid, i.e. the left pane content width.
        assertEquals(5, ShellLayout.columnCount(1248f))
        assertEquals(3, ShellLayout.columnCount(768f))
        assertEquals(1, ShellLayout.columnCount(100f))
        assertEquals(1, ShellLayout.columnCount(0f))
    }

    @Test
    fun column_count_at_the_boundaries_of_a_column() {
        val min = ShellLayout.MIN_CARD_WIDTH
        val gap = ShellLayout.CARD_GAP
        assertEquals(2, ShellLayout.columnCount(2 * min + gap))
        assertEquals(1, ShellLayout.columnCount(2 * min + gap - 1f))
    }

    @Test
    fun window_widths_give_expected_columns() {
        assertEquals(5, ShellLayout.columnCount(ShellLayout.gridWidth(1920f)))
        assertEquals(4, ShellLayout.columnCount(ShellLayout.gridWidth(1440f)))
        assertEquals(3, ShellLayout.columnCount(ShellLayout.gridWidth(1200f)))
    }

    @Test
    fun card_width_fills_the_column() {
        val grid = ShellLayout.gridWidth(1920f)
        val columns = ShellLayout.columnCount(grid)
        val width = ShellLayout.cardWidth(grid)
        assertEquals(grid, columns * width + (columns - 1) * ShellLayout.CARD_GAP, 0.01f)
    }

    @Test
    fun card_height_is_far_less_than_a_square_card() {
        val width = ShellLayout.cardWidth(ShellLayout.gridWidth(1920f))
        assertTrue(ShellLayout.cardHeight(width) < width * 1.2f)
    }

    @Test
    fun about_fifteen_cards_are_visible_at_1920x1080() {
        val grid = ShellLayout.gridWidth(1920f)
        val rows = ShellLayout.visibleRows(ShellLayout.gridHeight(1080f), ShellLayout.cardWidth(grid))
        assertTrue(rows >= 3, "expected at least 3 rows, got $rows")
        assertTrue(ShellLayout.columnCount(grid) * rows >= 15)
    }

    @Test
    fun at_minimum_size_at_least_one_full_row_is_visible() {
        val grid = ShellLayout.gridWidth(1200f)
        val rows = ShellLayout.visibleRows(ShellLayout.gridHeight(700f), ShellLayout.cardWidth(grid))
        assertTrue(rows >= 1)
    }
}
