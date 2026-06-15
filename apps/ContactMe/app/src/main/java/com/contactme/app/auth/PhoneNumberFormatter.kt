package com.contactme.app.auth

object PhoneNumberFormatter {
    fun normalizeBangladeshNumber(input: String): String? {
        val trimmed = input.trim()
        val digits = trimmed.filter(Char::isDigit)

        return when {
            trimmed.startsWith("+880") && digits.length == 13 -> "+$digits"
            digits.startsWith("880") && digits.length == 13 -> "+$digits"
            digits.startsWith("0") && digits.length == 11 -> "+880${digits.drop(1)}"
            digits.length == 10 && digits.startsWith("1") -> "+880$digits"
            else -> null
        }
    }
}
