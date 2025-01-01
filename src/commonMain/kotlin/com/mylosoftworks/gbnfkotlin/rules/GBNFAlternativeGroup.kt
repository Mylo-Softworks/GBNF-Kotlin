package com.mylosoftworks.gbnfkotlin.rules

import com.mylosoftworks.gbnfkotlin.parsing.ParseResult

class GBNFAlternativeGroup: GBNFGroup() {
    override fun compile(): String {
        return rules.joinToString(" | ", "(", ")") { it.compile() }
    }

    override fun parse(string: String): Pair<ParseResult, String>? { // Alternative group only requires one
        // Very similar to regular group match, but instead of collecting all matches, we return at the first match.
        rules.forEach {
            val possibleResultPair = it.parse(string) // If anything in this entity fails to parse, that means this entity failed to parse.

            if (possibleResultPair != null) { // Match found!
                val (match, remainder) = possibleResultPair
                return ParseResult(
                    match.strValue,
                    this,
                    listOf(match)
                ) to remainder
            }
        }
        return null
    }
}