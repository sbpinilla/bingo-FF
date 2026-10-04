package com.sergiodev.bingo.data.file

import com.sergiodev.bingo.domain.model.BingoLetter
import com.sergiodev.bingo.domain.model.BoardCard
import com.sergiodev.bingo.domain.model.GameMode
import com.sergiodev.bingo.domain.repository.ActiveGame
import com.sergiodev.bingo.presentation.FakeBoardRepository
import com.sergiodev.bingo.presentation.GameSessionHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FileStoresTest {

    private val dir: Path = Files.createTempDirectory("bingoff-files")

    @Serializable
    private data class Sample(val name: String, val count: Int = 0)

    private fun sampleStore() = JsonFileStore(dir.resolve("sample.json"), Sample.serializer())

    private fun gameFile() = dir.resolve("active-game.json")

    private val game = ActiveGame(GameMode.COLUMNA, listOf(5, 20), setOf(BingoLetter.B))

    // --- generic JsonFileStore ---

    @Test
    fun store_loads_what_was_saved() = runTest {
        val store = sampleStore()

        store.save(Sample("a", 3))

        assertEquals(Sample("a", 3), store.load())
    }

    @Test
    fun store_load_without_a_file_is_null() = runTest {
        assertNull(sampleStore().load())
    }

    @Test
    fun store_corrupt_content_falls_back_to_null() = runTest {
        dir.resolve("sample.json").writeText("{ not json at all")

        assertNull(sampleStore().load())
    }

    @Test
    fun store_content_of_the_wrong_shape_falls_back_to_null() = runTest {
        dir.resolve("sample.json").writeText("""{"name": 7}""")

        assertNull(sampleStore().load())
    }

    @Test
    fun store_overwrite_replaces_the_content_and_leaves_no_temp_file() = runTest {
        val store = sampleStore()
        store.save(Sample("first", 1))

        store.save(Sample("second", 2))

        assertEquals(Sample("second", 2), store.load())
        assertEquals(listOf("sample.json"), dir.listDirectoryEntries().map { it.fileName.toString() })
    }

    @Test
    fun store_creates_missing_parent_directories() = runTest {
        val nested = JsonFileStore(dir.resolve("a/b/sample.json"), Sample.serializer())

        nested.save(Sample("deep"))

        assertEquals(Sample("deep"), nested.load())
    }

    @Test
    fun store_clear_removes_the_file_and_is_a_noop_when_absent() = runTest {
        val store = sampleStore()
        store.clear()
        store.save(Sample("a"))

        store.clear()

        assertNull(store.load())
        assertFalse(dir.resolve("sample.json").exists())
    }

    // --- FileActiveGameRepository ---

    @Test
    fun active_game_round_trips_with_order_and_dismissed_letters() = runTest {
        val repo = FileActiveGameRepository(gameFile())

        repo.save(game)

        assertEquals(game, repo.load())
        assertEquals(listOf(5, 20), repo.load()?.calledNumbers)
    }

    @Test
    fun active_game_file_carries_version_1() = runTest {
        FileActiveGameRepository(gameFile()).save(game)

        val text = gameFile().readText()
        assertTrue("\"version\"" in text && Regex("\"version\"\\s*:\\s*1").containsMatchIn(text), text)
        assertTrue("\"mode\"" in text && "\"calledNumbers\"" in text && "\"dismissedLetters\"" in text, text)
    }

    @Test
    fun active_game_corrupt_file_means_no_game() = runTest {
        gameFile().writeText("###")

        assertNull(FileActiveGameRepository(gameFile()).load())
    }

    @Test
    fun active_game_unknown_version_means_no_game() = runTest {
        gameFile().writeText("""{"version":2,"mode":"COLUMNA","calledNumbers":[1],"dismissedLetters":[]}""")

        assertNull(FileActiveGameRepository(gameFile()).load())
    }

    @Test
    fun active_game_unknown_mode_means_no_game() = runTest {
        gameFile().writeText("""{"version":1,"mode":"DIAGONAL","calledNumbers":[1],"dismissedLetters":[]}""")

        assertNull(FileActiveGameRepository(gameFile()).load())
    }

    @Test
    fun active_game_clear_removes_it() = runTest {
        val repo = FileActiveGameRepository(gameFile())
        repo.save(game)

        repo.clear()

        assertNull(repo.load())
    }

    @Test
    fun active_game_overwrite_keeps_only_the_latest() = runTest {
        val repo = FileActiveGameRepository(gameFile())
        repo.save(game)
        val later = game.copy(calledNumbers = listOf(5, 20, 47), dismissedLetters = emptySet())

        repo.save(later)

        assertEquals(later, repo.load())
    }

    // --- real-file restart through the holder (real dispatchers: the store does real IO) ---

    @Test
    fun a_new_holder_on_the_same_file_restores_a_started_game() = runTest {
        val boards = FakeBoardRepository(listOf(BoardCard(1L, "Casa1", List(24) { it + 1 })))
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        try {
            withContext(Dispatchers.Default) {
                withTimeout(TIMEOUT_MS) {
                    val first = GameSessionHolder(boards, FileActiveGameRepository(gameFile()), scope)
                    first.loaded.first { it }
                    first.setup.first { it.hasBoards }
                    first.start(GameMode.I)
                    // Persistence is asynchronous: wait for the file, as a closing app would.
                    while (FileActiveGameRepository(gameFile()).load() == null) delay(10)

                    val second = GameSessionHolder(boards, FileActiveGameRepository(gameFile()), scope)
                    second.loaded.first { it }

                    assertEquals(ActiveGame(GameMode.I, emptyList(), emptySet()), second.active.value)
                }
            }
        } finally {
            scope.cancel()
        }
    }

    private companion object {
        const val TIMEOUT_MS = 10_000L
    }
}
