# GBNF Kotlin
A Kotlin DSL for writing GBNF LLM grammars.

## Usage
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