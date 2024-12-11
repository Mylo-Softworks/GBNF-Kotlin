package com.mylosoftworks.com.mylosoftworks.gbnfkotlin.rules

import com.mylosoftworks.com.mylosoftworks.gbnfkotlin.entries.GBNFEntity

/**
 * A group rule is a rule which indicates that items within should be grouped together, but unnamed.
 */
open class GBNFGroup: GBNFEntity(null, null) {
    override fun compile(): String {
        return rules.joinToString(" ", "(", ")") { it.compile() }
    }

    fun rule(): GBNFGroupRule {
        return GBNFGroupRule(this)
    }
}

class GBNFGroupRule(val group: GBNFGroup): GBNFRule() {
    override fun compile() = group.compile()
}