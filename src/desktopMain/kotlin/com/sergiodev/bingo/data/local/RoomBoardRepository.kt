package com.sergiodev.bingo.data.local

import androidx.sqlite.SQLiteException
import com.sergiodev.bingo.domain.model.BoardCard
import com.sergiodev.bingo.domain.repository.BoardRepository
import com.sergiodev.bingo.domain.repository.DuplicateIdentifierException
import com.sergiodev.bingo.domain.repository.ImportResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomBoardRepository(private val database: BingoDatabase) : BoardRepository {

    private val dao get() = database.boardDao()

    override fun observeBoards(): Flow<List<BoardCard>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    /** The transactional [BoardDao.insertIfAbsent] is the primary guard; the unique index is the backstop. */
    override suspend fun addBoard(identifier: String, numbers: List<Int>): Result<Unit> =
        try {
            val id = dao.insertIfAbsent(BoardEntity(identifier = identifier, numbers = numbers))
            if (id == null) Result.failure(DuplicateIdentifierException(identifier)) else Result.success(Unit)
        } catch (e: SQLiteException) {
            Result.failure(DuplicateIdentifierException(identifier).apply { initCause(e) })
        }

    override suspend fun importBoards(boards: List<BoardCard>): ImportResult {
        val inserted = dao.importNew(boards.map { BoardEntity(it.id, it.identifier, it.numbers) })
        return ImportResult(imported = inserted, skipped = boards.size - inserted)
    }

    override suspend fun deleteBoard(id: Long) {
        dao.deleteById(id)
    }
}
