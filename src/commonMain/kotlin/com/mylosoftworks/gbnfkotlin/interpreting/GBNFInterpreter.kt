package com.mylosoftworks.gbnfkotlin.interpreting

import com.mylosoftworks.gbnfkotlin.GBNF

object GBNFInterpreter {
    val GBNFGBNF = GBNF { // See gbnf_for_gbnf.txt
        // The basic definitions
        val whitespace = entity("whitespace") { oneOrMore { range(" \r\n\t") } } // whitespace ::= [ \r\n\t]+ # Characters considered whitespace, the amount doesn't matter
        val identifier = entity("identifier") { oneOrMore { range("a-zA-Z\\-") } } // identifier ::= [a-zA-Z\-]+ # An identifier for a rule definition
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
        val rule = entity("rule") {} // Circular reference, so initialize later (after grouprules)

        val ruleStack = entity("rulestack") {
            rule()
            anyCount {
                debugName = "rulestack repeat"
                whitespace()
                rule()
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
            ruleList()
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
            identifierRule()
            literalRule()
            rangeRule()
        } } // contentrules ::= (identifierrule | literalrule | rangerule)

        rule.apply { // Initialize later
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
            optional { whitespace() }
            ruleDef()
        }
        optional { whitespace() } // root ::= (whitespace? ruledef)* whitespace? # The rule definitions, like `root ::= "bla bla"`
    }

    fun interpretGBNF(gbnf: String): GBNF {

        TODO()
    }
}