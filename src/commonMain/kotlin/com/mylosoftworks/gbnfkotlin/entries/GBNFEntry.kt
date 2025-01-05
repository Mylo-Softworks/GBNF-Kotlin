package com.mylosoftworks.gbnfkotlin.entries

import com.mylosoftworks.gbnfkotlin.CompilableParsable
import com.mylosoftworks.gbnfkotlin.parsing.ParseResult

/**
 * A single line entry.
 */
abstract class GBNFEntry : CompilableParsable {

    /**
     * Compile this entity to a valid GBNF string.
     */
    abstract override fun compile(): String

    /**
     * Parse the string (forwards only), returns the PartialParseResult (for merging) and the string containing the remainder.
     */
    abstract override fun parse(string: String): Result<Pair<ParseResult<*>, String>>
}