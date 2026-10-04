package com.sergiodev.bingo.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BingoLetterTest {

    private val cases: List<Pair<Int, BingoLetter?>> = listOf(
        0 to null,
        1 to BingoLetter.B,
        15 to BingoLetter.B,
        16 to BingoLetter.I,
        30 to BingoLetter.I,
        31 to BingoLetter.N,
        45 to BingoLetter.N,
        46 to BingoLetter.G,
        60 to BingoLetter.G,
        61 to BingoLetter.O,
        75 to BingoLetter.O,
        76 to null,
    )

    @Test
    fun fromNumber_returnsExpectedLetter() {
        cases.forEach { (number, expected) ->
            assertEquals(expected, BingoLetter.fromNumber(number), "fromNumber($number)")
        }
    }

    @Test
    fun fromNumber_negativeIsNull() {
        assertNull(BingoLetter.fromNumber(-1))
    }
}
