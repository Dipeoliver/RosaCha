package com.clausfonseca.rosacha.utils.mask

// E-mail Mask
fun String.validateEmailRegex(email: String): Boolean {
    val EMAIL_REGEX =
        "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"
    return EMAIL_REGEX.toRegex().matches(email)
}



