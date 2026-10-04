package com.sergiodev.bingo.domain.repository

import com.sergiodev.bingo.domain.model.BoardCard
import kotlinx.coroutines.flow.Flow

/**
 * Port for board persistence. Deliberately has no lookup-by-identifier
 * method — board lookup was descoped from this change; identifier
 * uniqueness is enforced by a DB unique index + insert-conflict handling.
 */
interface BoardRepository {
    fun observeBoards(): Flow<List<BoardCard>>

    /**
     * Adds a new board. Returns [Result.failure] when [identifier] already
     * exists (unique-identifier conflict).
     */
    suspend fun addBoard(identifier: String, numbers: List<Int>): Result<Unit>

    /**
     * Imports [boards], silently skipping any entry whose [BoardCard.id]
     * already exists, whose [BoardCard.identifier] already exists under a
     * different id, or that duplicates an earlier entry within [boards]
     * itself. Surviving entries are inserted preserving their original id.
     */
    suspend fun importBoards(boards: List<BoardCard>): ImportResult

    /**
     * Permanently removes the board with [id]. A no-op (no exception) when
     * no board with that id exists.
     */
    suspend fun deleteBoard(id: Long)
}
