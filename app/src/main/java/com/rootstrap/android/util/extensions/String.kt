package com.rootstrap.android.util.extensions

/**
 * Remove whitespaces from the given string
 * */
fun String.removeWhitespaces() = this.replace("\\s".toRegex(), "")
