package com.sergiodev.bingo.presentation

import com.sergiodev.bingo.domain.repository.ThemeMode
import com.sergiodev.bingo.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [ThemeRepository] for holder tests; records every write. */
class InMemoryThemeRepository(initial: ThemeMode = ThemeMode.SYSTEM) : ThemeRepository {
    private val state = MutableStateFlow(initial)
    val writes = mutableListOf<ThemeMode>()

    override val themeMode: Flow<ThemeMode> = state

    override suspend fun set(mode: ThemeMode) {
        writes += mode
        state.value = mode
    }
}
