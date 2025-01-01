package com.mylosoftworks.gbnfkotlin

import com.mylosoftworks.gbnfkotlin.entries.GBNFEntity

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
        // Start with self since super needs to be called
        builder.append(super.compile())
        entities.reversed().forEach {
            builder.append(it.compile())
        }
        return builder.toString()
    }

    fun entity(name: String? = null, init: GBNFEntity.() -> Unit): GBNFEntity {
        return GBNFEntity(name, this).apply(init).apply {entities.add(this)}
    }
}