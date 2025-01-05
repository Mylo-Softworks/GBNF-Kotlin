package com.mylosoftworks.gbnfkotlin.interpreting

import com.mylosoftworks.gbnfkotlin.GBNF
import com.mylosoftworks.gbnfkotlin.entries.GBNFEntity
import com.mylosoftworks.gbnfkotlin.parsing.ParseResult
import com.mylosoftworks.gbnfkotlin.rules.GBNFEntityRule
import com.mylosoftworks.gbnfkotlin.rules.GBNFRule

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
        // TODO: Strip comments (comments are content which starts with "#" and ends with a newline)
        val parsed = GBNFGBNF.parse(gbnf).getOrElse { return Result.failure(it) }.first.filter()[0] // Only named entities

//        parsed.forEach {
//            println("Children: ${it.descendants.size}, Identifier: ${it.getAsEntityIfPossible()?.identifier}, content: ${it.strValue}")
//        }

        val rules = parsed.findAll(includeSelf = false, deep = false) { it.isNamedEntity("ruledef") } // Find "ruledef ::= identifier whitespace "::=" whitespace rulelist"
        println(rules.joinToString("\n") { "Rule: ${it.strValue}, name: ${it.descendants[0].strValue}" })

        var result: Result<GBNF>? = null

        Result.success(GBNF {
            val lookupTable = hashMapOf<String, GBNFEntity>()
            rules.forEach {
                val name = it.descendants[0].strValue // identifier
                val contents = it.descendants[4] // rulelist
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
            // TODO: Read contents to parse the rules, entities use ScaffoldingEntityRule (see below) which is able to put a placeholder for
        }
    }

    fun GBNFEntity.scaffoldEntity(name: String) {
        this.rules.add(ScaffoldingEntityRule(name))
    }

    class ScaffoldingEntityRule(val name: String): GBNFRule() {
        override fun compile(): String = error("Scaffolding is a placeholder")
        override fun parse(string: String): Result<Pair<ParseResult<*>, String>> = error("Scaffolding is a placeholder")
    }

    fun mapScaffold(entity: GBNFEntity, lookup: HashMap<String, GBNFEntity>, completed: MutableList<String> = mutableListOf()): Result<Unit> {
        entity.rules.map {
            if (it is GBNFEntityRule && it.entity.identifier !in completed) {
                completed += it.entity.identifier!!
                mapScaffold(it.entity, lookup, completed)
            }
            else if (it is ScaffoldingEntityRule) {
                return@map lookup.getOrElse(it.name) { return Result.failure(UndefinedEntityException("Rule ${it.name} is not defined anywhere in this grammar.")) } // Fill in the scaffolding
            }

            return@map it
        }

        return Result.success(Unit)
    }

    class UndefinedEntityException(message: String) : RuntimeException(message)
}