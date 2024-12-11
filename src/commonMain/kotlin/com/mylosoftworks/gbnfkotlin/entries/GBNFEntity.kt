package com.mylosoftworks.com.mylosoftworks.gbnfkotlin.entries

import com.mylosoftworks.com.mylosoftworks.gbnfkotlin.GBNF
import com.mylosoftworks.com.mylosoftworks.gbnfkotlin.rules.*

open class GBNFEntity(identifier: String?, val host: GBNF?): GBNFEntry() {
    val rules: ArrayList<GBNFRule> = arrayListOf()

    var identifier: String? = identifier
        get() {
            if (field == null) field = host!!.generateUniqueName()
            return field
        }
        private set

    override fun compile(): String {
        return "$identifier ::= ${rules.joinToString(" ") { it.compile() }}\n"
    }

    /**
     * Insert this entity as a rule
     */
    operator fun GBNFEntity.invoke() {
        val parent = this@GBNFEntity // Find out where this is called from
        parent.rules.add(GBNFEntityRule(this@invoke))
    }

    // Groups

    fun group(init: GBNFGroup.() -> Unit) {
        addAndInit(GBNFGroup(), init)
    }

    fun oneOf(init: GBNFGroup.() -> Unit) {
        addAndInit(GBNFAlternativeGroup(), init)
    }

    fun repeat(min: Int = 0, max: Int? = min, init: GBNFGroup.() -> Unit) {
        addAndInit(GBNFRepeatGroup(min, max), init)
    }

    fun optional(init: GBNFGroup.() -> Unit) {
        addAndInit(GBNFOptionalGroup(), init)
    }

    fun oneOrMore(init: GBNFGroup.() -> Unit) {
        addAndInit(GBNFOneOrMoreGroup(), init)
    }

    fun anyCount(init: GBNFGroup.() -> Unit) {
        addAndInit(GBNFAnyCountGroup(), init)
    }

    // Content

    fun literal(content: String) {
        rules.add(GBNFLiteralRule(content))
    }

    fun range(rangeDef: String, negate: Boolean = false) {
        rules.add(GBNFRangeRule(rangeDef, negate))
    }

    private fun addAndInit(group: GBNFGroup, init: GBNFGroup.() -> Unit) {
        rules.add(group.apply(init).rule())
    }
}