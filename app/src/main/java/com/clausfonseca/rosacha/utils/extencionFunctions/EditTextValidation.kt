package com.clausfonseca.rosacha.utils.extencionFunctions

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import com.clausfonseca.rosacha.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

fun cleanErrorValidation(detail: TextInputEditText, container: TextInputLayout) {
    detail.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            container.error = ""
        }
    })
}

fun checkEmptyField(detail: TextInputEditText, container: TextInputLayout, context: Context, type: String = ""): Boolean {
    if (detail.text.toString() == "") {
        container.error = context.getString(R.string.required_field)
        return false
    }
    when (type) {
        "password" -> {
            if (detail.length() < 6) {
                container.error = context.getString(R.string.must_be_6_digits)
                return false
            }
        }

        "phone" -> {
            if (detail.length() < 14) {
                container.error = context.getString(R.string.must_be_14_digits)
                return false
            }
        }

        "email" -> {
            if (!Patterns.EMAIL_ADDRESS.matcher(detail.text.toString().trim().lowercase()).matches()) {
                container.error = context.getString(R.string.invalid_email_address)
                return false
            }
        }

        else -> {

        }
    }
    return true
}
