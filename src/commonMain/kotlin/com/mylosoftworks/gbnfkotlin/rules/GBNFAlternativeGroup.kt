package com.mylosoftworks.gbnfkotlin.rules

import com.mylosoftworks.gbnfkotlin.parsing.GBNFParseError
import com.mylosoftworks.gbnfkotlin.parsing.ParseResult
import com.mylosoftworks.gbnfkotlin.parsing.gbnfParseError

class GBNFAlternativeGroup: GBNFGroup() {
    override fun compile(): String {
        return rules.joinToString(" | ", "(", ")") { it.compile() }
    }

    override fun parse(string: String): Pair<ParseResult, String> { // Alternative group only requires one
        // Very similar to regular group match, but instead of collecting all matches, we return at the first match.
        rules.forEach {
            var possibleResultPair: Pair<ParseResult, String>? = null
            try {
                possibleResultPair = it.parse(string)
            }
            catch (e: GBNFParseError) {
                // This entity failed to parse
            }

            if (possibleResultPair != null) { // Match found!
                val (match, remainder) = possibleResultPair
                return ParseResult(
                    match.strValue,
                    this,
                    listOf(match)
                ) to remainder
            }
        }

        // Nothing matches
        gbnfParseError("No match found:\n$string")
    }
}