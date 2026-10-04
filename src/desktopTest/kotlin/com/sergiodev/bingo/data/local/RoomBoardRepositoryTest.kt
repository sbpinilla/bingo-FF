package com.sergiodev.bingo.data.local

import androidx.sqlite.SQLiteException
import com.sergiodev.bingo.domain.model.BoardCard
import com.sergiodev.bingo.domain.repository.DuplicateIdentifierException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class RoomBoardRepositoryTest {

    private val dir: Path = Files.createTempDirectory("bingoff-boards")
    private val opened = mutableListOf<BingoDatabase>()

    private fun open(): BingoDatabase = buildBingoDatabase(dir.resolve("bingoff.db")).also { opened += it }

    private fun repository(db: BingoDatabase = open()) = RoomBoardRepository(db)

    private fun numbers(offset: Int = 0) = List(24) { it + 1 + offset }

    @AfterTest
    fun closeAll() {
        opened.forEach { it.close() }
    }

    @Test
    fun addBoard_persists_a_board_with_its_numbers() = runTest {
        val repo = repository()

        val result = repo.addBoard("A1", numbers())

        assertTrue(result.isSuccess)
        assertEquals(listOf(BoardCard(1L, "A1", numbers())), repo.observeBoards().first())
    }

    @Test
    fun addBoard_duplicate_identifier_fails() = runTest {
        val repo = repository()
        repo.addBoard("A1", numbers())

        val result = repo.addBoard("A1", numbers(10))

        assertTrue(result.isFailure)
        assertIs<DuplicateIdentifierException>(result.exceptionOrNull())
        assertEquals(1, repo.observeBoards().first().size)
        assertEquals(numbers(), repo.observeBoards().first().single().numbers)
    }

    @Test
    fun dao_unique_index_throws_the_kmp_sqlite_exception() = runTest {
        val dao = open().boardDao()
        dao.insert(BoardEntity(identifier = "A1", numbers = numbers()))

        assertFailsWith<SQLiteException> {
            dao.insert(BoardEntity(identifier = "A1", numbers = numbers(5)))
        }
    }

    @Test
    fun ids_ascending_after_restart() = runTest {
        val first = open()
        val repo = RoomBoardRepository(first)
        repo.addBoard("C", numbers())
        repo.addBoard("A", numbers(1))
        repo.addBoard("B", numbers(2))
        repo.deleteBoard(2L)
        first.close()

        val restarted = repository()
        restarted.addBoard("D", numbers(3))

        val boards = restarted.observeBoards().first()
        assertEquals(listOf(1L, 3L, 4L), boards.map { it.id })
        assertEquals(listOf("C", "B", "D"), boards.map { it.identifier })
    }

    @Test
    fun delete_removes() = runTest {
        val repo = repository()
        repo.addBoard("A1", numbers())
        repo.addBoard("A2", numbers(1))

        repo.deleteBoard(1L)

        assertEquals(listOf("A2"), repo.observeBoards().first().map { it.identifier })
    }

    @Test
    fun delete_unknown_id_is_a_noop() = runTest {
        val repo = repository()
        repo.addBoard("A1", numbers())

        repo.deleteBoard(999L)

        assertEquals(listOf(1L), repo.observeBoards().first().map { it.id })
    }

    @Test
    fun import_skips_dups() = runTest {
        val repo = repository()
        repo.addBoard("A1", numbers())

        val result = repo.importBoards(
            listOf(
                BoardCard(1L, "Renamed", numbers(1)), // existing id
                BoardCard(7L, "A1", numbers(2)), // existing identifier, other id
                BoardCard(8L, "B1", numbers(3)), // new
                BoardCard(8L, "B2", numbers(4)), // in-batch duplicate id
                BoardCard(9L, "B1", numbers(5)), // in-batch duplicate identifier
            ),
        )

        assertEquals(1, result.imported)
        assertEquals(4, result.skipped)
        val boards = repo.observeBoards().first()
        assertEquals(listOf(1L, 8L), boards.map { it.id })
        assertEquals(listOf("A1", "B1"), boards.map { it.identifier })
    }

    @Test
    fun import_preserves_original_ids() = runTest {
        val repo = repository()

        val result = repo.importBoards(listOf(BoardCard(5L, "X", numbers()), BoardCard(2L, "Y", numbers(1))))

        assertEquals(2, result.imported)
        assertEquals(0, result.skipped)
        assertEquals(listOf(2L, 5L), repo.observeBoards().first().map { it.id })
    }
}
