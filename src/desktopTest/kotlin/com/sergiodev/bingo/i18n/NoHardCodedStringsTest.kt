package com.sergiodev.bingo.i18n

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Guards against user-facing text creeping back into the UI as literals instead of resources. */
class NoHardCodedStringsTest {
    @Test
    fun ui_sources_pass_no_letter_literals_to_text_or_titles() {
        val root = File("src/desktopMain/kotlin/com/sergiodev/bingo")
        val files = root.walkTopDown().filter { it.isFile && it.extension == "kt" && (it.parentFile.name == "ui" || it.name == "Main.kt") }.toList()
        assertTrue(files.size >= 10, "expected the UI sources to be scanned, got ${files.size}")
        val offenders = files.flatMap { file ->
            file.readLines().mapIndexedNotNull { index, line ->
                if (literalUiText(line)) "${file.name}:${index + 1}: ${line.trim()}" else null
            }
        }
        assertEquals(emptyList(), offenders)
    }

    @Test
    fun detector_flags_literals_and_accepts_resources_and_symbols() {
        assertTrue(literalUiText("""Text("Jugar")"""))
        assertTrue(literalUiText("""label = { Text("Number") }"""))
        assertTrue(literalUiText("""title = "BingoFF","""))
        assertTrue(literalUiText("""Text("Hola ${'$'}name")"""))
        assertEquals(false, literalUiText("""Text(stringResource(Res.string.menu_label))"""))
        assertEquals(false, literalUiText("""Text("★")"""))
        assertEquals(false, literalUiText("""Text("#${'$'}{board.id}", fontSize = 14.sp)"""))
        assertEquals(false, literalUiText("""// Text("comentario")"""))
    }

    private fun literalUiText(line: String): Boolean {
        val code = line.trim()
        if (code.startsWith("//") || code.startsWith("*") || code.startsWith("/*")) return false
        val literalOnly = TEMPLATE_EXPRESSION.replace(code, "")
        return TEXT_LITERAL.containsMatchIn(literalOnly) || TITLE_LITERAL.containsMatchIn(literalOnly)
    }

    private companion object {
        val TEMPLATE_EXPRESSION = Regex("""\$\{[^}]*\}""")
        val TEXT_LITERAL = Regex("""\b(Text|NoticeDialog)\(\s*"[^"]*\p{L}""")
        val TITLE_LITERAL = Regex("""\btitle\s*=\s*"[^"]*\p{L}""")
    }
}
