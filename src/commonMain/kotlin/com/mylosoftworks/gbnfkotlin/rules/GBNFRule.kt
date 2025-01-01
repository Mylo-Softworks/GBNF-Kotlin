package com.mylosoftworks.gbnfkotlin.rules

import com.mylosoftworks.gbnfkotlin.CompilableParsable
import com.mylosoftworks.gbnfkotlin.parsing.ParseResult

/**
 * A rule is a single entry defining grammar, for example:
 *
 * ```bnf
 * entity ::= "these" "are" rules
 * rules ::= "these" | "too"
 * ```
 * In the example above, `"these"` is a rule, `"are"` is a rule, and `entities` is also a rule.
 * `"these"` and `"are"` are literals, while `entities` is an entity.
 */
abstract class GBNFRule : CompilableParsable {
    /**
     * Compile this rule
     *
     * Literals will be compiled as `"literal"` while entity references will be compiled as `entity`.
     */
    abstract override fun compile(): String
    abstract override fun parse(string: String): Pair<ParseResult, String>
}