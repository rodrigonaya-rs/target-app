package com.rootstrap.android.utils

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import com.rootstrap.android.ui.custom.FormInput
import org.hamcrest.Description
import org.hamcrest.Matcher

class ClearTextOnFormInput : ViewAction {
    override fun getConstraints(): Matcher<View> {
        return isAssignableFrom(FormInput::class.java)
    }

    override fun getDescription(): String {
        return "Clears text of a FormInput"
    }

    override fun perform(uiController: UiController?, view: View?) {
        val formInput = view as FormInput
        formInput.setText("")
    }
}

class TypeTextOnFormInput(private val text: String) : ViewAction {
    override fun getConstraints(): Matcher<View> {
        return isAssignableFrom(FormInput::class.java)
    }

    override fun getDescription(): String {
        return "Type text into a FormInput"
    }

    override fun perform(uiController: UiController?, view: View?) {
        val formInput = view as FormInput
        formInput.setText(text)
    }
}

fun hasFormInputError(expectedError: Int): Matcher<View> {
    return object : BoundedMatcher<View, FormInput>(FormInput::class.java) {
        private var expectedErrorText = ""

        override fun describeTo(description: Description?) {
            description?.appendText("Expected error text: $expectedErrorText")
        }

        override fun matchesSafely(item: FormInput?): Boolean {
            if (item !is FormInput) {
                return false
            }

            val error = item.getError() ?: return false
            expectedErrorText = item.context.getString(expectedError)
            return expectedErrorText == error
        }
    }
}
