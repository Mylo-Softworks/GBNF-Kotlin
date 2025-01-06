[![](https://www.jitpack.io/v/Mylo-Softworks/GBNF-Kotlin.svg)](https://www.jitpack.io/#Mylo-Softworks/GBNF-Kotlin)

# GBNF Kotlin
A Kotlin multiplatform library for anything GBNF.

Features:
* DSL for writing GBNF grammars in kotlin
* Interpreter for loading GBNF grammars to the format as if written in the DSL
* Parsing of GBNF grammars
  * Functions for converting parsed trees to a custom tree format.
  * Functions for searching in and filtering trees

## What is this library for?
Originally, GBNF Kotlin was supposed to just be a DSL for writing GBNF grammars in kotlin, to be used with [KotLLMs].  
Then, parsing was added, originally meant for defining a grammar for function calling in [KotLLMs], parsing was expanded with utility functions.  
Since GBNF is effectively a superset of BNF, it can be used to describe languages, in fact, you can use GBNF to describe GBNF, as seen in [gbnf_for_gbnf.gbnf](gbnf_for_gbnf.gbnf).  
Because of this, it is now possible to write a parser for GBNF inside of GBNF Kotlin, so I wrote one, and with it, I added support for parsing GBNF from text to the DSL object. So existing grammars are also valid and usable.

You might be wondering, what can this library be used for? On the surface, the features could sound confusing, it's essentially a library for using GBNF, and a helper library for creating GBNF grammars programatically.  
Usages:
* Writing an LLM grammar, and reading the result back. (As seen in [KotLLMs])
* Writing a parser for a computer language (not exclusively programming languages), for parsing from text to an [abstract syntax tree]. (As seen in the GBNFInterpreter)

## Usage

### DSL
Example: "I'm having a great day!"
```kotlin
package example

import com.mylosoftworks.com.mylosoftworks.gbnfkotlin.GBNF

fun main() {
    val example = GBNF {
        literal("I'm having a ")
        oneOf {
            literal("terrible")
            literal("bad")
            literal("okay")
            literal("good")
            literal("great")
        }
        literal(" day!")
    }
    
    // Print the resulting GBNF code
    println(example.compile())
}
```
Result:
```bnf
root ::= "I'm having a " ("terrible" | "bad" | "okay" | "good" | "great") " day!"
```

## Features
GBNF Kotlin supports all features described in [the llama.cpp grammars documentation](https://github.com/ggerganov/llama.cpp/blob/master/grammars/README.md) except for comments.

### Terminal rules
Terminal rules are rules which specifically describe allowed characters

Literal strings can be defined like this:
```kotlin
literal("Content")
// "Content"
```

Characters and rages can be defined like this:
```kotlin
// In order to allow the letters a-z
range("a-z")
// [a-z]

// In order to allow everything except "y"
range("y", true)
// [^y]
```

### Non-terminal rules
Entities (predefined nonterminal rules) can be defined like this:
```kotlin
val entity = entity("entity") { // Name is optional
    literal("This is an entity.")
}

// To then use this entity as a nonterminal rule, simply invoke it:
entity()

// Result when compile() is called:

// root ::= entity
// entity ::= "This is an entity."
```

Groups are used to group rules together, they are like entities, but non-reusable
```kotlin
group {
    literal("This is an entity.")
    literal("This is another entity.")
}

// ("This is an entity." "This is another entity.")
```

Alternatives (one of) are used to provide multiple options that can be matched individually
```kotlin
oneOf {
    literal("This is an entity.")
    literal("This is another entity.")
}

// ("This is an entity." | "This is another entity.")
```

Repeat is used to mark a group as repeating a certain amount of times
```kotlin
repeat(5) { // Repeat 5 times exactly
    literal("This is an entity.")
}
// ("This is an entity."){5,5}

repeat(max=5) { // Repeat between 0 and 5 times
    literal("This is an entity.")
}
// ("This is an entity."){0,5}

repeat(1, 5) { // Repeat between 1 and 5 times
    literal("This is an entity.")
}
// ("This is an entity."){1,5}

repeat(5, null) { // Repeat at least 5 times
    literal("This is an entity.")
}
// ("This is an entity."){5,}
```

For some types of repeat, there are alternative functions.
```kotlin
optional {
    literal("This is an entity.")
}
// ("This is an entity.")?

oneOrMore {
    literal("This is an entity.")
}
// ("This is an entity.")+

anyCount {
    literal("This is an entity.")
}
// ("This is an entity.")*
```

[KotLLMs]: https://github.com/Mylo-Softworks/KotLLMs
[abstract syntax tree]: https://en.wikipedia.org/wiki/Abstract_syntax_tree