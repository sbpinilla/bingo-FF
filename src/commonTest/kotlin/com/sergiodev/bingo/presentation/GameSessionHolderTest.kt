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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameSessionHolderTest {

    /** Column-major numbers where B holds 1..5, so calling 1..5 completes column B. */
    private val boardB = BoardCard(1L, "Casa1", List(24) { it + 1 })
    private val boardOther = BoardCard(2L, "Casa2", List(24) { it + 31 })

    private fun TestScope.holder(
        boards: FakeBoardRepository = FakeBoardRepository(listOf(boardB)),
        repo: InMemoryActiveGameRepository = InMemoryActiveGameRepository(),
    ): GameSessionHolder {
        val holder = GameSessionHolder(boards, repo, backgroundScope)
        runCurrent()
        return holder
    }

    // --- setup state (rewrite of the Android GameSetupViewModelTest) ---

    @Test
    fun startBlocked_whenZeroBoardsRegistered() = runTest {
        val holder = holder(boards = FakeBoardRepository())

        assertFalse(holder.setup.value.hasBoards)
        assertFalse(holder.setup.value.canStart)
    }

    @Test
    fun startAllowed_withAtLeastOneBoardAndColumnaPreselected() = runTest {
        val boards = FakeBoardRepository()
        val holder = holder(boards = boards)
        assertFalse(holder.setup.value.canStart)

        boards.boards.value = listOf(boardB)
        runCurrent()

        assertTrue(holder.setup.value.canStart)
        assertEquals(GameMode.COLUMNA, holder.setup.value.selectedMode)

        holder.selectMode(GameMode.O)
        runCurrent()

        assertTrue(holder.setup.value.canStart)
        assertEquals(GameMode.O, holder.setup.value.selectedMode)
    }

    @Test
    fun startBlockedAgain_whenTheLastBoardIsDeleted() = runTest {
        val boards = FakeBoardRepository(listOf(boardB))
        val holder = holder(boards = boards)
        assertTrue(holder.setup.value.canStart)

        boards.deleteBoard(boardB.id)
        runCurrent()

        assertFalse(holder.setup.value.canStart)
    }

    @Test
    fun exposesAllFiveModes() = runTest {
        assertEquals(GameMode.entries.toList(), holder().setup.value.availableModes)
    }

    @Test
    fun columnaPreselected_byDefault() = runTest {
        assertEquals(GameMode.COLUMNA, holder().setup.value.selectedMode)
    }

    // --- start / end ---

    @Test
    fun no_game_is_active_on_a_clean_launch() = runTest {
        val holder = holder()

        assertTrue(holder.loaded.value)
        assertNull(holder.active.value)
        assertNull(holder.session.value)
    }

    @Test
    fun start_activates_a_game_with_the_mode_and_no_calls_and_persists_it() = runTest {
        val repo = InMemoryActiveGameRepository()
        val holder = holder(repo = repo)

        holder.start(GameMode.L)
        runCurrent()

        val expected = ActiveGame(GameMode.L, emptyList(), emptySet())
        assertEquals(expected, holder.active.value)
        assertEquals(expected, repo.stored)
    }

    @Test
    fun start_with_another_mode_activates_that_mode() = runTest {
        val holder = holder()

        holder.start(GameMode.CARTON_COMPLETO)
        runCurrent()

        assertEquals(GameMode.CARTON_COMPLETO, holder.active.value?.mode)
    }

    @Test
    fun start_is_ignored_with_no_boards_and_stores_nothing() = runTest {
        val repo = InMemoryActiveGameRepository()
        val holder = holder(boards = FakeBoardRepository(), repo = repo)

        holder.start(GameMode.COLUMNA)
        runCurrent()

        assertNull(holder.active.value)
        assertEquals(0, repo.saveCalls)
        assertNull(repo.stored)
    }

    @Test
    fun start_is_ignored_while_a_game_is_active() = runTest {
        val repo = InMemoryActiveGameRepository()
        val holder = holder(repo = repo)
        holder.start(GameMode.L)
        runCurrent()

        holder.start(GameMode.O)
        runCurrent()

        assertEquals(GameMode.L, holder.active.value?.mode)
        assertEquals(1, repo.saveCalls)
    }

    @Test
    fun end_clears_the_active_game_and_the_stored_one_and_keeps_the_boards() = runTest {
        val boards = FakeBoardRepository(listOf(boardB))
        val repo = InMemoryActiveGameRepository()
        val holder = holder(boards = boards, repo = repo)
        holder.start(GameMode.COLUMNA)
        runCurrent()
        assertNotNull(repo.stored)

        holder.end()
        runCurrent()

        assertNull(holder.active.value)
        assertNull(repo.stored)
        assertEquals(1, repo.clearCalls)
        assertEquals(listOf(boardB), boards.boards.value)
    }

    @Test
    fun a_new_game_can_start_after_the_previous_one_ended() = runTest {
        val holder = holder()
        holder.start(GameMode.L)
        runCurrent()
        holder.end()
        runCurrent()

        holder.start(GameMode.I)
        runCurrent()

        assertEquals(GameMode.I, holder.active.value?.mode)
    }

    // --- restart ---

    @Test
    fun restart_with_a_new_holder_and_the_same_store_restores_the_game() = runTest {
        val repo = InMemoryActiveGameRepository()
        val first = holder(repo = repo)
        first.start(GameMode.O)
        runCurrent()

        val second = holder(repo = repo)

        assertEquals(ActiveGame(GameMode.O, emptyList(), emptySet()), second.active.value)
    }

    @Test
    fun restore_keeps_called_numbers_in_order_and_the_dismissed_letters() = runTest {
        val stored = ActiveGame(GameMode.COLUMNA, listOf(5, 20), setOf(BingoLetter.B))
        val holder = holder(repo = InMemoryActiveGameRepository(stored))

        assertEquals(stored, holder.active.value)
        assertEquals(listOf(5, 20), holder.active.value?.calledNumbers)
    }

    @Test
    fun restore_rebuilds_winners_and_announced_by_replaying_the_calls() = runTest {
        val stored = ActiveGame(GameMode.COLUMNA, listOf(1, 2, 3, 4, 5), emptySet())
        val holder = holder(
            boards = FakeBoardRepository(listOf(boardB, boardOther)),
            repo = InMemoryActiveGameRepository(stored),
        )

        val session = assertNotNull(holder.session.value)
        assertEquals(listOf(1, 2, 3, 4, 5), session.calledNumbers)
        assertEquals(listOf(boardB.id), session.winners.map { it.boardId })
        assertEquals("COLUMN_B", session.winners.single().patternId)
        assertEquals(1, session.announced.size)
    }

    @Test
    fun restore_with_no_winning_calls_has_no_winners() = runTest {
        val stored = ActiveGame(GameMode.COLUMNA, listOf(1, 2), emptySet())
        val holder = holder(repo = InMemoryActiveGameRepository(stored))

        assertTrue(assertNotNull(holder.session.value).winners.isEmpty())
    }

    @Test
    fun an_ended_game_is_not_restored() = runTest {
        val repo = InMemoryActiveGameRepository()
        val first = holder(repo = repo)
        first.start(GameMode.L)
        runCurrent()
        first.end()
        runCurrent()

        val second = holder(repo = repo)

        assertNull(second.active.value)
    }
}
