package com.sergiodev.bingo.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sergiodev.bingo.domain.model.BingoLetter
import com.sergiodev.bingo.domain.model.BoardCard
import com.sergiodev.bingo.domain.model.GridPosition
import com.sergiodev.bingo.presentation.BoardCardState
import com.sergiodev.bingo.presentation.BoardsPaneState
import com.sergiodev.bingo.presentation.BoardsState
import com.sergiodev.bingo.presentation.CellState

private val CardGap = 8.dp
private const val MIN_CARD_WIDTH_DP = 220
private const val BOARD_SIZE = 5
private val WinnerBorderWidth = 3.dp

/** Left pane: all boards as compact 5x5 cards in an adaptive, vertically scrolling grid. */
@Composable
fun BoardsPane(
    boardsState: BoardsState,
    paneState: BoardsPaneState,
    onAddBoard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by boardsState.state.collectAsState()
    val cards by paneState.cards.collectAsState()
    val cardsById = remember(cards) { cards.associateBy { it.id } }

    Column(modifier = modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(CardGap)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Cartones (${state.boards.size})", style = MaterialTheme.typography.titleLarge)
            Button(onClick = onAddBoard) { Text("Agregar cartón") }
        }
        if (state.boards.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Todavía no hay cartones. Agrega el primero.")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(MIN_CARD_WIDTH_DP.dp),
                horizontalArrangement = Arrangement.spacedBy(CardGap),
                verticalArrangement = Arrangement.spacedBy(CardGap),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(state.boards, key = { it.id }, contentType = { "board" }) { board ->
                    BoardCardView(board, cardsById[board.id], onDelete = { boardsState.onDeleteRequested(board) })
                }
            }
        }
    }

    state.pendingDelete?.let { board ->
        DeleteConfirmDialog(
            board = board,
            onConfirm = boardsState::onDeleteConfirmed,
            onDismiss = boardsState::onDeleteDismissed,
        )
    }
}

/**
 * Compact card: header (number, identifier, delete) plus the 5x5 grid with the FREE centre.
 * Called cells are filled and bold (never colour alone); a winning board gets a thick border,
 * a tinted background and a label with its completed patterns.
 */
@Composable
private fun BoardCardView(board: BoardCard, card: BoardCardState?, onDelete: () -> Unit) {
    val winner = card?.isWinner == true
    val border = if (winner) {
        BorderStroke(WinnerBorderWidth, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    }
    val container = if (winner) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        border = border,
        colors = CardDefaults.outlinedCardColors(containerColor = container),
    ) {
        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("#${board.id}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    board.identifier,
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp,
                )
                TextButton(onClick = onDelete) { Text("Eliminar", fontSize = 12.sp) }
            }
            if (card != null && winner) {
                Text(
                    "¡BINGO! " + card.winningPatternIds.joinToString(", ") { patternLabel(it) },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp,
                )
            }
            Row(Modifier.fillMaxWidth()) {
                BingoLetter.entries.forEach { letter ->
                    Text(
                        letter.name,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                    )
                }
            }
            (0 until BOARD_SIZE).forEach { row ->
                Row(Modifier.fillMaxWidth()) {
                    BingoLetter.entries.forEachIndexed { column, letter ->
                        val cell = card?.cells?.get(row * BOARD_SIZE + column)
                            ?: CellState(board.numberAt(GridPosition(letter, row + 1)), marked = false)
                        BoardCell(cell, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun BoardCell(cell: CellState, modifier: Modifier) {
    val background = if (cell.marked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (cell.marked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = modifier.aspectRatio(1f).padding(1.dp).background(background, MaterialTheme.shapes.extraSmall),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            cell.number?.toString() ?: "★",
            color = textColor,
            fontSize = 14.sp,
            fontWeight = if (cell.marked) FontWeight.ExtraBold else FontWeight.Normal,
            textAlign = TextAlign.Center,
        )
    }
}
