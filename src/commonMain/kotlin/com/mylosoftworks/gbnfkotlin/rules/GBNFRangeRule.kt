package com.mylosoftworks.gbnfkotlin.rules

import com.mylosoftworks.gbnfkotlin.parsing.ParseResult
import com.mylosoftworks.gbnfkotlin.parsing.gbnfParseError

class GBNFRangeRule(val rangeDef: String, val negate: Boolean = false): GBNFRule() {
    override fun compile(): String {
        return "[" + (if (negate) "^" else "") +
                "${rangeDef.replace("\\^", "^").replace("^", "\\^")}]" // Prevent mistakes when the user already escapes
    }

//    val regex by lazy { Regex(compile()) } // T/ODO: Test properly, currently, assuming GBNF's ranges are like regex ranges, it should work, however, I'm not sure if this is true yet.

    private val ranges: Array<CharRange> by lazy {
        // Example: [a-fABCDEF0-9\-] should allow all of [abcdefABCDEF0123456789-]
        val currentList = mutableListOf<CharRange>()
        var currentChars = ""
        for (char in rangeDef) {
            if (char == '-') {
                if (currentChars.isEmpty()) error("Range contains \"-\" but doesn't have start char and is not escaped.")
                if (currentChars == "\\") {
                    currentList.add(CharRange('-', '-')) // Escaped hyphen
                    currentChars = ""
                    continue
                }
            }

            if (currentChars.endsWith("-")) { // Next character is end of range
                currentList.add(CharRange(currentChars[0], char))
                currentChars = ""
                continue
            }

            if (currentChars.length == 1) { // Already has a character
                var currentChar = currentChars[0]
                currentList.add(CharRange(currentChar, currentChar))
                currentChars = ""
            }

            currentChars += char
        }

        currentList.toTypedArray()
    }

    private fun validateCharacter(char: Char): Boolean {
        return ranges.any { it.contains(char) } != negate
    }

    /**
     * Using regex to parse the character here (not very efficient, but it does the job)
     */
    override fun parse(string: String): Pair<ParseResult, String> {
        if (string.isEmpty()) gbnfParseError("String can't match range because it is empty.")

        // Take first character
        val char = string[0]
        if (validateCharacter(char)) return ParseResult(char.toString(), this) to string.substring(1)

        gbnfParseError("Char doesn't match range:\n$string")
    }
}