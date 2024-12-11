package com.mylosoftworks.com.mylosoftworks.gbnfkotlin

/**
 * Sanitize a string for literal in GBNF
 */
fun sanitizeGBNFString(input: String): String {
    return input
        .replace("\"", "\\").replace("\n", "\\n")
        .replace("\t", "\\t").replace("\\", "\\\\")
}