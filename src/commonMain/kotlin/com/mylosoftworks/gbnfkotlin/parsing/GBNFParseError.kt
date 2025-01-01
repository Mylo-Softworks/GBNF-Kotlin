package com.mylosoftworks.gbnfkotlin.parsing

internal fun gbnfParseError(message: String? = null): Nothing = throw GBNFParseError(message)
open class GBNFParseError(message: String? = null): RuntimeException(message)