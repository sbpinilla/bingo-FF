package com.sergiodev.bingo.presentation

import com.sergiodev.bingo.domain.model.BoardCard
import com.sergiodev.bingo.domain.model.GameMode
import com.sergiodev.bingo.domain.repository.ActiveGame
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RightPaneDestinationTest {

    private val board = BoardCard(1L, "Casa1", List(24) { it + 1 })

    private fun TestScope.session(repo: InMemoryActiveGameRepository): GameSessionHolder {
        val session = GameSessionHolder(FakeBoardRepository(listOf(board)), repo, backgroundScope)
        runCurrent()
        return session
    }

    /** Mirrors how the right pane derives its destination from the session. */
    private fun GameSessionHolder.destination() = rightPaneDestination(active.value)

    @Test
    fun no_active_game_is_setup() {
        assertEquals(RightPaneDestination.Setup, rightPaneDestination(null))
    }

    @Test
    fun active_game_is_play() {
        assertEquals(RightPaneDestination.Play, rightPaneDestination(ActiveGame(GameMode.L, emptyList(), emptySet())))
    }

    @Test
    fun clean_launch_opens_setup() = runTest {
        val session = session(InMemoryActiveGameRepository())

        assertEquals(RightPaneDestination.Setup, session.destination())
    }

    @Test
    fun restored_game_opens_play() = runTest {
        val session = session(InMemoryActiveGameRepository(ActiveGame(GameMode.L, listOf(7), emptySet())))

        assertEquals(RightPaneDestination.Play, session.destination())
    }

    @Test
    fun start_moves_to_play_and_end_returns_to_setup() = runTest {
        val session = session(InMemoryActiveGameRepository())

        session.start(GameMode.COLUMNA)
        runCurrent()
        assertEquals(RightPaneDestination.Play, session.destination())

        session.end()
        runCurrent()
        assertEquals(RightPaneDestination.Setup, session.destination())
    }
}
