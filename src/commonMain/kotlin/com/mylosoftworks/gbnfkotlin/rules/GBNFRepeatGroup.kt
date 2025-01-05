package com.mylosoftworks.gbnfkotlin.rules

import com.mylosoftworks.gbnfkotlin.parsing.GBNFParseError
import com.mylosoftworks.gbnfkotlin.parsing.ParseResult

open class GBNFRepeatGroup(val min: Int = 0, val max: Int? = min): GBNFGroup() {
    override fun compile(): String {
        return super.compile() + compileSuffix()
    }

    open fun compileSuffix(): String {
        return if (max == min) "{$min}" else if (max == null) "{$min,}" else "{$min,$max}"
    }

    override fun parse(string: String): Result<Pair<ParseResult<*>, String>> {
        // Try to match as many times as possible (as many as allowed), if unable to match enough times, return null.

        var hitCount = 0

        var stringRemainder = string
        val subMatches = mutableListOf<ParseResult<*>>()
        var forceBreak = false // Since break isn't possible from within getOrElse.

        while (!forceBreak && (max == null || hitCount < max)) {
            super.parse(stringRemainder).fold({
                val (result, remainder) = it
                stringRemainder = remainder // Since parsing moves forwards

                subMatches.add(result) // Add the results to the parsed classes list
                hitCount++
            }, {
                forceBreak = true // Prevents the loop from continuing at the next iteration
            }) // If this entity failed to parse, there's no reason to continue.
        }

//        if (debugName == "rulestack repeat") { // TODO: Remove debug print
//            println(hitCount)
//            println(subMatches[0])
//        }

        if (hitCount < min) return Result.failure(GBNFParseError("Not enough hits for match:\n$stringRemainder")) // Not enough hits, match is not valid

        return Result.success(ParseResult(
            subMatches.joinToString("") { it.strValue },
            this,
            subMatches
        ) to stringRemainder)
    }
}

class GBNFOptionalGroup: GBNFRepeatGroup(0, 1) {
    override fun compileSuffix() = "?"
}

class GBNFOneOrMoreGroup: GBNFRepeatGroup(1, null) {
    override fun compileSuffix() = "+"
}

class GBNFAnyCountGroup: GBNFRepeatGroup(0, null) {
    override fun compileSuffix() = "*"
}