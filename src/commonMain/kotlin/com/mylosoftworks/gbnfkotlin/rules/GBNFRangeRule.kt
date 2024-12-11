package com.mylosoftworks.com.mylosoftworks.gbnfkotlin.rules

class GBNFRangeRule(val rangeDef: String, val negate: Boolean = false): GBNFRule() {
    override fun compile(): String {
        return "[" + (if (negate) "^" else "") +
                "${rangeDef.replace("\\^", "^").replace("^", "\\^")}]" // Prevent mistakes when the user already escapes
    }
}