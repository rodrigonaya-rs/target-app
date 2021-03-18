package com.rootstrap.android

import com.rootstrap.android.util.extensions.isEmail
import com.rootstrap.android.util.extensions.removeWhitespaces
import org.junit.Assert.assertEquals
import org.junit.Test

public class ValidationTests {
    @Test
    fun checkEmailTest() {
        assertEquals(true, "email@mkdi.com".isEmail())
        assertEquals(false, "email@mkdi".isEmail())
        assertEquals(false, "email".isEmail())
        assertEquals(false, "email.com".isEmail())
    }

    @Test
    fun checkRemoveWhitespacesTest() {
        assertEquals("helloworld", "hello world".removeWhitespaces())
        assertEquals("helloworld", "     hello world".removeWhitespaces())
        assertEquals("helloworld", "hello       world".removeWhitespaces())
        assertEquals("helloworld", "hello world     ".removeWhitespaces())
        assertEquals("helloworld", "    hello      world     ".removeWhitespaces())
    }
}
