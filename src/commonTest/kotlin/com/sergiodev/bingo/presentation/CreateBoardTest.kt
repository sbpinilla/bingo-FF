package com.sergiodev.bingo.presentation

import com.sergiodev.bingo.domain.model.BingoLetter
import com.sergiodev.bingo.domain.model.BoardCard
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CreateBoardTest {

    private fun valid(): Map<BingoLetter, List<String>> = mapOf(
        BingoLetter.B to listOf("1", "2", "3", "4", "5"),
        BingoLetter.I to listOf("16", "17", "18", "19", "20"),
        BingoLetter.N to listOf("31", "32", "34", "35"),
        BingoLetter.G to listOf("46", "47", "48", "49", "50"),
        BingoLetter.O to listOf("61", "62", "63", "64", "65"),
    )

    private fun TestScope.holder(repo: FakeBoardRepository = FakeBoardRepository()) =
        CreateBoardHolder(repo, backgroundScope)

    private fun CreateBoardHolder.fill(numbers: Map<BingoLetter, List<String>>) {
        numbers.forEach { (letter, values) -> values.forEachIndexed { i, v -> onNumberChange(letter, i, v) } }
    }

    // --- computeFieldErrors (pure) ---

    @Test
    fun computeFieldErrors_valid_board_has_no_errors() {
        val errors = computeFieldErrors(valid(), flagBlank = true)

        assertEquals(24, errors.values.sumOf { it.size })
        assertTrue(errors.values.flatten().all { it == null })
    }

    @Test
    fun computeFieldErrors_out_of_range() {
        val numbers = valid() + (BingoLetter.B to listOf("16", "2", "3", "4", "5"))

        val errors = computeFieldErrors(numbers, flagBlank = true)

        assertEquals("out_of_range", errors.getValue(BingoLetter.B)[0])
        assertNull(errors.getValue(BingoLetter.B)[1])
    }

    @Test
    fun computeFieldErrors_duplicate_flags_every_occurrence() {
        // Letter ranges are disjoint, so an in-range duplicate always sits in one column.
        val numbers = valid() + (BingoLetter.B to listOf("7", "2", "3", "4", "7"))

        val errors = computeFieldErrors(numbers, flagBlank = true)

        assertEquals("duplicate", errors.getValue(BingoLetter.B)[0])
        assertEquals("duplicate", errors.getValue(BingoLetter.B)[4])
        assertNull(errors.getValue(BingoLetter.B)[1])
    }

    @Test
    fun computeFieldErrors_range_beats_duplicate_and_out_of_range_never_counts_as_duplicate() {
        // "16" twice in B: both out of range, neither is reported as duplicate.
        val numbers = valid() + (BingoLetter.B to listOf("16", "16", "3", "4", "5"))

        val errors = computeFieldErrors(numbers, flagBlank = true)

        assertEquals("out_of_range", errors.getValue(BingoLetter.B)[0])
        assertEquals("out_of_range", errors.getValue(BingoLetter.B)[1])
    }

    @Test
    fun computeFieldErrors_non_integer_is_invalid() {
        val numbers = valid() + (BingoLetter.G to listOf("abc", "47", "48", "49", "50"))

        val errors = computeFieldErrors(numbers, flagBlank = false)

        assertEquals("invalid", errors.getValue(BingoLetter.G)[0])
    }

    @Test
    fun computeFieldErrors_blank_flagged_only_when_flagBlank() {
        val numbers = valid() + (BingoLetter.O to listOf("", "62", "63", "64", "65"))

        assertEquals("invalid", computeFieldErrors(numbers, flagBlank = true).getValue(BingoLetter.O)[0])
        assertNull(computeFieldErrors(numbers, flagBlank = false).getValue(BingoLetter.O)[0])
    }

    // --- holder ---

    @Test
    fun initial_state_has_24_empty_fields_and_no_errors() = runTest {
        val state = holder().state.value

        assertEquals("", state.identifier)
        assertEquals(listOf(5, 5, 4, 5, 5), BingoLetter.entries.map { state.numbers.getValue(it).size })
        assertTrue(state.numbers.values.flatten().all { it.isEmpty() })
        assertTrue(state.fieldErrors.isEmpty())
        assertFalse(state.submitSuccess)
    }

    @Test
    fun valid_submit_persists_column_major_and_signals_success() = runTest {
        val repo = FakeBoardRepository()
        val holder = holder(repo)
        holder.onIdentifierChange("Casa1")
        holder.fill(valid())

        holder.onSubmit()
        runCurrent()

        assertTrue(holder.state.value.submitSuccess)
        assertNull(holder.state.value.identifierError)
        assertEquals("Casa1", repo.lastAdded?.first)
        assertEquals(
            listOf(1, 2, 3, 4, 5, 16, 17, 18, 19, 20, 31, 32, 34, 35, 46, 47, 48, 49, 50, 61, 62, 63, 64, 65),
            repo.lastAdded?.second,
        )
    }

    @Test
    fun blank_identifier_exposes_reason_and_persists_nothing() = runTest {
        val repo = FakeBoardRepository()
        val holder = holder(repo)
        holder.onIdentifierChange("   ")
        holder.fill(valid())

        holder.onSubmit()
        runCurrent()

        assertEquals(CreateBoardErrorReason.BlankIdentifier, holder.state.value.identifierError)
        assertFalse(holder.state.value.submitSuccess)
        assertEquals(0, repo.addCalls)
    }

    @Test
    fun editing_identifier_clears_its_error() = runTest {
        val holder = holder()
        holder.onSubmit()
        assertEquals(CreateBoardErrorReason.BlankIdentifier, holder.state.value.identifierError)

        holder.onIdentifierChange("A")

        assertNull(holder.state.value.identifierError)
    }

    @Test
    fun duplicate_identifier_exposes_reason_and_adds_nothing() = runTest {
        val repo = FakeBoardRepository(listOf(BoardCard(1L, "A1", List(24) { it + 1 })))
        val holder = holder(repo)
        holder.onIdentifierChange("A1")
        holder.fill(valid())

        holder.onSubmit()
        runCurrent()

        assertEquals(CreateBoardErrorReason.DuplicateIdentifier, holder.state.value.identifierError)
        assertFalse(holder.state.value.submitSuccess)
        assertEquals(1, repo.boards.value.size)
    }

    @Test
    fun out_of_range_submit_sets_field_error_and_persists_nothing() = runTest {
        val repo = FakeBoardRepository()
        val holder = holder(repo)
        holder.onIdentifierChange("Casa1")
        holder.fill(valid() + (BingoLetter.B to listOf("16", "2", "3", "4", "5")))

        holder.onSubmit()
        runCurrent()

        assertEquals("out_of_range", holder.state.value.fieldErrors.getValue(BingoLetter.B)[0])
        assertEquals(0, repo.addCalls)
    }

    @Test
    fun blank_field_blocks_submit_when_submitted() = runTest {
        val repo = FakeBoardRepository()
        val holder = holder(repo)
        holder.onIdentifierChange("Casa1")
        holder.fill(valid() + (BingoLetter.O to listOf("", "62", "63", "64", "65")))

        holder.onSubmit()
        runCurrent()

        assertNotNull(holder.state.value.fieldErrors.getValue(BingoLetter.O)[0])
        assertEquals(0, repo.addCalls)
    }

    @Test
    fun blank_not_flagged_while_editing_after_failed_submit_but_flagged_on_next_submit() = runTest {
        val holder = holder()
        holder.onIdentifierChange("Casa1")
        holder.fill(valid() + (BingoLetter.B to listOf("16", "2", "3", "4", "5")) + (BingoLetter.O to listOf("", "62", "63", "64", "65")))
        holder.onSubmit()
        runCurrent()
        assertNotNull(holder.state.value.fieldErrors.getValue(BingoLetter.O)[0]) // flagged by the submit

        holder.onNumberChange(BingoLetter.B, 0, "1") // edit another field
        assertNull(holder.state.value.fieldErrors.getValue(BingoLetter.O)[0]) // blank no longer flagged

        holder.onSubmit()
        runCurrent()
        assertNotNull(holder.state.value.fieldErrors.getValue(BingoLetter.O)[0]) // flagged again on submit
    }

    @Test
    fun fixing_one_field_keeps_other_errors() = runTest {
        val holder = holder()
        holder.onIdentifierChange("Casa1")
        holder.fill(
            valid() + (BingoLetter.B to listOf("16", "2", "3", "4", "5")) +
                (BingoLetter.G to listOf("99", "47", "48", "49", "50")),
        )
        holder.onSubmit()
        runCurrent()

        holder.onNumberChange(BingoLetter.B, 0, "1")

        assertNull(holder.state.value.fieldErrors.getValue(BingoLetter.B)[0])
        assertNotNull(holder.state.value.fieldErrors.getValue(BingoLetter.G)[0])
    }

    @Test
    fun fixing_a_duplicate_clears_both_sides() = runTest {
        val holder = holder()
        holder.onIdentifierChange("Casa1")
        holder.fill(
            valid() + (BingoLetter.B to listOf("5", "5", "3", "4", "2")) +
                (BingoLetter.G to listOf("99", "47", "48", "49", "50")),
        )
        holder.onSubmit()
        runCurrent()
        assertNotNull(holder.state.value.fieldErrors.getValue(BingoLetter.B)[0])
        assertNotNull(holder.state.value.fieldErrors.getValue(BingoLetter.B)[1])

        holder.onNumberChange(BingoLetter.B, 1, "9")

        assertNull(holder.state.value.fieldErrors.getValue(BingoLetter.B)[0])
        assertNull(holder.state.value.fieldErrors.getValue(BingoLetter.B)[1])
        assertNotNull(holder.state.value.fieldErrors.getValue(BingoLetter.G)[0])
    }
}
