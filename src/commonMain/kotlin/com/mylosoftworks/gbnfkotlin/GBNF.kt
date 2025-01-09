package com.mylosoftworks.gbnfkotlin

import com.mylosoftworks.gbnfkotlin.entries.GBNFEntity
import com.mylosoftworks.gbnfkotlin.parsing.ParseResult

class GBNF(rules: GBNF.() -> Unit): GBNFEntity("root", null) { // Host is null because this is the host
    val entities: ArrayList<GBNFEntity> = arrayListOf()

    var lastNameId = 0
    fun generateUniqueName(): String {
        return "unnamed${lastNameId++}"
    }

    init {
        rules()
    }

    override fun compile(): String { // Compile all instead of only self
        val builder = StringBuilder()
        val invalidate = rules.isEmpty() || entities.any { it.identifier == "root" }
        // Start with self since super needs to be called
        if (!invalidate) builder.insert(0, super.compile())
        entities.forEach {
            builder.append(it.compile())
        }
        return builder.toString()
    }

    override fun parse(string: String): Result<Pair<ParseResult<*>, String>> {
        val otherRoot = entities.find { it.identifier == "root" }
        val invalidate = rules.isEmpty() || otherRoot != null
        return if (invalidate) otherRoot?.parse(string) ?: Result.failure(RuntimeException("No valid root rule exists.")) else super.parse(string)
    }

    fun entity(name: String? = null, init: GBNFEntity.() -> Unit): GBNFEntity {
        return GBNFEntity(name, this).apply(init).apply {entities.add(this)}
    }

    // Operator function based utilities
    /**
     * Adds a named entity from a string.
     *
     * Usage: `"name" {}`
     *
     * Alternative to `entity("name") {}`
     */
    operator fun String.invoke(block: GBNFEntity.() -> Unit): GBNFEntity {
        return entity(this, block)
    }
}