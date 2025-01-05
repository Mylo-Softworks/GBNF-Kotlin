package com.mylosoftworks.gbnfkotlin.interpreting

import com.mylosoftworks.gbnfkotlin.GBNF

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

        // Modifier rules (should take priority to get parsed properly)
        val optional = entity("optional") { literal("?") } // optional ::= "?"
        val oneOrMore = entity("oneormore") { literal("+") } // oneormore ::= "+"
        val anyCount = entity("anycount") { literal("*") } // anycount ::= "*"
        val countFromTo = entity("countfromto") {
            literal("{")
            integer()
            optional {
                group {
                    literal(",")
                    optional { integer() }
                }
            }
            literal("}")
        } // countfromto ::= "{" integer ("," integer?)? "}"
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
            optional {
                whitespace()
                literal("|")
                whitespace()
                this@rulelist() // rulelist, recursive
            }
        } // rulelist ::= rulestack (whitespace "|" whitespace rulelist)? # The part of rule definitions containing the rules

        val ruleStackNL = entity("rulestack") {
            ruleVal()
            anyCount {
                whitespaceN()
                ruleVal()
            }
        } // rulestacknl ::= rule (whitespacen rule)*
        val ruleListNL = entity("rulelist") rulelist@{
            ruleStackNL()
            optional {
                whitespaceN()
                literal("|")
                whitespaceN()
                this@rulelist() // rulelist, recursive
            }
        } // rulelistnl ::= rulestacknl (whitespacen "|" whitespacen rulelist)? # Same as rulelist but with newlines allowed


        val ruleDef = entity("ruledef") {
            identifier()
            whitespace()
            literal("::=")
            whitespace()
            ruleList()
        } // ruledef ::= identifier whitespace "::=" whitespace rulelist # A definition of a rule `name ::= rules`

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
            optional { whitespaceN() }
            ruleDef()
        }
        optional { whitespaceN() } // root ::= (whitespace? ruledef)* whitespace? # The rule definitions, like `root ::= "bla bla"`
    }

    fun interpretGBNF(gbnf: String): Result<GBNF> {
        val parsed = GBNFGBNF.parse(gbnf).getOrElse { return Result.failure(it) }.first.filter()[0] // Only named entities

//        parsed.forEach {
//            println("Children: ${it.descendants.size}, Identifier: ${it.getAsEntityIfPossible()?.identifier}, content: ${it.strValue}")
//        }

        val rules = parsed.findAll(includeSelf = false, deep = false) { it.isNamedEntity("ruledef") } // Find "ruledef ::= identifier whitespace "::=" whitespace rulelist"
        println(rules.size)
        println(rules.joinToString("\n") { "Rule: " + it.strValue })

        TODO()
    }
}