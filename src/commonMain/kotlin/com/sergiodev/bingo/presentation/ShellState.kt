package com.sergiodev.bingo.presentation

import com.sergiodev.bingo.domain.repository.ActiveGame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** What the right pane shows. */
sealed interface RightPaneDestination {
    data object Setup : RightPaneDestination
    data object Play : RightPaneDestination
}

/** The single rule behind right-pane navigation: a game in progress means Play, otherwise Setup. */
fun rightPaneDestination(active: ActiveGame?): RightPaneDestination =
    if (active != null) RightPaneDestination.Play else RightPaneDestination.Setup

/** Right-pane navigation, derived from whether a game is active so there is no separate state to desync. */
class ShellState(session: GameSessionHolder, scope: CoroutineScope) {
    val destination: StateFlow<RightPaneDestination> = session.active
        .map(::rightPaneDestination)
        .stateIn(scope, SharingStarted.Eagerly, RightPaneDestination.Setup)
}
