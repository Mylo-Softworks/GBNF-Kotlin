package com.mylosoftworks.gbnfkotlin.interpreting

import com.mylosoftworks.gbnfkotlin.GBNF
import com.mylosoftworks.gbnfkotlin.entries.GBNFEntity
import com.mylosoftworks.gbnfkotlin.parsing.ParseResult
import com.mylosoftworks.gbnfkotlin.rules.GBNFEntityRule
import com.mylosoftworks.gbnfkotlin.rules.GBNFGroupRule
import com.mylosoftworks.gbnfkotlin.rules.GBNFRule
import com.mylosoftworks.gbnfkotlin.unSanitizeGBNFRangeChars
import com.mylosoftworks.gbnfkotlin.unSanitizeGBNFString

object GBNFInterpreter {
    val GBNFGBNF = GBNF { // See gbnf_for_gbnf.txt
        // The basic definitions
        val whitespace = entity("whitespace") { oneOrMore { range(" \r\t") } } // whitespace ::= [ \r\n\t]+ # Characters considered whitespace, the amount doesn't matter
        val whitespaceN = entity("whitespacen") { oneOrMore { range(" \r\n\t") } } // whitespace ::= [ \r\n\t]+ # Characters considered whitespace, the amount doesn't matter
        val identifier = entity("identifier") { oneOrMore { range("a-zA-Z0-9\\-") } } // identifier ::= [a-zA-Z0-9\-]+ # An identifier for a rule definition
        val literalContent = entity("literalcontent") { anyCount { oneOf {
            literal("\\\\")
            literal("\\\"")
            range("\"", true)
        } } } // literalcontent ::= ("\\\\" | "\\\"" | [^\"])* # Continue on escaped quotes
        val rangeContent = entity("rangecontent") { anyCount { oneOf {
            literal("\\\\")
            literal("\\]")
            range("]", true)
        } } } // rangecontent ::= ("\\\\" | "\\]" | [^\]])* # Continue on escaped closing brackets
        val integer = entity("integer") { oneOrMore { range("0-9") } } // integer ::= [0-9]+
        val comment = entity("comment") {
            literal("#")
            anyCount {
                range("\n", true)
            }
        } // comment ::= "#" [^\n]* # Comments can be used pretty much anywhere

        // Modifier rules (should take priority to get parsed properly)
        val optional = entity("optional") { literal("?") } // optional ::= "?"
        val oneOrMore = entity("oneormore") { literal("+") } // oneormore ::= "+"
        val anyCount = entity("anycount") { literal("*") } // anycount ::= "*"
        val comma = entity("comma") { literal(",") } // comma ::= ","
        val countFromTo = entity("countfromto") {
            literal("{")
            integer()
            optional {
                comma()
                optional { integer() }
            }
            literal("}")
        } // countfromto ::= "{" integer (comma integer?)? "}"
        val modifier = entity("modifier") { oneOf {
            optional()
            oneOrMore()
            anyCount()
            countFromTo()
        } } // modifier ::= (optional | oneormore | anycount, countfromto)

        // rules
        val ruleVal = entity("rule") {} // Circular reference, so initialize later (after grouprules)

        val ruleStack = entity("rulestack") {
            ruleVal()
            anyCount {
                whitespace()
                ruleVal()
            }
        } // rulestack ::= rule (whitespace rule)* # Like rule list, but without "|"
        val ruleList = entity("rulelist") rulelist@{
            ruleStack()
            anyCount {
                whitespace()
                literal("|")
                whitespace()
                ruleStack()
            }
        } // rulelist ::= rulestack (whitespace "|" whitespace rulestack)* # The part of rule definitions containing the rules

        val ruleStackNL = entity("rulestack") {
            ruleVal()
            anyCount {
                whitespaceN()
                ruleVal()
            }
        } // rulestacknl ::= rule (whitespacen rule)*
        val ruleListNL = entity("rulelist") rulelist@{
            ruleStackNL()
            anyCount {
                whitespaceN()
                literal("|")
                whitespaceN()
                ruleStackNL()
            }
        } // rulelistnl ::= rulestacknl (whitespacen "|" whitespacen rulestacknl)* # Same as rulelist but with newlines allowed


        val ruleDef = entity("ruledef") {
            optional { comment() }
            identifier()
            whitespace()
            literal("::=")
            whitespace()
            ruleList()
            optional { comment() }
        } // ruledef ::= comment? identifier whitespace "::=" whitespace rulelist comment? # A definition of a rule `name ::= rules`

        // Group rules
        val groupRules = entity("grouprules") {
            literal("(")
            ruleListNL()
            literal(")")
        } // grouprules ::= "(" rulelist ")"

        // Content rules
        val identifierRule = entity("identifierrule") {
            identifier()
        } // identifierrule ::= identifier # an identifier
        val literalRule = entity("literalrule") {
            literal("\"")
            literalContent()
            literal("\"")
        } // literalrule ::= "\"" literalcontent "\""
        val rangeRule = entity("rangerule") {
            literal("[")
            rangeContent()
            literal("]")
        } // rangerule ::= "[" rangecontent "]"
        val contentRules = entity("contentrules") { oneOf {
            rangeRule()
            literalRule()
            identifierRule()
        } } // contentrules ::= (identifierrule | literalrule | rangerule)

        ruleVal.apply { // Initialize later
            oneOf {
                groupRules()
                contentRules()
            }
            optional {
                modifier()
            }
        }

        // Root
        anyCount {
            anyCount {
                oneOf {
                    whitespaceN()
                    comment()
                }
            }
            ruleDef()
        }
        optional { whitespaceN() } // root ::= (whitespacen? comment? whitespacen? ruledef)* whitespacen? # The rule definitions, like `root ::= "bla bla"`
    }

