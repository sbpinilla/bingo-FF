package com.sergiodev.bingo.presentation

import com.sergiodev.bingo.domain.repository.ActiveGame

/** What the right pane shows. */
sealed interface RightPaneDestination {
    data object Setup : RightPaneDestination
    data object Play : RightPaneDestination
}

/** The single rule behind right-pane navigation: a game in progress means Play, otherwise Setup. */
fun rightPaneDestination(active: ActiveGame?): RightPaneDestination =
    if (active != null) RightPaneDestination.Play else RightPaneDestination.Setup
