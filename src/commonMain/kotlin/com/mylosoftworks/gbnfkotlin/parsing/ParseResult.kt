package com.mylosoftworks.gbnfkotlin.parsing

import com.mylosoftworks.gbnfkotlin.CompilableParsable
import com.mylosoftworks.gbnfkotlin.entries.GBNFEntity

/**
 * A chunk of the result from a GBNF parse result
 */
class ParseResult(val strValue: String, val associatedEntry: CompilableParsable, val descendants: List<ParseResult> = listOf()) {
    fun find(includeSelf: Boolean = false, deep: Boolean = true, predicate: (ParseResult) -> Boolean): ParseResult? {
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

    fun findAllWhere(includeSelf: Boolean = false, deep: Boolean = true, predicate: (ParseResult) -> Boolean): List<ParseResult> {
        val list = mutableListOf<ParseResult>()

        if (includeSelf && predicate(this)) list.add(this)

        if (!deep) {
            list.addAll(descendants.filter(predicate))
            return list
        }

        descendants.forEach {
            val results = it.findAllWhere(true, true, predicate)
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
}