    fun interpretGBNF(gbnf: String): Result<GBNF> {
        val parsed = GBNFGBNF.parse(gbnf).getOrElse { return Result.failure(it) }.first.filter()[0] // Only named entities


        val rules = parsed.findAll(includeSelf = false, deep = false) { it.isNamedEntity("ruledef") } // Find "ruledef ::= identifier whitespace "::=" whitespace rulelist"

        var result: Result<GBNF>? = null

        Result.success(GBNF {
            val lookupTable = hashMapOf<String, GBNFEntity>()
            rules.forEach {
                val name = it.find(deep = false) { it.isNamedEntity("identifier") }!!.strValue
                val contents = it.find(deep = false) { it.isNamedEntity("rulelist") }!!
                lookupTable[name] = entity(name) {
                    interpretEntity(this, contents)
                }
            }

            // Replace scaffolding with real entities (which are now always defined)
            entities.forEach {
                mapScaffold(it, lookupTable).getOrElse { result = Result.failure(it) }
            }

            result = Result.success(this)
        })

        return result!!
    }

    fun interpretEntity(entityBase: GBNFEntity, contents: ParseResult<*>) {
        entityBase.apply {
            when (contents.getAsEntityIfPossible()?.identifier) {
                "rulelist", "rulelistnl" -> { // rulelist ::= rulestack (whitespace "|" whitespace rulestack)*
                    // If another rulelist is directly contained, that means this is a oneOf group
                    val stacks = contents.findAll(deep = false) { it.isNamedEntity("rulestack") }
                    if (stacks.count() == 1) { // Not a oneOf group (|)
                        // Simply parse rulestack on this level
                        interpretEntity(this, stacks[0])
                    }
                    else { // OneOf group
                        oneOf {
                            for (ent in stacks) {
                                interpretEntity(this, ent)
                            }
                        }
                    }
                }
                "rulestack", "rulestacknl" -> { // rulestack ::= rule (whitespace rule)*
                    // Add every rule inside entityBase directly (as groups are already handled by grouprules)
                    val rules = contents.findAll(deep = false) { it.isNamedEntity("rule") } // Get all rules
                    for (rule in rules) {
                        interpretEntity(this, rule) // Add rule directly, now can be parsed on next step as "rule"
                    }
                }
                "rule" -> { // rule ::= (grouprules | contentrules) modifier?
                    // If a modifier is found, wrap it around whichever rule is used, then send that rule down again
                    val rule = contents.descendants[0]
                    val modifierOpt = contents.find(deep = false) { it.isNamedEntity("modifier") }

                    if (modifierOpt == null) { // No modifier, don't add modifier
                        interpretEntity(this, rule) // parse grouprules | contentrules
                    }
                    else {
                        val actualMod = modifierOpt.descendants[0]
                        when (actualMod.getAsEntityIfPossible()!!.identifier) { // modifier ::= (optional | oneormore | anycount | countfromto)
                            "optional" -> { // optional ::= "?"
                                optional {
                                    interpretEntity(this, rule)
                                }
                            }
                            "oneormore" -> { // oneormore ::= "+"
                                oneOrMore {
                                    interpretEntity(this, rule)
                                }
                            }
                            "anycount" -> { // anycount ::= "*"
                                anyCount {
                                    interpretEntity(this, rule)
                                }
                            }
                            "countfromto" -> { // countfromto ::= "{" integer (comma integer?)? "}"
                                /*
                                    From https://github.com/ggerganov/llama.cpp/blob/master/grammars/README.md
                                    {m} repeats the precedent symbol or sequence exactly m times
                                    {m,} repeats the precedent symbol or sequence at least m times
                                    {m,n} repeats the precedent symbol or sequence at between m and n times (included)
                                    {0,n} repeats the precedent symbol or sequence at most n times (included)
                                 */
                                val givenNumbers = actualMod.findAll(deep = false) { it.isNamedEntity("integer") } // 1 or 2 entries
                                val commaProvided = actualMod.find(deep = false) { it.isNamedEntity("comma") } != null // Has a comma

                                val startNumber = givenNumbers[0].strValue.toInt()
                                if (givenNumbers.size == 1) {
                                    if (commaProvided) { // {5,}
                                        repeat(startNumber, null) {
                                            interpretEntity(this, rule)
                                        }
                                    }
                                    else { // {5}
                                        repeat(startNumber) {
                                            interpretEntity(this, rule)
                                        }
                                    }
                                }
                                else { // 2 numbers given
                                    val endNumber = givenNumbers[1].strValue.toInt()
                                    repeat(startNumber, endNumber) {
                                        interpretEntity(this, rule)
                                    }
                                }
                            }
                        }
                    }
                }
                "grouprules" -> { // grouprules ::= "(" rulelistnl ")"
                    // Group these entries
                    group {
                        interpretEntity(this, contents.descendants[0])
                    }
                }

                "contentrules" -> { // contentrules ::= (rangerule | literalrule | identifierrule)
                    // Take whatever is in this and pass it down, we don't care which one it is
                    interpretEntity(this, contents.descendants[0])
                }
                "rangerule" -> { // rangerule ::= "[" rangecontent "]"
                    // Parse the range rule
                    val content = contents.descendants[0].strValue
                    if (content.startsWith("^")) { // Inverted
                        range(unSanitizeGBNFRangeChars(content.substring(1)), true)
                    }
                    else {
                        range(unSanitizeGBNFRangeChars(content))
                    }
                }
                "literalrule" -> { // literalrule ::= "\"" literalcontent "\""
                    // Parse the literal rule
                    val content = contents.descendants[0].strValue
                    literal(unSanitizeGBNFString(content))
                }
                "identifierrule" -> { // identifierrule ::= identifier
                    // Insert a placeholder
                    scaffoldEntity(contents.descendants[0].strValue)
                }
                else -> error("Unimplemented identifier: ${contents.getAsEntityIfPossible()?.identifier}")
            }
        }
    }

    fun GBNFEntity.scaffoldEntity(name: String) {
        this.rules.add(ScaffoldingEntityRule(name))
    }

    class ScaffoldingEntityRule(val name: String): GBNFRule() {
        override fun compile(): String = error("Scaffolding is a placeholder")
        override fun parse(string: String): Result<Pair<ParseResult<*>, String>> = error("Scaffolding is a placeholder")
    }

    fun mapScaffold(entity: GBNFEntity, lookup: HashMap<String, GBNFEntity>): Result<Unit> {
        val newRules = entity.rules.map {
            if (it is GBNFGroupRule) {
                mapScaffold(it.group, lookup)
            }
            else if (it is ScaffoldingEntityRule) {
                return@map GBNFEntityRule(lookup.getOrElse(it.name) { return Result.failure(UndefinedEntityException("Rule ${it.name} is not defined anywhere in this grammar.")) }) // Fill in the scaffolding
            }

            return@map it
        }

        entity.rules.clear()
        newRules.forEach { entity.rules.add(it) }

        return Result.success(Unit)
    }

    class UndefinedEntityException(message: String) : RuntimeException(message)
}