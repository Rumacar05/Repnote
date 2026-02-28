package com.ruma.repnote.core.domain.util

import java.util.Locale

private const val ENGLISH = "en"
private const val SPANISH = "es"
private const val DEFAULT_LANGUAGE = ENGLISH

/**
 * Gets the normalized language code from the device locale.
 *
 * Supports English and Spanish. Any other language defaults to English.
 *
 * @return Language code: "en" for English, "es" for Spanish
 */
fun getNormalizedLanguage(): String {
    val deviceLanguage = Locale.getDefault().language.lowercase()
    return when (deviceLanguage) {
        SPANISH -> SPANISH
        ENGLISH -> ENGLISH
        else -> DEFAULT_LANGUAGE
    }
}
