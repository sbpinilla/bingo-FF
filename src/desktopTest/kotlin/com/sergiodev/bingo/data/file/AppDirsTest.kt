package com.sergiodev.bingo.data.file

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class AppDirsTest {

    @Test
    fun macos_uses_application_support() {
        val dirs = AppDirs.resolve(osName = "Mac OS X", home = "/Users/ana", env = emptyMap())

        assertEquals(Path.of("/Users/ana/Library/Application Support/BingoFF"), dirs.root)
    }

    @Test
    fun windows_uses_appdata() {
        val dirs = AppDirs.resolve("Windows 11", "C:\\Users\\ana", mapOf("APPDATA" to "C:\\Users\\ana\\AppData\\Roaming"))

        assertEquals(Path.of("C:\\Users\\ana\\AppData\\Roaming", "BingoFF"), dirs.root)
    }

    @Test
    fun linux_prefers_xdg_data_home() {
        val dirs = AppDirs.resolve("Linux", "/home/ana", mapOf("XDG_DATA_HOME" to "/data/xdg"))

        assertEquals(Path.of("/data/xdg/bingoff"), dirs.root)
    }

    @Test
    fun linux_falls_back_to_local_share_when_xdg_unset_or_empty() {
        val unset = AppDirs.resolve("Linux", "/home/ana", emptyMap())
        val empty = AppDirs.resolve("Linux", "/home/ana", mapOf("XDG_DATA_HOME" to ""))

        assertEquals(Path.of("/home/ana/.local/share/bingoff"), unset.root)
        assertEquals(Path.of("/home/ana/.local/share/bingoff"), empty.root)
    }

    @Test
    fun database_file_lives_under_the_root() {
        val dirs = AppDirs(Path.of("/tmp/x"))

        assertEquals(Path.of("/tmp/x/bingoff.db"), dirs.databaseFile)
    }

    @Test
    fun active_game_file_lives_under_the_root() {
        val dirs = AppDirs(Path.of("/tmp/x"))

        assertEquals(Path.of("/tmp/x/active-game.json"), dirs.activeGameFile)
    }

    @Test
    fun theme_file_lives_under_the_root() {
        assertEquals(Path.of("/tmp/x/theme.json"), AppDirs(Path.of("/tmp/x")).themeFile)
    }
}
