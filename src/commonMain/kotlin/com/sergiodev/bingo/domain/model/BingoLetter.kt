package com.sergiodev.bingo.domain.model

/**
 * The five bingo columns, each covering a fixed range of the 1-75 number space.
 */
enum class BingoLetter(val range: IntRange) {
    B(1..15),
    I(16..30),
    N(31..45),
    G(46..60),
    O(61..75),
    ;

    companion object {
        /**
         * Returns the [BingoLetter] whose range contains [number], or `null` when
         * [number] falls outside 1..75.
         */
        fun fromNumber(number: Int): BingoLetter? = entries.firstOrNull { number in it.range }
    }
}
