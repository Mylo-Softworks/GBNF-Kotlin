package com.mylosoftworks.gbnfkotlin

import com.mylosoftworks.gbnfkotlin.parsing.ParseResult

interface CompilableParsable {
    /**
     * Compile this entity to a valid GBNF string.
     */
    fun compile(): String

    /**
     * Parse the string (forwards only), returns the PartialParseResult (for merging) and the string containing the remainder.
     */
    fun parse(string: String): Result<Pair<ParseResult, String>>

    fun parseOrThrow(string: String): Pair<ParseResult, String> {
        return parse(string).getOrThrow()
    }
}