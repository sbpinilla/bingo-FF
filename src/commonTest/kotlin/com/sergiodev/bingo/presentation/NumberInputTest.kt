package com.sergiodev.bingo.presentation

import kotlin.test.Test
import kotlin.test.assertEquals

/** Port of the Android `BingoNumberFieldTest` (the sanitizer is Compose free). */
class NumberInputTest {

    @Test
    fun sanitizeNumberInput_returnsExpectedResult() {
        val cases = listOf(
            "" to "",
            "abc" to "",
            "1a2" to "12",
            "123" to "12",
            "07" to "07",
            "-3" to "3",
            " 7 " to "7",
            "5.5" to "55",
        )
        cases.forEach { (raw, expected) -> assertEquals(expected, sanitizeNumberInput(raw), "sanitize('$raw')") }
    }

    @Test
    fun sanitizeNumberInput_pastedMultiCharacterStringIsFilteredAndTruncated() {
        assertEquals("12", sanitizeNumberInput("a1b2c3d4"))
    }

    @Test
    fun sanitizeNumberInput_isIdempotent() {
        val once = sanitizeNumberInput("1a2b3")
        assertEquals(once, sanitizeNumberInput(once))
    }

    @Test
    fun sanitizeNumberInput_rejectsNonAsciiDigits() {
        assertEquals("", sanitizeNumberInput("٣٤"))
    }
}
