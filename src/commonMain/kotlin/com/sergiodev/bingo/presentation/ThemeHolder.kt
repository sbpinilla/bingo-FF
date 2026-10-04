package com.sergiodev.bingo.presentation

import com.sergiodev.bingo.domain.repository.ThemeMode
import com.sergiodev.bingo.domain.repository.ThemeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Whether this mode renders dark, given the OS preference (only [ThemeMode.SYSTEM] looks at it). */
fun ThemeMode.isDark(systemDark: Boolean): Boolean = when (this) {
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
    ThemeMode.SYSTEM -> systemDark
}

/** Current theme mode: follows the repository, applies a change at once and persists it. */
class ThemeHolder(private val repository: ThemeRepository, private val scope: CoroutineScope) {
    private val _mode = MutableStateFlow(ThemeMode.SYSTEM)
    val mode: StateFlow<ThemeMode> = _mode

    init {
        scope.launch { repository.themeMode.collect { _mode.value = it } }
    }

    fun set(mode: ThemeMode) {
        _mode.value = mode
        scope.launch { repository.set(mode) }
    }
}
