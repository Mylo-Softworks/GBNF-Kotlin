package com.mylosoftworks.gbnfkotlin.rules

import com.mylosoftworks.gbnfkotlin.parsing.ParseResult
import com.mylosoftworks.gbnfkotlin.parsing.gbnfParseError

class GBNFRangeRule(val rangeDef: String, val negate: Boolean = false): GBNFRule() {
    override fun compile(): String {
        return "[" + (if (negate) "^" else "") +
                "${rangeDef.replace("\\^", "^").replace("^", "\\^")}]" // Prevent mistakes when the user already escapes
    }

    val regex by lazy { Regex(compile()) } // TODO: Test properly, currently, assuming GBNF's ranges are like regex ranges, it should work, however, I'm not sure if this is true yet.

    /**
     * Using regex to parse the character here (not very efficient, but it does the job)
     */
    override fun parse(string: String): Pair<ParseResult, String> {
        if (string.isEmpty()) gbnfParseError("String can't match range because it is empty.")

        // Take first character
        val char = string[0].toString()
        if (!regex.matches(char)) gbnfParseError("Char doesn't match range:\n$string")

        return ParseResult(char, this) to string.substring(1)
    }
}