package com.sergiodev.bingo.presentation

import com.sergiodev.bingo.domain.repository.ThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeHolderTest {

    private fun TestScope.holder(repo: InMemoryThemeRepository) =
        ThemeHolder(repo, CoroutineScope(UnconfinedTestDispatcher(testScheduler)))

    @Test
    fun startup_without_a_choice_is_system() = runTest {
        assertEquals(ThemeMode.SYSTEM, holder(InMemoryThemeRepository()).mode.value)
    }

    @Test
    fun startup_restores_the_stored_mode() = runTest {
        assertEquals(ThemeMode.DARK, holder(InMemoryThemeRepository(ThemeMode.DARK)).mode.value)
    }

    @Test
    fun set_updates_the_mode_and_persists() = runTest {
        val repo = InMemoryThemeRepository()
        val holder = holder(repo)

        holder.set(ThemeMode.LIGHT)
        assertEquals(ThemeMode.LIGHT, holder.mode.value)
        holder.set(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, holder.mode.value)
        assertEquals(listOf(ThemeMode.LIGHT, ThemeMode.DARK), repo.writes)
    }

    @Test
    fun a_new_holder_on_the_same_repository_restores_the_mode() = runTest {
        val repo = InMemoryThemeRepository()
        holder(repo).set(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, holder(repo).mode.value)
    }

    @Test
    fun explicit_modes_ignore_the_system_theme() {
        assertTrue(ThemeMode.DARK.isDark(systemDark = false))
        assertFalse(ThemeMode.LIGHT.isDark(systemDark = true))
    }

    @Test
    fun system_mode_follows_the_system_theme() {
        assertTrue(ThemeMode.SYSTEM.isDark(systemDark = true))
        assertFalse(ThemeMode.SYSTEM.isDark(systemDark = false))
    }
}
