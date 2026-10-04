package com.sergiodev.bingo.data.file

import java.nio.file.Path

/**
 * Per-user application data location. [root] is injectable so tests can use a temp dir.
 * Files kept here: `bingoff.db`, `active-game.json`, `theme.json`.
 */
class AppDirs(val root: Path) {
    val databaseFile: Path get() = root.resolve("bingoff.db")
    val activeGameFile: Path get() = root.resolve("active-game.json")

    companion object {
        /** Pure resolution from OS name, home directory and environment (empty values count as unset). */
        fun resolve(osName: String, home: String, env: Map<String, String>): AppDirs {
            val os = osName.lowercase()
            val root = when {
                "mac" in os -> Path.of(home, "Library", "Application Support", "BingoFF")
                "win" in os -> {
                    val appData = env["APPDATA"]?.takeIf { it.isNotEmpty() } ?: Path.of(home, "AppData", "Roaming").toString()
                    Path.of(appData, "BingoFF")
                }
                else -> {
                    val xdg = env["XDG_DATA_HOME"]?.takeIf { it.isNotEmpty() }
                    if (xdg != null) Path.of(xdg, "bingoff") else Path.of(home, ".local", "share", "bingoff")
                }
            }
            return AppDirs(root)
        }

        fun default(): AppDirs =
            resolve(System.getProperty("os.name"), System.getProperty("user.home"), System.getenv())
    }
}
