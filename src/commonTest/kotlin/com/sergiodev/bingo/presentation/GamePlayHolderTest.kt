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
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Rewrite of the Android `GamePlayViewModelTest` (SavedStateHandle) against the persisted holder. */
class GamePlayHolderTest {

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

    private class Fixture(val session: GameSessionHolder, val play: GamePlayHolder, val repo: InMemoryActiveGameRepository)

    private fun TestScope.fixture(
        boards: List<BoardCard> = listOf(board1),
        repo: InMemoryActiveGameRepository = InMemoryActiveGameRepository(),
        mode: GameMode? = GameMode.COLUMNA,
    ): Fixture {
        val session = GameSessionHolder(FakeBoardRepository(boards), repo, backgroundScope)
        runCurrent()
        if (mode != null && session.active.value == null) session.start(mode)
        val play = GamePlayHolder(session, backgroundScope)
        runCurrent()
        return Fixture(session, play, repo)
    }

    private fun TestScope.submit(f: Fixture, text: String) {
        f.play.onNumberInputChanged(text)
        f.play.onSubmitCall()
        runCurrent()
    }

    // --- number input and letter derivation ---

    @Test
    fun typingNumber_derivesLetter() = runTest {
        val f = fixture()

        f.play.onNumberInputChanged("47")
        runCurrent()

        assertEquals(BingoLetter.G, f.play.state.value.selectedLetter)
        assertFalse(f.play.state.value.letterOverridden)
    }

    @Test
    fun typingNumber_rederivesAsTheInputChanges_andClearsOutOfRange() = runTest {
        val f = fixture()

        f.play.onNumberInputChanged("4")
        runCurrent()
        assertEquals(BingoLetter.B, f.play.state.value.selectedLetter)

        f.play.onNumberInputChanged("42")
        runCurrent()
        assertEquals(BingoLetter.N, f.play.state.value.selectedLetter)

        f.play.onNumberInputChanged("80")
        runCurrent()
        assertNull(f.play.state.value.selectedLetter)
    }

    @Test
    fun overriddenLetter_isKeptWhileTyping() = runTest {
        val f = fixture()

        f.play.onLetterSelected(BingoLetter.B)
        f.play.onNumberInputChanged("47")
        runCurrent()

        assertEquals(BingoLetter.B, f.play.state.value.selectedLetter)
        assertTrue(f.play.state.value.letterOverridden)
    }

    // --- submit outcomes ---

    @Test
    fun accept_appendsPersistsAndClearsInput() = runTest {
        val f = fixture()

        submit(f, "47")

        val state = f.play.state.value
        assertNull(state.inputError)
        assertEquals("", state.numberInput)
        assertNull(state.selectedLetter)
        assertEquals(listOf(47), f.session.active.value?.calledNumbers)
        assertEquals(listOf(47), f.repo.stored?.calledNumbers)
    }

    @Test
    fun accept_overriddenLetterMatchingTheRange() = runTest {
        val f = fixture()

        f.play.onLetterSelected(BingoLetter.B)
        submit(f, "7")

        assertNull(f.play.state.value.inputError)
        assertEquals(listOf(7), f.play.state.value.callsByLetter[BingoLetter.B])
        assertFalse(f.play.state.value.letterOverridden)
    }

    @Test
    fun invalidNumber_80_exposesReasonAndAddsNoCall() = runTest {
        val f = fixture()

        submit(f, "80")

        assertEquals(GamePlayInputError.InvalidNumber, f.play.state.value.inputError)
        assertEquals("80", f.play.state.value.numberInput)
        assertEquals(0, f.play.state.value.calledCount)
        assertEquals(1, f.repo.saveCalls) // only the start() save
    }

    @Test
    fun invalidNumber_blankZeroAndNonNumeric() = runTest {
        val f = fixture()

        listOf("", "0", "abc", "76").forEach { text ->
            submit(f, text)
            assertEquals(GamePlayInputError.InvalidNumber, f.play.state.value.inputError, "input '$text'")
        }
        assertEquals(0, f.play.state.value.calledCount)
    }

    @Test
    fun letterMismatch_exposesReasonAndAddsNoCall() = runTest {
        val f = fixture()

        f.play.onLetterSelected(BingoLetter.B)
        submit(f, "47")

        assertEquals(GamePlayInputError.LetterMismatch, f.play.state.value.inputError)
        assertEquals(0, f.play.state.value.calledCount)
    }

    @Test
    fun duplicateCall_exposesNumberClearsInputAndAddsNoCall() = runTest {
        val f = fixture()
        submit(f, "47")

        submit(f, "47")

        val state = f.play.state.value
        assertEquals(GamePlayInputError.DuplicateCall(47), state.inputError)
        assertEquals("", state.numberInput)
        assertEquals(1, state.calledCount)
    }

    @Test
    fun editingInput_clearsTheError() = runTest {
        val f = fixture()
        submit(f, "80")

        f.play.onNumberInputChanged("8")
        runCurrent()

        assertNull(f.play.state.value.inputError)
    }

