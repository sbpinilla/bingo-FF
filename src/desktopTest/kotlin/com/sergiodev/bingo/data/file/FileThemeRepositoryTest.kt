package com.sergiodev.bingo.data.file

import com.sergiodev.bingo.domain.repository.ThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FileThemeRepositoryTest {

    private val dir: Path = Files.createTempDirectory("bingoff-theme")
    private val file = dir.resolve("theme.json")

    @Test
    fun first_launch_without_a_file_is_system() = runTest {
        assertEquals(ThemeMode.SYSTEM, FileThemeRepository(file).themeMode.first())
    }

    @Test
    fun corrupt_file_falls_back_to_system() = runTest {
        file.writeText("### not json")

        assertEquals(ThemeMode.SYSTEM, FileThemeRepository(file).themeMode.first())
    }

    @Test
    fun unrecognized_stored_value_falls_back_to_system() = runTest {
        file.writeText("""{"version":1,"mode":"SEPIA"}""")

        assertEquals(ThemeMode.SYSTEM, FileThemeRepository(file).themeMode.first())
    }

    @Test
    fun set_is_emitted_and_persisted() = runTest {
        val repo = FileThemeRepository(file)

        repo.set(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, repo.themeMode.first())
        assertTrue("DARK" in file.readText(), file.readText())
    }

    @Test
    fun a_new_repository_on_the_same_file_restores_each_mode() = runTest {
        ThemeMode.entries.forEach { mode ->
            FileThemeRepository(file).set(mode)

            assertEquals(mode, FileThemeRepository(file).themeMode.first())
        }
    }

    @Test
    fun a_set_before_the_first_read_wins_over_the_stored_value() = runTest {
        FileThemeRepository(file).set(ThemeMode.LIGHT)
        val repo = FileThemeRepository(file)

        repo.set(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, repo.themeMode.first())
    }
}
