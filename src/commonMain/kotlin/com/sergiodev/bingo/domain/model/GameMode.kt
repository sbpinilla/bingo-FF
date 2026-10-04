package com.sergiodev.bingo.domain.model

/**
 * The five supported win modes. Each exposes its coordinate-based [patterns];
 * a pattern wins when every one of its cells is satisfied (FREE or called)
 * for a given board.
 */
enum class GameMode {
    COLUMNA,
    O,
    L,
    I,
    CARTON_COMPLETO,
    ;

    val patterns: List<WinPattern>
        get() = when (this) {
            COLUMNA -> BingoLetter.entries.map { letter ->
                WinPattern(
                    id = "COLUMN_${letter.name}",
                    cells = (1..5).map { row -> GridPosition(letter, row) }.toSet(),
                )
            }

            O -> listOf(
                WinPattern(
                    id = "O",
                    cells = buildSet {
                        BingoLetter.entries.forEach { letter ->
                            add(GridPosition(letter, 1))
                            add(GridPosition(letter, 5))
                        }
                        (2..4).forEach { row ->
                            add(GridPosition(BingoLetter.B, row))
                            add(GridPosition(BingoLetter.O, row))
                        }
                    },
                ),
            )

            L -> listOf(
                WinPattern(
                    id = "L",
                    cells = buildSet {
                        (1..5).forEach { row -> add(GridPosition(BingoLetter.B, row)) }
                        BingoLetter.entries.forEach { letter -> add(GridPosition(letter, 5)) }
                    },
                ),
            )

            I -> listOf(
                WinPattern(
                    id = "I",
                    cells = buildSet {
                        BingoLetter.entries.forEach { letter ->
                            add(GridPosition(letter, 1))
                            add(GridPosition(letter, 5))
                        }
                        (1..5).forEach { row -> add(GridPosition(BingoLetter.N, row)) }
                    },
                ),
            )

            CARTON_COMPLETO -> listOf(
                WinPattern(
                    id = "FULL_CARD",
                    cells = buildSet {
                        BingoLetter.entries.forEach { letter ->
                            (1..5).forEach { row -> add(GridPosition(letter, row)) }
                        }
                    },
                ),
            )
        }
}
