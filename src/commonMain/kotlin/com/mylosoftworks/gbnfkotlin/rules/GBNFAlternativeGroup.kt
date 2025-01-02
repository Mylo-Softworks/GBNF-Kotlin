package com.mylosoftworks.gbnfkotlin.rules

import com.mylosoftworks.gbnfkotlin.parsing.GBNFParseError
import com.mylosoftworks.gbnfkotlin.parsing.ParseResult

class GBNFAlternativeGroup: GBNFGroup() {
    override fun compile(): String {
        return rules.joinToString(" | ", "(", ")") { it.compile() }
    }

    override fun parse(string: String): Result<Pair<ParseResult, String>> { // Alternative group only requires one
        // Very similar to regular group match, but instead of collecting all matches, we return at the first match.
        rules.forEach {
            var possibleResultPair: Pair<ParseResult, String>? = it.parse(string).getOrElse { return@forEach } // Skip this entry if it doesn't match

            if (possibleResultPair != null) { // Match found!
                val (match, remainder) = possibleResultPair
                return Result.success(ParseResult(
                    match.strValue,
                    this,
                    listOf(match)
                ) to remainder)
            }
        }

        // Nothing matches
        return Result.failure(GBNFParseError("No match found:\n$string"))
    }
}