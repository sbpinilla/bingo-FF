package com.sergiodev.bingo.presentation

import com.sergiodev.bingo.domain.model.BoardCard
import com.sergiodev.bingo.domain.model.GameMode
import com.sergiodev.bingo.domain.repository.ActiveGame
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ShellStateTest {

    private val board = BoardCard(1L, "Casa1", List(24) { it + 1 })

    private fun TestScope.shell(repo: InMemoryActiveGameRepository): Pair<GameSessionHolder, ShellState> {
        val session = GameSessionHolder(FakeBoardRepository(listOf(board)), repo, backgroundScope)
        val shell = ShellState(session, backgroundScope)
        runCurrent()
        return session to shell
    }

    @Test
    fun clean_launch_opens_setup() = runTest {
        val (_, shell) = shell(InMemoryActiveGameRepository())

        assertEquals(RightPaneDestination.Setup, shell.destination.value)
    }

    @Test
    fun restored_game_opens_play() = runTest {
        val repo = InMemoryActiveGameRepository(ActiveGame(GameMode.L, listOf(7), emptySet()))
        val (_, shell) = shell(repo)

        assertEquals(RightPaneDestination.Play, shell.destination.value)
    }

    @Test
    fun start_moves_to_play_and_end_returns_to_setup() = runTest {
        val (session, shell) = shell(InMemoryActiveGameRepository())

        session.start(GameMode.COLUMNA)
        runCurrent()
        assertEquals(RightPaneDestination.Play, shell.destination.value)

        session.end()
        runCurrent()
        assertEquals(RightPaneDestination.Setup, shell.destination.value)
    }
}

class RightPaneDestinationTest {
    @Test
    fun no_active_game_is_setup() {
        assertEquals(RightPaneDestination.Setup, rightPaneDestination(null))
    }

    @Test
    fun active_game_is_play() {
        assertEquals(RightPaneDestination.Play, rightPaneDestination(ActiveGame(GameMode.L, emptyList(), emptySet())))
    }
}
