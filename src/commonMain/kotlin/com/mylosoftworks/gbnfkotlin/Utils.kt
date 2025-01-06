package com.mylosoftworks.gbnfkotlin

/**
 * Sanitize a string for literal in GBNF
 */
fun sanitizeGBNFString(input: String): String {
    return input.replace("\\", "\\\\").replace("\"", "\\\"")
        .replace("\n", "\\n").replace("\t", "\\t").replace("\r", "\\r")
}

fun sanitizeGBNFRangeChars(input: String): String {
    return input.replace("\\", "\\\\").replace("\"", "\\\"")
        .replace("\\^", "^").replace("^", "\\^").replace("]", "\\]")
        .replace("\n", "\\n").replace("\t", "\\t").replace("\r", "\\r")
}

/**
 * Reverse of sanitizeGBNFString, turns \\n into \n for example. Used in interpreting.
 */
fun unSanitizeGBNFString(input: String): String {
    return input.replace("\\r", "\r").replace("\\t", "\t").replace("\\n", "\n")
        .replace("\\\"", "\"").replace("\\\\", "\\")
}

fun unSanitizeGBNFRangeChars(input: String): String {
    return input.replace("\\r", "\r").replace("\\t", "\t").replace("\\n", "\n")
        .replace("\\]", "]").replace("\\^", "^")
        .replace("\\\"", "\"").replace("\\\\", "\\")
}