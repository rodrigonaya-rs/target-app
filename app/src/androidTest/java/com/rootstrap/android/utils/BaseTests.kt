package com.rootstrap.android.utils

import android.app.Activity
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.google.android.material.textfield.TextInputLayout
import com.rootstrap.android.network.managers.session.SessionManager
import com.rootstrap.android.network.models.User
import com.rootstrap.android.network.providers.ServiceProviderModule
import dagger.hilt.android.testing.HiltAndroidRule
import okhttp3.mockwebserver.Dispatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Rule
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4ClassRunner::class)
open class BaseTests {

    @Inject lateinit var sessionManager: SessionManager

    var mockServer: MockServer = MockServer

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    open fun setServerDispatch(dispatcher: Dispatcher) {
        mockServer.server().dispatcher = dispatcher
    }

    open fun before() {
        mockServer.startServer()
        ServiceProviderModule.URL_API = mockServer.server().url("/").toString()
        hiltRule.inject()
    }

    open fun after() {
        mockServer.stopServer()
    }

    open fun testUser() = User(
        "9032",
        "user123@mail.com",
        "Richard",
        "Richard",
        "Female",
        "asdasdasdasda",
        "Richard"
    )

    open fun scrollAndTypeText(id: Int, text: String) {
        onView(withId(id)).perform(
            scrollTo(),
            click(),
            clearText(),
            typeText(text),
            closeSoftKeyboard()
        )
    }

    open fun scrollAndSelectItem(id: Int, selectionText: String) {
        onView(withId(id)).perform(
            scrollTo(),
            click()
        )
        onView(withText(selectionText)).perform(click())
    }

    open fun typeText(id: Int, text: String) {
        onView(withId(id)).perform(
                click(),
                clearText(),
                typeText(text),
                closeSoftKeyboard()
        )
    }

    open fun scrollAndPerformClick(viewId: Int) {
        onView(withId(viewId)).perform(scrollTo(), click())
    }

    open fun performClick(viewId: Int) {
        onView(withId(viewId)).perform(click())
    }

    open fun stringMatches(viewId: Int, value: String) {
        onView(withId(viewId)).check(
            ViewAssertions.matches(
                withText(
                    value
                )
            )
        )
    }

    open fun currentActivity(): Activity {
        // Get the activity that currently started
        val activities =
            ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
        return activities.first()
    }

    open fun hasTextInputLayoutError(expectedError: Int): Matcher<View> {
        return object : BoundedMatcher<View, TextInputLayout>(TextInputLayout::class.java) {
            private var expectedErrorText = ""

            override fun describeTo(description: Description?) {
                description?.appendText("Expected error text: $expectedErrorText")
            }

            override fun matchesSafely(item: TextInputLayout?): Boolean {
                if (item !is TextInputLayout) {
                    return false
                }

                val error = item.error ?: return false
                expectedErrorText = item.context.getString(expectedError)
                return expectedErrorText == error
            }
        }
    }
}