    @Test
    fun selectingALetter_clearsTheError() = runTest {
        val f = fixture()
        submit(f, "80")

        f.play.onLetterSelected(BingoLetter.O)
        runCurrent()

        assertNull(f.play.state.value.inputError)
    }

    @Test
    fun submitWithoutActiveGame_isIgnored() = runTest {
        val f = fixture(mode = null)

        submit(f, "47")

        assertNull(f.session.active.value)
        assertEquals(0, f.repo.saveCalls)
        assertEquals(0, f.play.state.value.calledCount)
    }

    @Test
    fun endingTheGame_resetsThePendingEntry() = runTest {
        val f = fixture()
        f.play.onLetterSelected(BingoLetter.B)
        f.play.onNumberInputChanged("12")
        runCurrent()

        f.session.end()
        runCurrent()

        assertEquals(GamePlayUiState(), f.play.state.value)
        f.session.start(GameMode.L)
        runCurrent()
        assertEquals("", f.play.state.value.numberInput)
        assertFalse(f.play.state.value.letterOverridden)
    }

    // --- called grid ---

    @Test
    fun grouping_47_3_52_listsPerLetterInCallOrder() = runTest {
        val f = fixture()

        listOf("47", "3", "52").forEach { submit(f, it) }

        val state = f.play.state.value
        assertEquals(listOf(47, 52), state.callsByLetter[BingoLetter.G])
        assertEquals(listOf(3), state.callsByLetter[BingoLetter.B])
        assertEquals(3, state.calledCount)
    }

    @Test
    fun callsByLetter_hasFiveRowsInLetterOrder_evenWhenEmpty() = runTest {
        val f = fixture()

        assertEquals(BingoLetter.entries.toList(), f.play.state.value.callsByLetter.keys.toList())
        assertTrue(f.play.state.value.callsByLetter.values.all { it.isEmpty() })

        submit(f, "70")

        assertEquals(BingoLetter.entries.toList(), f.play.state.value.callsByLetter.keys.toList())
        assertEquals(listOf(70), f.play.state.value.callsByLetter[BingoLetter.O])
    }

    @Test
    fun state_carriesModeForTheDismissAffordance() = runTest {
        val f = fixture(mode = GameMode.L)

        assertEquals(GameMode.L, f.play.state.value.mode)
    }

    // --- win announcements ---

    @Test
    fun newWin_announcedWithBoardIdAsSequentialNumber() = runTest {
        val f = fixture()

        listOf("3", "7", "12", "14").forEach { submit(f, it) }
        assertTrue(f.play.state.value.winners.isEmpty())
        submit(f, "15")

        val winner = f.play.state.value.winners.single()
        assertEquals(1L, winner.boardId)
        assertEquals(1L, winner.sequentialNumber)
        assertEquals("Casa1", winner.identifier)
        assertEquals("COLUMN_B", winner.patternId)
    }

    @Test
    fun win_isNotReannouncedByLaterCalls_andSecondBoardIsAnnouncedOnItsOwn() = runTest {
        val f = fixture(boards = listOf(board1, board2))

        listOf("3", "7", "12", "14", "15").forEach { submit(f, it) }
        listOf("47", "20").forEach { submit(f, it) }
        assertEquals(1, f.play.state.value.winners.size)

        listOf("1", "2", "4", "5", "6").forEach { submit(f, it) }

        val winners = f.play.state.value.winners
        assertEquals(1, winners.count { it.boardId == 1L && it.patternId == "COLUMN_B" })
        assertEquals(1, winners.count { it.boardId == 2L && it.patternId == "COLUMN_B" })
        assertEquals(2, winners.size)
    }

    @Test
    fun restart_replaysTheSameWinnersAndCallsFromTheStoredGame() = runTest {
        val repo = InMemoryActiveGameRepository()
        val first = fixture(repo = repo)
        listOf("3", "7", "12", "14", "15", "47").forEach { submit(first, it) }
        val before = first.play.state.value

        val second = fixture(repo = repo, mode = null)

        val after = second.play.state.value
        assertEquals(before.winners, after.winners)
        assertEquals(before.callsByLetter, after.callsByLetter)
        assertEquals(6, after.calledCount)
        assertEquals(GameMode.COLUMNA, after.mode)
    }

    // --- COLUMNA dismissal ---

    @Test
    fun toggleTwice_dismissesThenRestoresAndPersistsBoth() = runTest {
        val f = fixture()

        f.play.onLetterDismissToggled(BingoLetter.G)
        runCurrent()
        assertEquals(setOf(BingoLetter.G), f.play.state.value.dismissedLetters)
        assertEquals(setOf(BingoLetter.G), f.repo.stored?.dismissedLetters)

        f.play.onLetterDismissToggled(BingoLetter.G)
        runCurrent()
        assertTrue(f.play.state.value.dismissedLetters.isEmpty())
        assertTrue(f.repo.stored?.dismissedLetters?.isEmpty() == true)
    }

