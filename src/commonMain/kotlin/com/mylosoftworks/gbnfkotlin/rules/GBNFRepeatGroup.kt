package com.mylosoftworks.gbnfkotlin.rules

import com.mylosoftworks.gbnfkotlin.parsing.GBNFParseError
import com.mylosoftworks.gbnfkotlin.parsing.ParseResult
import com.mylosoftworks.gbnfkotlin.parsing.gbnfParseError

open class GBNFRepeatGroup(val min: Int = 0, val max: Int? = min): GBNFGroup() {
    override fun compile(): String {
        return rules.joinToString(" ", "(", ")") { it.compile() } + compileSuffix()
    }

    open fun compileSuffix(): String {
        return if (max == min) "{$min}" else if (max == null) "{$min,}" else "{$min,$max}"
    }

    override fun parse(string: String): Pair<ParseResult, String> {
        // Try to match as many times as possible (as many as allowed), if unable to match enough times, return null.

        var hitCount = 0

        var stringRemainder = string
        val subMatches = mutableListOf<ParseResult>()
        for(i in 0..(max ?: 999999)) { // One of the reasons this parser is not recommended
            try {
                val (result, remainder) = super.parse(stringRemainder) // If anything in this entity fails to parse, that means this entity failed to parse.
                stringRemainder = remainder // Since parsing moves forwards

                subMatches.add(result) // Add the results to the parsed classes list
                hitCount++
            }
            catch (e: GBNFParseError) {}
        }

        if (hitCount < min) gbnfParseError("Not enough hits for match:\n$stringRemainder") // Not enough hits, match is not valid

        return ParseResult(
            subMatches.joinToString("") { it.strValue },
            this,
            subMatches
        ) to stringRemainder
    }
}

class GBNFOptionalGroup: GBNFRepeatGroup(0, 1) {
    override fun compileSuffix() = "?"
}

class GBNFOneOrMoreGroup: GBNFRepeatGroup(1, null) {
    override fun compileSuffix() = "+"
}

class GBNFAnyCountGroup: GBNFRepeatGroup(0, null) {
    override fun compileSuffix() = "*"
}