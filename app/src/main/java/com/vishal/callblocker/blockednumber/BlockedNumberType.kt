package com.vishal.callblocker.blockednumber

enum class BlockedNumberType private constructor(val displayText: String) {
    EXACT_MATCH("Exact match"),
    REGEX_MATCH("Prefix match")
}
