package com.sergiodev.bingo.presentation

import com.sergiodev.bingo.domain.model.BingoLetter

/** Flat position (0..23) of a create-board field in reading order B, I, N (4 fields), G, O. */
fun flatFieldIndex(numbers: Map<BingoLetter, List<String>>, letter: BingoLetter, index: Int): Int =
    BingoLetter.entries.take(letter.ordinal).sumOf { numbers.getValue(it).size } + index
