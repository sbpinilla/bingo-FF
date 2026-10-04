package com.sergiodev.bingo.presentation

import com.sergiodev.bingo.domain.model.BingoLetter
import kotlin.test.Test
import kotlin.test.assertEquals

/** Port of the Android `CreateBoardFocusOrderTest` (the mapping is Compose free). */
class CreateBoardFocusOrderTest {
    private val numbers = CreateBoardState.defaultNumbers()

    @Test
    fun flatFieldIndex_returnsExpectedFlatPositionForEveryField() {
        // B 0-4, I 5-9, N 10-13 (FREE is not a field), G 14-18, O 19-23.
        val expected = mapOf(
            BingoLetter.B to listOf(0, 1, 2, 3, 4),
            BingoLetter.I to listOf(5, 6, 7, 8, 9),
            BingoLetter.N to listOf(10, 11, 12, 13),
            BingoLetter.G to listOf(14, 15, 16, 17, 18),
            BingoLetter.O to listOf(19, 20, 21, 22, 23),
        )
        expected.forEach { (letter, flats) ->
            flats.forEachIndexed { index, flat ->
                assertEquals(flat, flatFieldIndex(numbers, letter, index), "$letter[$index]")
            }
        }
    }

    @Test
    fun flatFieldIndex_boundaries() {
        assertEquals(10, flatFieldIndex(numbers, BingoLetter.N, 0))
        assertEquals(13, flatFieldIndex(numbers, BingoLetter.N, 3))
        assertEquals(23, flatFieldIndex(numbers, BingoLetter.O, 4))
    }
}
