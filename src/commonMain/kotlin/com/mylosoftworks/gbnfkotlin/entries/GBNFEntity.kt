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
        // Character count optimization
        when {
            min == 0 && max == 1 -> optional(init)
            min == 0 && max == null -> anyCount(init)
            min == 1 && max == null -> oneOrMore(init)
            else -> addAndInit(GBNFRepeatGroup(min, max), init)
        }
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

    // Operator function based utilities
    /**
     * Adds a literal from a string.
     *
     * Usage: `+"literal"` -> `"literal"`
     */
    operator fun String.unaryPlus() {
        literal(this)
    }

    /**
     * Adds a range from a string.
     *
     * Usage: `-"a-zA-Z"` -> `[a-zA-Z]`
     */
    operator fun String.unaryMinus() {
        range(this)
    }

    /**
     * Adds data so range can understand that it's inverted
     */
    operator fun String.not() = this to true
    operator fun Pair<String, Boolean>.not() = first to !second

    /**
     * Adds a negated range from a string, bool pair.
     *
     * Usage: `-!"a-zA-Z"` -> `[^a-zA-Z]`
     */
    operator fun Pair<String, Boolean>.unaryMinus() {
        range(first, second)
    }

    /**
     * Repeat n times. When number is negative, repeat up to n times.
     *
     * Usage: `5 { +"literal" }` -> `"literal"{5}`
     *
     * Usage: `(-5) { +"literal" }` -> `"literal"{0,5}`
     */
    operator fun Int.invoke(block: GBNFRepeatGroup.() -> Unit) {
        if (this < 0) {
            repeat(0, -this, block)
        }
        else {
            repeat(this, this, block)
        }
    }

    /**
     * Repeat from a to b times.
     *
     * Usage: `(2..5) { +"literal" }` -> `"literal"{2,5}`
     *
     * Usage: `(2..Infinity) { +"literal" }` -> `"literal"{2,}`
     */
    operator fun IntRange.invoke(block: GBNFRepeatGroup.() -> Unit) = repeat(first, last, block)
    /**
     * Repeat from a to b times.
     *
     * Usage: `(2..5) { +"literal" }` -> `"literal"{2,5}`
     *
     * Usage: `(2..Infinity) { +"literal" }` -> `"literal"{2,}`
     */
    operator fun Pair<Int, Int?>.invoke(block: GBNFRepeatGroup.() -> Unit) = repeat(first, second, block)

    /**
     * Make this number indicate that it's a range from the int to infinity.
     *
     * Usage: `(+1) { +"literal" }` -> `"literal"{1,}`
     */
    operator fun Int.rangeTo(inf: Inf) = Pair(this, null)
}

object Inf // Infinity used for pseudo-ranges.