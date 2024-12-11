package com.mylosoftworks.com.mylosoftworks.gbnfkotlin.rules

import com.mylosoftworks.com.mylosoftworks.gbnfkotlin.sanitizeGBNFString

class GBNFLiteralRule(val literal: String): GBNFRule() {
    override fun compile(): String {
        return "\"${sanitizeGBNFString(literal)}\""
    }
}