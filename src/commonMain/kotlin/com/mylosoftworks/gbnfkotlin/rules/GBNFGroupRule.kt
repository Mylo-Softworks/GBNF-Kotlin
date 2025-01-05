package com.mylosoftworks.gbnfkotlin.rules

import com.mylosoftworks.gbnfkotlin.entries.GBNFEntity
import com.mylosoftworks.gbnfkotlin.parsing.ParseResult

/**
 * A group rule is a rule which indicates that items within should be grouped together, but unnamed.
 */
open class GBNFGroup: GBNFEntity(null, null) {
    override fun compile(): String {
        if (rules.count() == 1) return rules[0].compile()
        return rules.joinToString(" ", "(", ")") { it.compile() }
    }

    fun rule(): GBNFGroupRule {
        return GBNFGroupRule(this)
    }
}

class GBNFGroupRule(val group: GBNFGroup): GBNFRule() {
    override fun compile() = group.compile()
    override fun parse(string: String): Result<Pair<ParseResult<*>, String>> = group.parse(string)
}