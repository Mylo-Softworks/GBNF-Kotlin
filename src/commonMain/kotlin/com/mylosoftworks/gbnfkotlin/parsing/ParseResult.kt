package com.mylosoftworks.gbnfkotlin.parsing

import com.mylosoftworks.gbnfkotlin.entries.GBNFEntity
import com.mylosoftworks.gbnfkotlin.rules.GBNFGroup

/**
 * A chunk of the result from a GBNF parse result
 */
data class ParseResult<T>(val strValue: String, val associatedEntry: T?, val descendants: List<ParseResult<*>> = listOf()) {
    fun <Target> descendantsCast() = descendants as List<Target>

    fun find(includeSelf: Boolean = false, deep: Boolean = true, predicate: (ParseResult<*>) -> Boolean): ParseResult<*>? {
        if (includeSelf && predicate(this)) return this

        if (!deep) {
            return descendants.find(predicate)
        }

        descendants.forEach {
            val result = it.find(true, true, predicate)
            if (result != null) return result
        }
        return null
    }

    fun findAll(includeSelf: Boolean = false, deep: Boolean = true, predicate: (ParseResult<*>) -> Boolean): List<ParseResult<*>> {
        val list = mutableListOf<ParseResult<*>>()

        if (includeSelf && predicate(this)) list.add(this)

        if (!deep) {
            list.addAll(descendants.filter(predicate))
            return list
        }

        descendants.forEach {
            val results = it.findAll(true, true, predicate)
            list.addAll(results)
        }

        return list
    }

    fun isNamedEntity(name: String): Boolean {
        val entry = associatedEntry
        if (entry !is GBNFEntity) {
            return false
        }
        return entry.identifier == name
    }

    fun getAsEntityIfPossible(): GBNFEntity? {
        val entry = associatedEntry
        if (entry is GBNFEntity) return entry
        return null
    }

    /**
     * Transform the tree recursively to your custom tree class.
     */
    fun <Target> mapTransform(transform: ParseResult<*>.() -> Target?): Target? {
        return transform(
            ParseResult(
                strValue,
                associatedEntry,
                descendants.mapNotNull { it.mapTransform(transform) } as List<ParseResult<*>> // Use descendants to store the temp values, this cast is safe because of type erasure.
            )
        )
    }

    /**
     * Filters a tree based on a predicate, if an entry is removed, all children will be merged upwards
     */
    fun filter(predicate: (ParseResult<*>) -> Boolean = { it.associatedEntry is GBNFEntity && it.associatedEntry !is GBNFGroup }): List<ParseResult<*>> {
        val mappedDescendants = this.descendants.flatMap { it.filter(predicate) }
        return if (predicate(this)) listOf(ParseResult(this.strValue, this.associatedEntry, mappedDescendants))
            else mappedDescendants
    }

    fun forEach(func: (ParseResult<*>) -> Unit) {
        func(this)
        descendants.forEach { it.forEach(func) }
    }
}