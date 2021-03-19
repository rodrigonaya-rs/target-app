package com.rootstrap.android.tests

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import com.google.gson.Gson
import com.rootstrap.android.R
import com.rootstrap.android.network.models.UserSerializer
import com.rootstrap.android.ui.activity.main.ProfileActivity
import com.rootstrap.android.ui.activity.main.SignInActivity
import com.rootstrap.android.utils.BaseTests
import com.rootstrap.android.utils.hasFormInputError
import com.rootstrap.android.utils.typeTextOnFormInput
import dagger.hilt.android.testing.HiltAndroidTest
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@HiltAndroidTest
class SignInActivityTest : BaseTests() {

    private lateinit var activity: SignInActivity
    private lateinit var scenario: ActivityScenario<SignInActivity>

    @Before
    override fun before() {
        super.before()
        scenario = ActivityScenario.launch(SignInActivity::class.java)
        scenario.onActivity { activity -> this.activity = activity }
    }

    @Test
    fun signInSuccessfulTest() {
        scenario.recreate()
        setServerDispatch(signInDispatcher())
        val testUser = testUser()
        typeTextOnFormInput(R.id.email_form_input, testUser.email)
        typeTextOnFormInput(R.id.password_form_input, testUser.password)
        signIn()
        val user = sessionManager.user
        assertEquals(user, testUser)

        activity.runOnUiThread {
            val current = currentActivity()
            assertEquals(ProfileActivity::class.java.name, current::class.java.name)
        }
    }

    @Test
    fun signInErrorTest() {
        scenario.recreate()
        setServerDispatch(unsuccessfulSignUpDispatcher())
        val testUser = testUser()
        typeTextOnFormInput(R.id.email_form_input, testUser.email)
        typeTextOnFormInput(R.id.password_form_input, testUser.password)
        signIn()
        Espresso.onView(ViewMatchers.withText(R.string.error))
            .inRoot(RootMatchers.isDialog())
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withText(R.string.ok)).perform(ViewActions.click())
        activity.runOnUiThread {
            val current = currentActivity()
            assertEquals(SignInActivity::class.java.name, current::class.java.name)
        }
    }

    @Test
    fun signInMissingInformationTest() {
        scenario.recreate()
        signIn()
        Espresso.onView(ViewMatchers.withId(R.id.email_form_input)).check(
            ViewAssertions.matches(hasFormInputError(R.string.missing_email_error))
        )
        Espresso.onView(ViewMatchers.withId(R.id.password_form_input)).check(
            ViewAssertions.matches(hasFormInputError(R.string.missing_password_error))
        )
    }

    @Test
    fun signInInvalidEmailTest() {
        scenario.recreate()
        typeTextOnFormInput(R.id.email_form_input, "hello@world")
        signIn()
        Espresso.onView(ViewMatchers.withId(R.id.email_form_input)).check(
            ViewAssertions.matches(hasFormInputError(R.string.email_not_valid_error))
        )
    }

    @Test
    fun signInSuccessfulAfterUpdatingInvalidData() {
        scenario.recreate()
        setServerDispatch(signInDispatcher())
        val testUser = testUser()
        val wrongTestUser = testUser.copy(email = "test@test")
        typeTextOnFormInput(R.id.email_form_input, wrongTestUser.email)
        typeTextOnFormInput(R.id.password_form_input, testUser.password)
        signIn()
        Espresso.onView(ViewMatchers.withId(R.id.email_form_input)).check(
            ViewAssertions.matches(hasFormInputError(R.string.email_not_valid_error))
        )
        typeTextOnFormInput(R.id.email_form_input, testUser.email)
        signIn()
        val user = sessionManager.user
        assertEquals(user, testUser)
        activity.runOnUiThread {
            val current = currentActivity()
            assertEquals(ProfileActivity::class.java.name, current::class.java.name)
        }
    }

    private fun signIn() {
        performClick(R.id.sign_in_button)
    }

    private fun signInDispatcher(): Dispatcher {
        return object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return if (request.path!!.contains("users/sign_in")) {
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
