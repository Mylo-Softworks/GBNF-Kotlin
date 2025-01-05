package com.mylosoftworks.gbnfkotlin.rules

import com.mylosoftworks.gbnfkotlin.parsing.GBNFParseError
import com.mylosoftworks.gbnfkotlin.parsing.ParseResult
import com.mylosoftworks.gbnfkotlin.sanitizeGBNFString

class GBNFLiteralRule(val literal: String): GBNFRule() {
    override fun compile(): String {
        return "\"${sanitizeGBNFString(literal)}\""
    }

    override fun parse(string: String): Result<Pair<ParseResult<*>, String>> {
        if (!string.startsWith(literal)) return Result.failure(GBNFParseError("String doesn't match literal \"$literal\":\n$string"))

        return Result.success(ParseResult(literal, this) to string.substring(literal.length))
    }
}