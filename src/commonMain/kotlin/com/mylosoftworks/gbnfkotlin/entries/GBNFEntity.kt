package com.mylosoftworks.gbnfkotlin.entries

import com.mylosoftworks.gbnfkotlin.GBNF
import com.mylosoftworks.gbnfkotlin.parsing.ParseResult
import com.mylosoftworks.gbnfkotlin.rules.*

open class GBNFEntity(identifier: String?, val host: GBNF?): GBNFEntry() {
    val rules: ArrayList<GBNFRule> = arrayListOf()

    var identifier: String? = identifier
        get() {
            if (field == null) field = host?.generateUniqueName()
            return field
        }
        private set

    override fun compile(): String {
        return "$identifier ::= ${rules.joinToString(" ") { it.compile() }}\n"
    }

    override fun parse(string: String): Result<Pair<ParseResult<*>, String>> {
        var stringRemainder = string
        val subMatches = mutableListOf<ParseResult<*>>()
        rules.forEach {
            val (result, remainder) = it.parse(stringRemainder).getOrElse { return Result.failure(it) } // If anything in this entity fails to parse, that means this entity failed to parse.
            stringRemainder = remainder // Since parsing moves forwards

            subMatches.add(result) // Add the results to the parsed classes list
        }
        return Result.success(ParseResult(
            subMatches.joinToString("") { it.strValue },
            this,
            subMatches
        ) to stringRemainder)
    }

    /**
     * Insert this entity as a rule
     */
    operator fun GBNFEntity.invoke() {
        val parent = this@GBNFEntity // Find out where this is called from
        parent.rules.add(GBNFEntityRule(this@invoke))
    }

    fun entity(entity: GBNFEntity) {
        val parent = this
        parent.rules.add(GBNFEntityRule(entity))
    }

    // Groups

    fun group(init: GBNFGroup.() -> Unit) {
        addAndInit(GBNFGroup(), init)
    }

    fun oneOf(init: GBNFAlternativeGroup.() -> Unit) {
        addAndInit(GBNFAlternativeGroup(), init)
    }

    fun repeat(min: Int = 0, max: Int? = min, init: GBNFRepeatGroup.() -> Unit) {
        addAndInit(GBNFRepeatGroup(min, max), init)
    }

    fun optional(init: GBNFOptionalGroup.() -> Unit) {
        addAndInit(GBNFOptionalGroup(), init)
    }

    fun oneOrMore(init: GBNFOneOrMoreGroup.() -> Unit) {
        addAndInit(GBNFOneOrMoreGroup(), init)
    }

    fun anyCount(init: GBNFAnyCountGroup.() -> Unit) {
        addAndInit(GBNFAnyCountGroup(), init)
    }

    // Content

    fun literal(content: String) {
        rules.add(GBNFLiteralRule(content))
    }

    fun range(rangeDef: String, negate: Boolean = false) {
        rules.add(GBNFRangeRule(rangeDef, negate))
    }

    private fun <T : GBNFGroup> addAndInit(group: T, init: T.() -> Unit) {
        rules.add(group.apply(init).rule())
    }
}