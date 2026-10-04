package com.sergiodev.bingo.presentation

import com.sergiodev.bingo.domain.model.BoardCard
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BoardListTest {

    private val b1 = BoardCard(1L, "Casa1", List(24) { it + 1 })
    private val b2 = BoardCard(2L, "Casa2", List(24) { it + 30 })

    private fun TestScope.holder(repo: FakeBoardRepository): BoardsState {
        val holder = BoardsState(repo, backgroundScope)
        backgroundScope.launch { holder.state.collect {} }
        runCurrent()
        return holder
    }

    @Test
    fun initial_state_is_an_empty_list_with_no_pending_delete() = runTest {
        val state = holder(FakeBoardRepository()).state.value

        assertTrue(state.boards.isEmpty())
        assertNull(state.pendingDelete)
    }

    @Test
    fun boards_follow_the_repository() = runTest {
        val repo = FakeBoardRepository()
        val holder = holder(repo)

        repo.boards.value = listOf(b1, b2)
        runCurrent()

        assertEquals(listOf(b1, b2), holder.state.value.boards)
    }

    @Test
    fun delete_requested_sets_pending_without_deleting() = runTest {
        val repo = FakeBoardRepository(listOf(b1, b2))
        val holder = holder(repo)

        holder.onDeleteRequested(b1)
        runCurrent()

        assertEquals(b1, holder.state.value.pendingDelete)
        assertTrue(repo.deletedIds.isEmpty())
    }

    @Test
    fun cancel_delete_keeps_the_board() = runTest {
        val repo = FakeBoardRepository(listOf(b1, b2))
        val holder = holder(repo)
        holder.onDeleteRequested(b1)
        runCurrent()

        holder.onDeleteDismissed()
        runCurrent()

        assertNull(holder.state.value.pendingDelete)
        assertTrue(repo.deletedIds.isEmpty())
        assertEquals(listOf(b1, b2), holder.state.value.boards)
    }

    @Test
    fun confirm_delete_removes_the_board_and_clears_pending() = runTest {
        val repo = FakeBoardRepository(listOf(b1, b2))
        val holder = holder(repo)
        holder.onDeleteRequested(b1)
        runCurrent()

        holder.onDeleteConfirmed()
        runCurrent()

        assertEquals(listOf(1L), repo.deletedIds)
        assertNull(holder.state.value.pendingDelete)
        assertEquals(listOf(b2), holder.state.value.boards)
    }

    @Test
    fun confirm_without_a_pending_request_deletes_nothing() = runTest {
        val repo = FakeBoardRepository(listOf(b1))
        val holder = holder(repo)

        holder.onDeleteConfirmed()
        runCurrent()

        assertTrue(repo.deletedIds.isEmpty())
        assertEquals(listOf(b1), holder.state.value.boards)
    }
}
