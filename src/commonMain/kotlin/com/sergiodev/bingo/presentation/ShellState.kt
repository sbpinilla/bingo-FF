package com.sergiodev.bingo.presentation

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

/** Right-pane navigation, derived from whether a game is active so there is no separate state to desync. */
class ShellState(session: GameSessionHolder, scope: CoroutineScope) {
    val destination: StateFlow<RightPaneDestination> = session.active
        .map { if (it != null) RightPaneDestination.Play else RightPaneDestination.Setup }
        .stateIn(scope, SharingStarted.Eagerly, RightPaneDestination.Setup)
}
