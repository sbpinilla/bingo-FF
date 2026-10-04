package com.sergiodev.bingo.i18n

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Reads the real resource files so a key added to one language and forgotten in the other fails the build. */
class StringsParityTest {
    private val spanish = readStrings("values")
    private val english = readStrings("values-en")

    @Test
    fun both_resource_sets_define_exactly_the_same_keys() {
        assertEquals(spanish.keys, english.keys)
    }

    @Test
    fun resource_sets_are_not_trivially_empty() {
        assertTrue(spanish.size > 50, "expected the full UI key set, got ${spanish.size}")
        assertTrue("app_name" in spanish)
    }

    @Test
    fun every_key_uses_the_same_format_arguments_in_both_languages() {
        val mismatches = spanish.keys.filter { placeholders(spanish.getValue(it)) != placeholders(english.getValue(it)) }
        assertEquals(emptyList(), mismatches)
    }

    @Test
    fun parameterized_keys_exist_and_carry_their_arguments() {
        assertEquals(listOf("%1\$d"), placeholders(spanish.getValue("game_play_error_duplicate_call")))
        assertEquals(listOf("%1\$d", "%2\$d"), placeholders(english.getValue("import_result_message")))
    }

    @Test
    fun placeholder_extraction_orders_by_argument_index() {
        assertEquals(listOf("%1\$s", "%2\$d"), placeholders("Mode %2\$d then %1\$s"))
        assertEquals(emptyList(), placeholders("no arguments"))
    }

    @Test
    fun no_value_is_blank() {
        val blanks = (spanish + english.mapKeys { "en:" + it.key }).filterValues { it.isBlank() }.keys
        assertEquals(emptySet(), blanks)
    }

    private fun readStrings(directory: String): Map<String, String> {
        val file = File("src/commonMain/composeResources/$directory/strings.xml")
        assertTrue(file.isFile, "missing ${file.path}")
        return STRING_ENTRY.findAll(file.readText()).associate { it.groupValues[1] to it.groupValues[2] }
    }

    private fun placeholders(value: String): List<String> =
        PLACEHOLDER.findAll(value).map { it.value }.sortedBy { it.substring(1, it.indexOf('$')).toInt() }.toList()

    private companion object {
        val STRING_ENTRY = Regex("""<string name="([^"]+)">(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)
        val PLACEHOLDER = Regex("""%\d+\$[sd]""")
    }
}
