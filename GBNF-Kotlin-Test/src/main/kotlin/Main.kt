package com.mylosoftworks

import com.mylosoftworks.gbnfkotlin.GBNF
import com.mylosoftworks.gbnfkotlin.entries.Inf

// https://github.com/ggerganov/llama.cpp/blob/master/grammars/README.md

fun main() {
//    testParsing()

    testShortCuts()
}

fun testShortCuts() {
    val gbnf = GBNF {
        val testEntity = entity("entity") {
            +"literal"
        }
        val anotherEntity = "another" {
            +"literal"
        }

        +"literal"
        testEntity()
        anotherEntity()

        // Range
        -"a-z"

        // Negated range
        -!"a-z"

        // Range, but negated and back
        -!!"a-z"

        // Repeat 5 times
        5 {
            +"literal"
        }

        // Repeat at least 2 times
        (2..Inf) {
            +"literal"
        }

        // Repeat at most 2 times
        (-2) {
            +"literal"
        }
    }
    println(gbnf.compile())
}

fun testInterpreting() {
//    val test = GBNF {
//        //[a-zA-Z0-9\-]+
//
//        oneOrMore { range("a-zA-Z0-9\\-") }
//    }
//
//    val result = test.parseOrThrow("""
//        ::=
//    """.trimIndent())
//
//    println(result.first.strValue)

//    val interpreted = GBNFInterpreter.interpretGBNF("""
//        # This GBNF represents GBNF itsself, and is written in DSL syntax for the interpreter.
//        # See com.mylosoftworks.gbnfkotlin.interpreting.GBNFInterpreter.kt
//
//        root ::= ((whitespacen | comment)* whitespacen? ruledef)* whitespacen? # The rule definitions, like `root ::= "bla bla"`
//
//        # The basic definitions
//        whitespace ::= [ \r\t]+ # Characters considered whitespace, the amount doesn't matter
//        whitespacen ::= [ \r\n\t]+ # Characters considered whitespace, the amount doesn't matter
//        identifier ::= [a-zA-Z0-9\-]+ # An identifier for a rule definition
//        literalcontent ::= ("\\\\" | "\\\"" | [^"])* # Continue on escaped quotes
//        rangecontent ::= ("\\\\" | "\\]" | [^\]])* # Continue on escaped closing brackets
//        integer ::= [0-9]+
//        comment ::= "#" [^\n]* # Comments can be used pretty much anywhere
//
//        ruledef ::= comment? identifier whitespace "::=" whitespace rulelist comment? # A definition of a rule `name ::= rules`
//        rulelist ::= rulestack (whitespace "|" whitespace rulestack)* # The part of rule definitions containing the rules
//        rulelistnl ::= rulestacknl (whitespacen "|" whitespacen rulestacknl)* # Same as rulelist but with newlines allowed
//        rulestack ::= rule (whitespace rule)* # Like rule list, but without "|"
//        rulestacknl ::= rule (whitespacen rule)*
//
//        rule ::= (grouprules | contentrules) modifier? # A single rule with an optional modifier
//
//        # Possible rules
//        contentrules ::= (rangerule | literalrule | identifierrule)
//        identifierrule ::= identifier # an identifier
//        literalrule ::= "\"" literalcontent "\""
//        rangerule ::= "[" rangecontent "]"
//
//        # Modifier rules (should take priority to get parsed properly)
//        modifier ::= (optional | oneormore | anycount | countfromto)
//        optional ::= "?"
//        oneormore ::= "+"
//        anycount ::= "*"
//        comma ::= ","
//        countfromto ::= "{" integer (comma integer?)? "}"
//
//        # Group rules
//        grouprules ::= "(" rulelistnl ")"
//    """.trimIndent()).getOrThrow()
//
//    println(interpreted.compile())

//    // ("\\\\" | "\\\"" | [^\"])*
//    val result = GBNFInterpreter.interpretGBNF("""
//        root ::= (whitespace? ruledef)* whitespace?
//
//        whitespace ::= [ \r\n\t]+
//        identifier ::= [a-zA-Z0-9\-]+
//        literalcontent ::= ("\\\\" | "\\\"" | [^"])*
//        rangecontent ::= ("\\\\" | "\\]" | [^\]])*
//        integer ::= [0-9]+
//
//        ruledef ::= identifier whitespace "::=" whitespace rulelist
//        rulelist ::= rulestack (whitespace "|" whitespace rulelist)?
//        rulestack ::= rule (whitespace rule)*
//
//        rule ::= (grouprules | contentrules) modifier?
//
//        contentrules ::= (rangerule | literalrule | identifierrule)
//        identifierrule ::= identifier
//        literalrule ::= "\"" literalcontent "\""
//        rangerule ::= "[" rangecontent "]"
//
//        modifier ::= (optional | oneormore | anycount | countfromto)
//        optional ::= "?"
//        oneormore ::= "+"
//        anycount ::= "*"
//        countfromto ::= "{" integer ("," integer?)? "}"
//
//        grouprules ::= "(" rulelist ")"
//    """.trimIndent()).getOrThrow()
//
//    println(result.compile())
}

fun testParsing() {
    val gbnf = GBNF {
        val klass = entity("class") {
            oneOrMore {
                range("a-zA-Z")
            }
        }

        val klass2 = entity("class2") {
            literal("alpha")
            anyCount {
                range("a-zA-Z")
            }
        }

        val klass3 = entity("class3") {
            literal("beta")
        }

        literal("test ")
        klass()
        literal(" ")
        oneOf {
            klass2()
            klass3()
        }
    }

    val parsed = gbnf.parseOrThrow("test secret alphabet").first

    println(parsed.find {
        it.isNamedEntity("class") // Alternatively, just check if it.associatedEntry == klass from earlier
    }?.strValue) // output: secret
    println(parsed.descendants[3].strValue) // output: alphabet

    data class TestClass(val content: String, val children: List<TestClass>)
    val converted = parsed.mapTransform { TestClass(strValue, descendantsCast()) }
    println(converted)
}

fun example1() {
    val example = GBNF {
        val boolean = entity("boolean") {
            oneOf {
                literal("True")
                literal("False")
            }
        }

        val opinion = entity("opinion") {
            literal("I think ")
            boolean()
            oneOf {
                literal("is pretty cool")
                literal("sucks")
            }
            literal("!")
        }

        literal("The statement is ")
        boolean()
        literal(". Now I will give a list of booleans: ")
        oneOrMore { boolean() }
        literal(". Also, ")
        opinion()
    }
    println(example.compile())
}

fun rolePlaySyntax() {
    val rolePlaySyntax = GBNF {
        val content = entity("content") {
            oneOrMore {
                range("*\"", true)
            }
        }
        val actions = entity("actions") {
            literal("*")
            content()
            literal("*")
        }
        val quotes = entity("quotes") {
            literal("\"")
            content()
            literal("\"")
        }

        oneOf {
            actions()
            quotes()
        }

        anyCount {
            oneOf {
                literal(" ")
                literal("\t")
                literal("\n")
            }
            actions()
            quotes()
        }
    }

    println(rolePlaySyntax.compile())
}