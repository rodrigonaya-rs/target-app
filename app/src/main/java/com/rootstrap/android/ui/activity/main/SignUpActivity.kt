package com.rootstrap.android.ui.activity.main

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
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
import com.rootstrap.android.util.extensions.validateIsEmail
import com.rootstrap.android.util.extensions.validateLength
import com.rootstrap.android.util.extensions.validateNotEmpty
import com.rootstrap.android.util.extensions.validateSameContent
import com.rootstrap.android.util.extensions.value
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
            genderEditText.onFocusChangeListener = View.OnFocusChangeListener { _, focused ->
                if (focused)
                    showGenderDialog()
            }
            genderEditText.setOnClickListener { showGenderDialog() }
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
                with(binding) {
                    genderEditText.setText(genders[position])
                    genderEditText.isEnabled = true
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
                genderEditText.isEnabled = false
            }
            gendersDialog.show()
        }
    }

    private fun signUp() {
        with(binding) {
            val validationResult = listOf(
                    nameTextInputLayout.validateNotEmpty(R.string.missing_name_error),
                    emailTextInputLayout.validateNotEmpty(R.string.missing_email_error) &&
                    emailTextInputLayout.validateIsEmail(R.string.email_not_valid_error),
                    passwordTextInputLayout.validateNotEmpty(R.string.missing_password_error) &&
                    passwordTextInputLayout.validateLength(MIN_PASSWORD_LENGTH, R.string.short_password_error),
                    passwordConfirmationTextInputLayout.validateNotEmpty(R.string.missing_confirm_password_error) &&
                    passwordConfirmationTextInputLayout.validateSameContent(passwordTextInputLayout, R.string.confirm_password_match_error),
                    genderTextInputLayout.validateNotEmpty(R.string.missing_gender_error)
            )
            if (validationResult.contains(false))
                return

            val signUpRequest = UserSignUpRequest(
                userName = nameEditText.value().removeWhitespaces().toLowerCase(Locale.ROOT),
                email = emailEditText.value(),
                gender = genderEditText.value().toLowerCase(Locale.ROOT),
                password = passwordEditText.value(),
                passwordConfirmation = passwordConfirmationEditText.value()
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
