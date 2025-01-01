package com.mylosoftworks.gbnfkotlin.rules

import com.mylosoftworks.gbnfkotlin.parsing.ParseResult
import com.mylosoftworks.gbnfkotlin.sanitizeGBNFString

class GBNFLiteralRule(val literal: String): GBNFRule() {
    override fun compile(): String {
        return "\"${sanitizeGBNFString(literal)}\""
    }

    override fun parse(string: String): Pair<ParseResult, String>? {
        if (!string.startsWith(literal)) return null

        return ParseResult(literal, this) to string.substring(literal.length)
    }
}