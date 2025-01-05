package com.mylosoftworks

import com.mylosoftworks.gbnfkotlin.GBNF
import com.mylosoftworks.gbnfkotlin.interpreting.GBNFInterpreter

// https://github.com/ggerganov/llama.cpp/blob/master/grammars/README.md

fun main() {
//    testParsing()

//    println(GBNF {
//        val whitespace = entity("whitespace") { oneOrMore { range(" \r\n\t") } } // whitespace ::= [ \r\n\t]+ # Characters considered whitespace, the amount doesn't matter
//        val identifier = entity("identifier") { oneOrMore { range("a-zA-Z\\-") } } // identifier ::= [a-zA-Z\-]+ # An identifier for a rule definition
//        val literalContent = entity("literalcontent") { anyCount { oneOf {
//            literal("\\\\")
//            literal("\\\"")
//            range("\"", true)
//        } } } // literalcontent ::= ("\\\\" | "\\\"" | [^\"])* # Continue on escaped quotes
//        val rangeContent = entity("rangecontent") { anyCount { oneOf {
//            literal("\\\\")
//            literal("\\]")
//            range("]", true)
//        } } } // rangecontent ::= ("\\\\" | "\\]" | [^\]])* # Continue on escaped closing brackets
//        val integer = entity("integer") { oneOrMore { range("0-9") } } // integer ::= [0-9]+
//
//        literal("\"")
//
//        literal("\"")
//    }.parseOrThrow("""
//        ""
//    """.trimIndent()).second)

    println(GBNFInterpreter.GBNFGBNF.compile())

    val result = GBNFInterpreter.GBNFGBNF.parseOrThrow("""
        test ::= needle needle2
    """.trimIndent())

//    println(result.second) // Print remaining
//    println(result.first.filter())
//    println(result.first.find { it.isNamedEntity("rulestack") })
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