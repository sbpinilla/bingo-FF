package com.sergiodev.bingo.presentation

import com.sergiodev.bingo.domain.game.PredictionCandidate
import com.sergiodev.bingo.domain.model.BingoLetter
import com.sergiodev.bingo.domain.model.BoardCard
import com.sergiodev.bingo.domain.model.GameMode
import com.sergiodev.bingo.domain.repository.ActiveGame
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PlayPaneStateTest {

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

    private class Fixture(val boards: FakeBoardRepository, val session: GameSessionHolder, val pane: PlayPaneState) {
        val winners: List<PredictionCandidate> get() = pane.possibleWinners.value
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
        val pane = PlayPaneState(boards, session, backgroundScope)
        runCurrent()
        return Fixture(boards, session, pane)
    }

    private fun TestScope.call(f: Fixture, vararg numbers: Int) {
        numbers.forEach { f.session.call(it) }
        runCurrent()
    }

    @Test
    fun noGame_noCandidates() = runTest {
        val f = fixture(start = null)

        assertEquals(emptyList(), f.winners)
    }

    @Test
    fun candidates_sortByMissingThenBoardThenLetterOrdinal() = runTest {
        val f = fixture()

        // board2 B missing 1; board1 B missing 2; board1 G missing 2.
        call(f, 1, 2, 4, 5, 3, 7, 12, 46, 47, 48)

        assertEquals(
            listOf(
                Triple(2L, BingoLetter.B, 1),
                Triple(1L, BingoLetter.B, 2),
                Triple(1L, BingoLetter.G, 2),
            ),
            f.winners.map { Triple(it.boardId, it.letter, it.missing) },
        )
    }

    @Test
    fun list_updatesLiveOnEachCall() = runTest {
        val f = fixture(seed = listOf(board1))
        call(f, 3, 7)
        assertEquals(emptyList(), f.winners)

        call(f, 12)
        assertEquals(listOf(2), f.winners.map { it.missing })

        call(f, 14)
        assertEquals(listOf(1), f.winners.map { it.missing })
    }

    @Test
    fun list_dropsAWinnerOnceAnnounced() = runTest {
        val f = fixture(seed = listOf(board1))
        call(f, 3, 7, 12, 14)
        assertEquals(listOf(1), f.winners.map { it.missing })

        call(f, 15)

        assertEquals(emptyList(), f.winners)
    }

    @Test
    fun list_dismissedColumnaLetterIsFiltered() = runTest {
        val f = fixture(seed = listOf(board1))
        call(f, 3, 7, 12, 46, 47, 48)
        assertEquals(listOf(BingoLetter.B, BingoLetter.G), f.winners.map { it.letter })

        f.session.toggleDismiss(BingoLetter.G)
        runCurrent()
        assertEquals(listOf(BingoLetter.B), f.winners.map { it.letter })

        f.session.toggleDismiss(BingoLetter.G)
        runCurrent()
        assertEquals(listOf(BingoLetter.B, BingoLetter.G), f.winners.map { it.letter })
    }

    @Test
    fun list_nonColumnaCandidatesHaveNoLetterAndAreNeverFiltered() = runTest {
        val f = fixture(seed = listOf(board1), start = GameMode.I)

        // Rows 1 and 5 called; N2 and N4 are still missing.
        call(f, 3, 15, 16, 20, 31, 35, 46, 50, 61, 65)

        assertEquals(listOf(Triple<Long, BingoLetter?, Int>(1L, null, 2)), f.winners.map { Triple(it.boardId, it.letter, it.missing) })
    }

    @Test
    fun list_fullCardUsesItsTenCellThreshold() = runTest {
        val f = fixture(seed = listOf(board1), start = GameMode.CARTON_COMPLETO)
        // 24 numbers + FREE = 25 cells; 14 called leaves 10 missing.
        call(f, 3, 7, 12, 14, 15, 16, 17, 18, 19, 20, 31, 32, 34, 35)

        assertEquals(listOf(10), f.winners.map { it.missing })
    }

    @Test
    fun restoredGame_listIsRebuilt() = runTest {
        val repo = InMemoryActiveGameRepository(
            ActiveGame(GameMode.COLUMNA, listOf(3, 7, 12), setOf(BingoLetter.I)),
        )

        val f = fixture(repo = repo, start = null)

        assertEquals(listOf(Triple(1L, BingoLetter.B, 2)), f.winners.map { Triple(it.boardId, it.letter, it.missing) })
    }

    @Test
    fun restoredDismissedLetter_isAlreadyFiltered() = runTest {
        val repo = InMemoryActiveGameRepository(
            ActiveGame(GameMode.COLUMNA, listOf(3, 7, 12), setOf(BingoLetter.B)),
        )

        val f = fixture(repo = repo, start = null)

        assertEquals(emptyList(), f.winners)
    }

    @Test
    fun ending_theGame_clearsTheList() = runTest {
        val f = fixture()
        call(f, 3, 7, 12)

        f.session.end()
        runCurrent()

        assertEquals(emptyList(), f.winners)
    }
}
