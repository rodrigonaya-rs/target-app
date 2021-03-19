package com.rootstrap.android.ui.activity.main

import android.app.AlertDialog
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.rootstrap.android.R
import com.rootstrap.android.databinding.ActivitySignUpBinding
import com.rootstrap.android.metrics.Analytics
import com.rootstrap.android.metrics.PageEvents
import com.rootstrap.android.metrics.VISIT_SIGN_UP
import com.rootstrap.android.network.models.UserSignUpRequest
import com.rootstrap.android.ui.base.BaseActivity
import com.rootstrap.android.ui.view.AuthView
import com.rootstrap.android.util.NetworkState
import com.rootstrap.android.util.extensions.removeWhitespaces
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class SignUpActivity : BaseActivity(), AuthView {

    private val viewModel: SignUpActivityViewModel by viewModels()
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var gendersDialog: AlertDialog

    companion object {
        const val MIN_PASSWORD_LENGTH = 8
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)

        setContentView(binding.root)
        Analytics.track(PageEvents.visit(VISIT_SIGN_UP))
        initGendersDialog()

        with(binding) {
            passwordConfirmationFormInput.setOnEditorActionListener(TextView.OnEditorActionListener { view, _, _ ->
                showGenderDialog()
                passwordConfirmationFormInput.clearFocus()
                hideSoftKeyboard(view)
                true
            })
            genderFormInput.setInputClickable { showGenderDialog() }
            signUpButton.setOnClickListener { signUp() }
            signInTextView.setOnClickListener { signIn() }
        }
        lifecycle.addObserver(viewModel)
        setObservers()
    }

    private fun initGendersDialog() {
        gendersDialog = AlertDialog.Builder(this).also {
            it.setTitle(R.string.select_gender)
            it.setCancelable(false)
            val genders = resources.getStringArray(R.array.genders)
            it.setItems(genders) { _, position ->
                with(binding.genderFormInput) {
                    setText(genders[position])
                    isEnabled = true
                }
            }
        }.create()
    }

    override fun showProfile() {
        startActivityClearTask(ProfileActivity())
    }

    private fun showGenderDialog() {
        if (!gendersDialog.isShowing) {
            with(binding) {
                genderFormInput.isEnabled = false
            }
            gendersDialog.show()
        }
    }

    private fun signUp() {
        with(binding) {
            val validationResult = listOf(
                    nameFormInput.validateNotEmpty(R.string.missing_name_error),
                    emailFormInput.validateNotEmpty(R.string.missing_email_error) &&
                    emailFormInput.validateIsEmail(R.string.email_not_valid_error),
                    passwordFormInput.validateNotEmpty(R.string.missing_password_error) &&
                    passwordFormInput.validateLength(MIN_PASSWORD_LENGTH, R.string.short_password_error),
                    passwordConfirmationFormInput.validateNotEmpty(R.string.missing_confirm_password_error) &&
                    passwordConfirmationFormInput.validateSameContent(passwordFormInput, R.string.confirm_password_match_error),
                    genderFormInput.validateNotEmpty(R.string.missing_gender_error)
            )
            if (validationResult.contains(false))
                return

            val signUpRequest = UserSignUpRequest(
                userName = nameFormInput.value().removeWhitespaces().toLowerCase(Locale.ROOT),
                email = emailFormInput.value(),
                gender = genderFormInput.value().toLowerCase(Locale.ROOT),
                password = passwordFormInput.value(),
                passwordConfirmation = passwordConfirmationFormInput.value()
            )
            viewModel.signUp(signUpRequest)
        }
    }

    private fun signIn() {
        onBackPressed()
    }

    private fun setObservers() {
        viewModel.state.observe(this, Observer {
            when (it) {
                SignUpState.signUpFailure -> showError(viewModel.error)
                SignUpState.signUpSuccess -> showProfile()
            }
        })

        viewModel.networkState.observe(this, Observer {
            when (it) {
                NetworkState.loading -> showProgress()
                NetworkState.idle -> hideProgress()
                else -> showError(viewModel.error ?: getString(R.string.default_error))
            }
        })
    }
}
