package com.mylosoftworks.gbnfkotlin.rules

import com.mylosoftworks.gbnfkotlin.parsing.ParseResult
import com.mylosoftworks.gbnfkotlin.parsing.gbnfParseError
import com.mylosoftworks.gbnfkotlin.sanitizeGBNFString

class GBNFLiteralRule(val literal: String): GBNFRule() {
    override fun compile(): String {
        return "\"${sanitizeGBNFString(literal)}\""
    }

    override fun parse(string: String): Pair<ParseResult, String> {
        if (!string.startsWith(literal)) gbnfParseError("String doesn't match literal \"$literal\":\n$string")

        return ParseResult(literal, this) to string.substring(literal.length)
    }
}