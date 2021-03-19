package com.rootstrap.android.tests

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.google.gson.Gson
import com.rootstrap.android.R
import com.rootstrap.android.network.models.User
import com.rootstrap.android.network.models.UserSerializer
import com.rootstrap.android.ui.activity.main.ProfileActivity
import com.rootstrap.android.ui.activity.main.SignUpActivity
import com.rootstrap.android.utils.BaseTests
import com.rootstrap.android.utils.hasFormInputError
import com.rootstrap.android.utils.scrollAndTypeTextOnFormInput
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
        signUp()
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
        signUp()
        onView(withText(R.string.error))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withText(R.string.ok)).perform(click())
        activity.runOnUiThread {
            val current = currentActivity()
            assertEquals(SignUpActivity::class.java.name, current::class.java.name)
        }
    }

    @Test
    fun signUpMissingInformationTest() {
        scenario.recreate()
        signUp()
        onView(withId(R.id.name_form_input)).check(
                matches(hasFormInputError(R.string.missing_name_error))
        )
        onView(withId(R.id.email_form_input)).check(
                matches(hasFormInputError(R.string.missing_email_error))
        )
        onView(withId(R.id.password_form_input)).check(
                matches(hasFormInputError(R.string.missing_password_error))
        )
        onView(withId(R.id.password_confirmation_form_input)).check(
                matches(hasFormInputError(R.string.missing_confirm_password_error))
        )
        onView(withId(R.id.gender_form_input)).check(
                matches(hasFormInputError(R.string.missing_gender_error))
        )
    }

    @Test
    fun signUpInvalidEmailTest() {
        scenario.recreate()
        scrollAndTypeTextOnFormInput(R.id.email_form_input, "hello@world")
        signUp()
        onView(withId(R.id.email_form_input)).check(
                matches(hasFormInputError(R.string.email_not_valid_error))
        )
    }

    @Test
    fun signUpShortPasswordTest() {
        scenario.recreate()
        scrollAndTypeTextOnFormInput(R.id.password_form_input, "12345")
        signUp()
        onView(withId(R.id.password_form_input)).check(
                matches(hasFormInputError(R.string.short_password_error))
        )
    }

    @Test
    fun signUpPasswordDoesNotMatchTest() {
        scenario.recreate()
        scrollAndTypeTextOnFormInput(R.id.password_form_input, "12345678")
        scrollAndTypeTextOnFormInput(R.id.password_confirmation_form_input, "12345679")
        signUp()
        onView(withId(R.id.password_confirmation_form_input)).check(
                matches(hasFormInputError(R.string.confirm_password_match_error))
        )
    }

    @Test
    fun signUpSuccessfulAfterUpdatingInvalidData() {
        scenario.recreate()
        setServerDispatch(signUpDispatcher())
        val testUser = testUser()
        val wrongTestUser = testUser.copy(email = "test@test")
        populateUserData(wrongTestUser)
        signUp()
        onView(withId(R.id.email_form_input)).check(
                matches(hasFormInputError(R.string.email_not_valid_error))
        )
        scrollAndTypeTextOnFormInput(R.id.email_form_input, testUser.email)
        signUp()
        val user = sessionManager.user
        assertEquals(user, testUser)
        activity.runOnUiThread {
            val current = currentActivity()
            assertEquals(ProfileActivity::class.java.name, current::class.java.name)
        }
    }

    private fun populateUserData(user: User) {
        scrollAndTypeTextOnFormInput(R.id.name_form_input, user.firstName)
        scrollAndTypeTextOnFormInput(R.id.email_form_input, user.email)
        scrollAndTypeTextOnFormInput(R.id.password_form_input, user.password)
        scrollAndTypeTextOnFormInput(R.id.password_confirmation_form_input, user.password)
        scrollAndSelectItem(R.id.gender_form_input, user.gender)
    }

    private fun signUp() {
        scrollAndPerformClick(R.id.sign_up_button)
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
