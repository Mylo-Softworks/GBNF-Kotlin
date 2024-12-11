package com.mylosoftworks.com.mylosoftworks.gbnfkotlin.rules

class GBNFAlternativeGroup: GBNFGroup() {
    override fun compile(): String {
        return rules.joinToString(" | ", "(", ")") { it.compile() }
    }
}