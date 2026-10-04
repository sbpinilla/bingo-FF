package com.sergiodev.bingo.presentation

import com.sergiodev.bingo.domain.model.BoardCard
import com.sergiodev.bingo.domain.repository.BoardRepository
import com.sergiodev.bingo.domain.repository.DuplicateIdentifierException
import com.sergiodev.bingo.domain.repository.ImportResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [BoardRepository] for holder tests. Ids ascend from 1. */
class FakeBoardRepository(seed: List<BoardCard> = emptyList()) : BoardRepository {
    val boards = MutableStateFlow(seed)
    var addCalls = 0
        private set
    var lastAdded: Pair<String, List<Int>>? = null
        private set
    var deletedIds = mutableListOf<Long>()
        private set

    override fun observeBoards(): Flow<List<BoardCard>> = boards

    override suspend fun addBoard(identifier: String, numbers: List<Int>): Result<Unit> {
        addCalls++
        if (boards.value.any { it.identifier == identifier }) {
            return Result.failure(DuplicateIdentifierException(identifier))
        }
        lastAdded = identifier to numbers
        val nextId = (boards.value.maxOfOrNull { it.id } ?: 0L) + 1
        boards.value = boards.value + BoardCard(nextId, identifier, numbers)
        return Result.success(Unit)
    }

    var importCalls = 0
        private set

    /** Same dedup contract as the real repository: id OR identifier already stored, or repeated in-batch, is skipped. */
    override suspend fun importBoards(boards: List<BoardCard>): ImportResult {
        importCalls++
        val accepted = mutableListOf<BoardCard>()
        for (candidate in boards) {
            val taken = this.boards.value + accepted
            if (taken.none { it.id == candidate.id || it.identifier == candidate.identifier }) accepted += candidate
        }
        this.boards.value = this.boards.value + accepted
        return ImportResult(imported = accepted.size, skipped = boards.size - accepted.size)
    }

    override suspend fun deleteBoard(id: Long) {
        deletedIds += id
        boards.value = boards.value.filterNot { it.id == id }
    }
}
