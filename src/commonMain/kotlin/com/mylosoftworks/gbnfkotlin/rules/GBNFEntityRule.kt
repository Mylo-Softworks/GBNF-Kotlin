package com.mylosoftworks.gbnfkotlin.rules

import com.mylosoftworks.gbnfkotlin.entries.GBNFEntity
import com.mylosoftworks.gbnfkotlin.parsing.ParseResult

class GBNFEntityRule(val entity: GBNFEntity): GBNFRule() {
    override fun compile(): String {
        return entity.identifier!!
    }

    override fun parse(string: String): Result<Pair<ParseResult, String>> {
        return entity.parse(string)
    }
}