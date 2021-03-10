package com.rootstrap.android.tests

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.google.gson.Gson
import com.rootstrap.android.R
import com.rootstrap.android.network.models.User
import com.rootstrap.android.network.models.UserSerializer
import com.rootstrap.android.ui.activity.main.ProfileActivity
import com.rootstrap.android.ui.activity.main.SignUpActivity
import com.rootstrap.android.utils.BaseTests
import dagger.hilt.android.testing.HiltAndroidTest
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@HiltAndroidTest
class SignUpActivityTest : BaseTests() {

    private lateinit var activity: SignUpActivity
    private lateinit var scenario: ActivityScenario<SignUpActivity>

    @Before
    override fun before() {
        super.before()
        scenario = ActivityScenario.launch(SignUpActivity::class.java)
        scenario.onActivity { activity -> this.activity = activity }
    }

    @Test
    fun signUpSuccessfulTest() {
        scenario.recreate()
        setServerDispatch(signUpDispatcher())
        val testUser = testUser()
        populateUserData(testUser)
        scrollAndPerformClick(R.id.sign_up_button)
        val user = sessionManager.user
        assertEquals(user, testUser)

        activity.runOnUiThread {
            val current = currentActivity()
            assertEquals(ProfileActivity::class.java.name, current::class.java.name)
        }
    }

    @Test
    fun signUpErrorTest() {
        scenario.recreate()
        setServerDispatch(unsuccessfulSignUpDispatcher())
        val testUser = testUser()
        populateUserData(testUser)
        scrollAndPerformClick(R.id.sign_up_button)
        onView(withText(R.string.error))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withText(R.string.ok)).perform(click())
        activity.runOnUiThread {
            val current = currentActivity()
            assertEquals(SignUpActivity::class.java.name, current::class.java.name)
        }
    }

    private fun populateUserData(user: User) {
        scrollAndTypeText(R.id.first_name_edit_text, user.firstName)
        scrollAndTypeText(R.id.last_name_edit_text, user.lastName)
        scrollAndTypeText(R.id.email_edit_text, user.email)
        scrollAndSelectItem(R.id.gender_spinner, user.gender)
        scrollAndTypeText(R.id.password_edit_text, user.password)
        scrollAndTypeText(R.id.password_confirmation_edit_text, user.password)
    }

    private fun signUpDispatcher(): Dispatcher {
        return object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return if (request.path!!.contains("users")) {
                    val userResponse = UserSerializer(testUser())
                    mockServer.successfulResponse().setBody(
                        Gson().toJson(userResponse)
                    )
                } else
                    mockServer.notFoundResponse()
            }
        }
    }

    private fun unsuccessfulSignUpDispatcher(): Dispatcher {
        return object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return if (request.path!!.contains("users")) {
                    mockServer.customResponse(500)
                } else
                    mockServer.notFoundResponse()
            }
        }
    }

    @After
    override fun after() {
        super.after()
    }
}
