package com.sergiodev.bingo.presentation

const val MAX_NUMBER_LENGTH = 2

/**
 * Keeps only ASCII digits and caps the result at [MAX_NUMBER_LENGTH] characters.
 *
 * `Char.isDigit` is deliberately avoided: it accepts non-ASCII digits (e.g. Arabic-Indic digits).
 * This is a keystroke sanitizer, not a numeric normalizer, so leading zeros are preserved.
 */
fun sanitizeNumberInput(raw: String): String = raw.filter { it in '0'..'9' }.take(MAX_NUMBER_LENGTH)
