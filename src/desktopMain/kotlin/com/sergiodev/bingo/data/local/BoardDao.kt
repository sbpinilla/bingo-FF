package com.sergiodev.bingo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface BoardDao {
    @Query("SELECT * FROM board ORDER BY id ASC")
    fun observeAll(): Flow<List<BoardEntity>>

    /** ABORT surfaces a unique-identifier violation as `androidx.sqlite.SQLiteException`. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(board: BoardEntity): Long

    @Query("SELECT COUNT(*) FROM board WHERE identifier = :identifier")
    suspend fun countByIdentifier(identifier: String): Int

    @Query("SELECT id FROM board")
    suspend fun getAllIds(): List<Long>

    @Query("SELECT identifier FROM board")
    suspend fun getAllIdentifiers(): List<String>

    /** Bulk insert for import. Non-zero ids are inserted literally. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(boards: List<BoardEntity>)

    @Query("DELETE FROM board WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** Atomic check-then-insert. Returns the new row id, or `null` when the identifier exists. */
    @Transaction
    suspend fun insertIfAbsent(board: BoardEntity): Long? =
        if (countByIdentifier(board.identifier) > 0) null else insert(board)

    /**
     * Atomically inserts the entries whose id and identifier are unused, in the DB and earlier
     * among the entries accepted earlier in [boards]. A skipped entry reserves nothing, so a later
     * entry may reuse its id or identifier. Returns how many rows were inserted.
     */
    @Transaction
    suspend fun importNew(boards: List<BoardEntity>): Int {
        val ids = getAllIds().toMutableSet()
        val identifiers = getAllIdentifiers().toMutableSet()
        val fresh = boards.filter { board ->
            val accepted = board.id !in ids && board.identifier !in identifiers
            if (accepted) {
                ids += board.id
                identifiers += board.identifier
            }
            accepted
        }
        if (fresh.isNotEmpty()) insertAll(fresh)
        return fresh.size
    }
}
