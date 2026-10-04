package com.sergiodev.bingo.domain.repository

import kotlinx.coroutines.flow.Flow

/** The 3 supported app-wide appearance modes. Default is [SYSTEM]. */
enum class ThemeMode { LIGHT, DARK, SYSTEM }

/** Port for the user's persisted theme preference. */
interface ThemeRepository {
    val themeMode: Flow<ThemeMode>

    suspend fun set(mode: ThemeMode)
}
