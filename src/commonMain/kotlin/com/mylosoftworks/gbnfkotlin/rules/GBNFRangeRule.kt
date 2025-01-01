package com.mylosoftworks.gbnfkotlin.rules

import com.mylosoftworks.gbnfkotlin.parsing.ParseResult

class GBNFRangeRule(val rangeDef: String, val negate: Boolean = false): GBNFRule() {
    override fun compile(): String {
        return "[" + (if (negate) "^" else "") +
                "${rangeDef.replace("\\^", "^").replace("^", "\\^")}]" // Prevent mistakes when the user already escapes
    }

    val regex by lazy { Regex(compile()) } // TODO: Test properly, currently, assuming GBNF's ranges are like regex ranges, it should work, however, I'm not sure if this is true yet.

    /**
     * Using regex to parse the character here (not very efficient, but it does the job)
     */
    override fun parse(string: String): Pair<ParseResult, String>? {
        if (string.isEmpty()) return null

        // Take first character
        val char = string[0].toString()
        if (!regex.matches(char)) return null

        return ParseResult(char, this) to string.substring(1)
    }
}