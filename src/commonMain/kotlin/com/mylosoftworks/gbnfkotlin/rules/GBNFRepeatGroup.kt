package com.mylosoftworks.com.mylosoftworks.gbnfkotlin.rules

open class GBNFRepeatGroup(val min: Int = 0, val max: Int? = min): GBNFGroup() {
    override fun compile(): String {
        return rules.joinToString(" ", "(", ")") { it.compile() } + compileSuffix()
    }

    open fun compileSuffix(): String {
        return if (max == min) "{$min}" else if (max == null) "{$min,}" else "{$min,$max}"
    }
}

class GBNFOptionalGroup: GBNFRepeatGroup() {
    override fun compileSuffix() = "?"
}

class GBNFOneOrMoreGroup: GBNFRepeatGroup() {
    override fun compileSuffix() = "+"
}

class GBNFAnyCountGroup: GBNFRepeatGroup() {
    override fun compileSuffix() = "*"
}