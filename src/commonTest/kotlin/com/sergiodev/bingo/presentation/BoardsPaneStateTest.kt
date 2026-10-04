package com.sergiodev.bingo.presentation

import com.sergiodev.bingo.domain.model.BingoLetter
import com.sergiodev.bingo.domain.model.BoardCard
import com.sergiodev.bingo.domain.model.GameMode
import com.sergiodev.bingo.domain.repository.ActiveGame
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BoardsPaneStateTest {

    // Column-major: B1-5, I1-5, N1,N2,N4,N5, G1-5, O1-5
    private val board1 = BoardCard(
        id = 1L,
        identifier = "Casa1",
        numbers = listOf(3, 7, 12, 14, 15, 16, 17, 18, 19, 20, 31, 32, 34, 35, 46, 47, 48, 49, 50, 61, 62, 63, 64, 65),
    )
    private val board2 = BoardCard(
        id = 2L,
        identifier = "Casa2",
        numbers = listOf(1, 2, 4, 5, 6, 21, 22, 23, 24, 25, 33, 36, 37, 38, 51, 52, 53, 54, 55, 66, 67, 68, 69, 70),
    )

    private class Fixture(
        val boards: FakeBoardRepository,
        val session: GameSessionHolder,
        val pane: BoardsPaneState,
    ) {
        fun card(id: Long) = pane.cards.value.single { it.id == id }
    }

    private fun TestScope.fixture(
        seed: List<BoardCard> = listOf(board1, board2),
        repo: InMemoryActiveGameRepository = InMemoryActiveGameRepository(),
        start: GameMode? = GameMode.COLUMNA,
    ): Fixture {
        val boards = FakeBoardRepository(seed)
        val session = GameSessionHolder(boards, repo, backgroundScope)
        runCurrent()
        if (start != null && session.active.value == null) session.start(start)
        val pane = BoardsPaneState(boards, session, backgroundScope)
        runCurrent()
        return Fixture(boards, session, pane)
    }

    private fun TestScope.call(f: Fixture, vararg numbers: Int) {
        numbers.forEach { f.session.call(it) }
        runCurrent()
    }

    @Test
    fun noGame_noMarksAndNoWinners() = runTest {
        val f = fixture(start = null)

        assertEquals(listOf(1L, 2L), f.pane.cards.value.map { it.id })
        assertTrue(f.pane.cards.value.all { card -> card.cells.none { it.marked } && card.winningPatternIds.isEmpty() })
        assertTrue(f.pane.cards.value.none { it.isWinner })
    }

    @Test
    fun cards_areRowMajorWithFreeCentreAndBoardNumbers() = runTest {
        val f = fixture(start = null)

        val cells = f.card(1).cells
        assertEquals(25, cells.size)
        // Row 1 reads B1, I1, N1, G1, O1; the centre (index 12) is FREE.
        assertEquals(listOf(3, 16, 31, 46, 61), cells.take(5).map { it.number })
        assertEquals(null, cells[12].number)
        assertEquals(listOf(15, 20, 35, 50, 65), cells.takeLast(5).map { it.number })
        assertEquals("Casa1", f.card(1).identifier)
    }

    @Test
    fun marks_areTheCalledNumbersThatAreOnTheBoard() = runTest {
        val f = fixture()

        call(f, 47, 3, 52)

        val marked1 = f.card(1).cells.filter { it.marked }.map { it.number }.toSet()
        assertEquals(setOf(47, 3), marked1)
        val marked2 = f.card(2).cells.filter { it.marked }.map { it.number }.toSet()
        assertEquals(setOf(52), marked2)
    }

    @Test
    fun freeCell_isNeverMarkedAsACall() = runTest {
        val f = fixture()

        call(f, 31, 32, 34, 35)

        assertFalse(f.card(1).cells[12].marked)
    }

    @Test
    fun calledNumberNotOnAnyBoard_marksNothing() = runTest {
        val f = fixture(seed = listOf(board1))

        call(f, 70)

        assertTrue(f.card(1).cells.none { it.marked })
    }

    @Test
    fun winnerPatternIds_listTheCompletedPatternsOfThatBoardOnly() = runTest {
        val f = fixture()

        call(f, 3, 7, 12, 14)
        assertTrue(f.card(1).winningPatternIds.isEmpty())
        call(f, 15)

        assertEquals(listOf("COLUMN_B"), f.card(1).winningPatternIds)
        assertTrue(f.card(1).isWinner)
        assertFalse(f.card(2).isWinner)
    }

    @Test
    fun winningBoard_staysAWinnerAfterLaterCalls() = runTest {
        val f = fixture()
        call(f, 3, 7, 12, 14, 15)

        call(f, 47, 20)

        assertEquals(listOf("COLUMN_B"), f.card(1).winningPatternIds)
    }

    @Test
    fun dismissedLetter_stillHighlightsTheWinner() = runTest {
        val f = fixture()
        f.session.toggleDismiss(BingoLetter.B)

        call(f, 3, 7, 12, 14, 15)

        assertEquals(listOf("COLUMN_B"), f.card(1).winningPatternIds)
    }

    @Test
    fun ending_theGame_clearsMarksAndWinners() = runTest {
        val f = fixture()
        call(f, 3, 7, 12, 14, 15)

        f.session.end()
        runCurrent()

        assertTrue(f.card(1).cells.none { it.marked })
        assertFalse(f.card(1).isWinner)
    }

    @Test
    fun restoredGame_marksAndWinnersAreRebuilt() = runTest {
        val repo = InMemoryActiveGameRepository(
            ActiveGame(GameMode.COLUMNA, listOf(3, 7, 12, 14, 15, 47), emptySet()),
        )

        val f = fixture(repo = repo, start = null)

        assertEquals(listOf("COLUMN_B"), f.card(1).winningPatternIds)
        assertEquals(6, f.card(1).cells.count { it.marked })
    }

    @Test
    fun boardAddedOrDeleted_cardsFollow() = runTest {
        val f = fixture(seed = listOf(board1))
        call(f, 1)

        f.boards.boards.value = listOf(board1, board2)
        runCurrent()
        assertEquals(listOf(1L, 2L), f.pane.cards.value.map { it.id })
        assertTrue(f.card(2).cells.any { it.marked })

        f.boards.deleteBoard(1L)
        runCurrent()
        assertEquals(listOf(2L), f.pane.cards.value.map { it.id })
    }

    @Test
    fun otherModes_useTheirOwnPatterns() = runTest {
        val f = fixture(seed = listOf(board1), start = GameMode.L)

        call(f, 3, 7, 12, 14, 15, 20, 35, 50, 65)

        assertEquals(listOf("L"), f.card(1).winningPatternIds)
    }

    @Test
    fun unaffectedCards_areStructurallyEqualAfterACallElsewhere() = runTest {
        val f = fixture()
        call(f, 47)
        val before2 = f.card(2)

        call(f, 3)

        assertEquals(before2, f.card(2))
        assertEquals(2, f.card(1).cells.count { it.marked })
    }
}
