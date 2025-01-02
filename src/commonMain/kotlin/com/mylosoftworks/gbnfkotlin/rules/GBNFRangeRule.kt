package com.mylosoftworks.gbnfkotlin.rules

import com.mylosoftworks.gbnfkotlin.parsing.GBNFParseError
import com.mylosoftworks.gbnfkotlin.parsing.ParseResult

class GBNFRangeRule(val rangeDef: String, val negate: Boolean = false): GBNFRule() {
    override fun compile(): String {
        return "[" + (if (negate) "^" else "") +
                "${rangeDef.replace("\\^", "^").replace("^", "\\^")
                    .replace("\n", "\\n")}]" // Prevent mistakes when the user already escapes
    }

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
     * Use a custom parser to parse the character ranges
     */
    override fun parse(string: String): Result<Pair<ParseResult, String>> {
        if (string.isEmpty()) return Result.failure(GBNFParseError("String can't match range because it is empty."))

        // Take first character
        val char = string[0]
        if (validateCharacter(char)) return Result.success(ParseResult(char.toString(), this) to string.substring(1))

        return Result.failure(GBNFParseError("Char doesn't match range ($rangeDef):\n$string"))
    }
}