    @Test
    fun dismissedLetters_surviveRestartAlongsideCalls() = runTest {
        val repo = InMemoryActiveGameRepository()
        val first = fixture(repo = repo)
        submit(first, "47")
        first.play.onLetterDismissToggled(BingoLetter.B)
        first.play.onLetterDismissToggled(BingoLetter.O)
        runCurrent()

        val second = fixture(repo = repo, mode = null)

        assertEquals(setOf(BingoLetter.B, BingoLetter.O), second.play.state.value.dismissedLetters)
        assertEquals(listOf(47), second.session.active.value?.calledNumbers)
    }

    @Test
    fun dismissedLetter_completingThatColumnAnnouncesNothing() = runTest {
        val f = fixture()
        f.play.onLetterDismissToggled(BingoLetter.B)
        runCurrent()

        listOf("3", "7", "12", "14", "15").forEach { submit(f, it) }

        assertTrue(f.play.state.value.winners.isEmpty())
        assertEquals(setOf(BingoLetter.B), f.play.state.value.dismissedLetters)
    }

    @Test
    fun dismissingAnAnnouncedColumn_removesItsAnnouncement_andReopeningRestoresIt() = runTest {
        val f = fixture()
        listOf("3", "7", "12", "14", "15").forEach { submit(f, it) }
        val announced = f.play.state.value.winners
        assertEquals("COLUMN_B", announced.single().patternId)

        f.play.onLetterDismissToggled(BingoLetter.B)
        runCurrent()
        assertTrue(f.play.state.value.winners.isEmpty())

        f.play.onLetterDismissToggled(BingoLetter.B)
        runCurrent()
        assertEquals(announced, f.play.state.value.winners)
    }

    @Test
    fun dismissingOneColumn_keepsTheOtherAnnouncementsInTheirOrder() = runTest {
        val f = fixture(boards = listOf(board1, board2))
        // board2 B, then board1 G, then board1 B.
        listOf("1", "2", "4", "5", "6", "46", "47", "48", "49", "50", "3", "7", "12", "14", "15").forEach { submit(f, it) }
        assertEquals(
            listOf(2L to "COLUMN_B", 1L to "COLUMN_G", 1L to "COLUMN_B"),
            f.play.state.value.winners.map { it.boardId to it.patternId },
        )

        f.play.onLetterDismissToggled(BingoLetter.B)
        runCurrent()

        assertEquals(listOf(1L to "COLUMN_G"), f.play.state.value.winners.map { it.boardId to it.patternId })
    }

    @Test
    fun restart_withADismissedLetter_replaysWithoutThatColumnWin() = runTest {
        val repo = InMemoryActiveGameRepository(
            ActiveGame(GameMode.COLUMNA, listOf(3, 7, 12, 14, 15, 46, 47, 48, 49, 50), setOf(BingoLetter.B)),
        )

        val f = fixture(repo = repo, mode = null)

        assertEquals(listOf("COLUMN_G"), f.play.state.value.winners.map { it.patternId })
    }

    @Test
    fun restoredNonColumnaGame_ignoresDismissedLettersForWins() = runTest {
        val repo = InMemoryActiveGameRepository(
            ActiveGame(GameMode.L, listOf(3, 7, 12, 14, 15, 20, 35, 50, 65), setOf(BingoLetter.B)),
        )

        val f = fixture(repo = repo, mode = null)

        assertEquals(listOf("L"), f.play.state.value.winners.map { it.patternId })
    }

    @Test
    fun toggleDismiss_isIgnoredOutsideColumna() = runTest {
        val f = fixture(mode = GameMode.L)

        f.play.onLetterDismissToggled(BingoLetter.B)
        runCurrent()

        assertTrue(f.play.state.value.dismissedLetters.isEmpty())
        assertTrue(f.repo.stored?.dismissedLetters?.isEmpty() == true)
    }

    @Test
    fun toggleDismiss_withoutActiveGame_isIgnored() = runTest {
        val f = fixture(mode = null)

        f.play.onLetterDismissToggled(BingoLetter.B)
        runCurrent()

        assertNull(f.session.active.value)
        assertEquals(0, f.repo.saveCalls)
    }

    // --- GameSessionHolder.call / toggleDismiss (deferred from unit 5) ---

    @Test
    fun sessionCall_returnsWhetherTheNumberWasAppended() = runTest {
        val f = fixture()

        assertTrue(f.session.call(47))
        assertFalse(f.session.call(47))
        runCurrent()

        assertEquals(listOf(47), f.repo.stored?.calledNumbers)
    }

    @Test
    fun sessionCall_keepsDismissedLettersAndOrder() = runTest {
        val repo = InMemoryActiveGameRepository()
        val f = fixture(repo = repo)
        f.session.toggleDismiss(BingoLetter.N)
        f.session.call(52)
        f.session.call(3)
        runCurrent()

        assertEquals(ActiveGame(GameMode.COLUMNA, listOf(52, 3), setOf(BingoLetter.N)), repo.stored)
    }
}
