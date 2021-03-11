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
import com.rootstrap.android.util.extensions.isEmail
import com.rootstrap.android.util.extensions.isNotEmpty
import com.rootstrap.android.util.extensions.removeWhitespaces
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
            var errors = false
            if (!nameEditText.isNotEmpty()) {
                nameTextInputLayout.error = getString(R.string.missing_name_error)
                errors = true
            }
            if (!emailEditText.isNotEmpty()) {
                emailTextInputLayout.error = getString(R.string.missing_email_error)
                errors = true
            } else if (!emailEditText.value().isEmail()) {
                emailTextInputLayout.error = getString(R.string.email_not_valid_error)
                errors = true
            }
            if (!passwordEditText.isNotEmpty()) {
                passwordTextInputLayout.error = getString(R.string.missing_password_error)
                errors = true
            } else if (passwordEditText.text!!.length < MIN_PASSWORD_LENGTH) {
                passwordTextInputLayout.error = getString(R.string.short_password_error)
                errors = true
            }
            if (!passwordConfirmationEditText.isNotEmpty()) {
                passwordConfirmationTextInputLayout.error = getString(R.string.missing_confirm_password_error)
                errors = true
            }
            if (!genderEditText.isNotEmpty()) {
                genderTextInputLayout.error = getString(R.string.missing_gender_error)
                errors = true
            }

            if (errors)
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
