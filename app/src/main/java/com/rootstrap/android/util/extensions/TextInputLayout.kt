package com.rootstrap.android.util.extensions

import com.google.android.material.textfield.TextInputLayout

/**
 * Check if TextInputLayout's EditText is not empty and set error if needed
 * */
fun TextInputLayout.validateNotEmpty(errorMessageResource: Int): Boolean {
    if (this.editText?.isNotEmpty() == false) {
        this.error = this.context.getString(errorMessageResource)
        return false
    }

    this.error = null
    return true
}

/**
 * Check if TextInputLayout's EditText has valid email format and set error if needed
 * */
fun TextInputLayout.validateIsEmail(errorMessageResource: Int): Boolean {
    if (this.editText?.value()?.isEmail() == false) {
        this.error = this.context.getString(errorMessageResource)
        return false
    }

    this.error = null
    return true
}

/**
 * Check TextInputLayout's EditText minimum length and set error if needed
 * */
fun TextInputLayout.validateLength(minLength: Int, errorMessageResource: Int): Boolean {
    if (this.editText != null && this.editText!!.text.length < minLength) {
        this.error = this.context.getString(errorMessageResource)
        return false
    }

    this.error = null
    return true
}

/**
 * Compare if TextInputLayout's EditText have the same value and set error if needed
 * */
fun TextInputLayout.validateSameContent(compareTo: TextInputLayout, errorMessageResource: Int): Boolean {
    if (this.editText != null && this.editText!!.text.toString() != compareTo.editText?.text.toString()) {
        this.error = this.context.getString(errorMessageResource)
        return false
    }

    this.error = null
    return true
}
