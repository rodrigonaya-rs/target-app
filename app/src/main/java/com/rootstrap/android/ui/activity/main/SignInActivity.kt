package com.rootstrap.android.ui.activity.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.rootstrap.android.R
import com.rootstrap.android.databinding.ActivitySignInBinding
import com.rootstrap.android.metrics.Analytics
import com.rootstrap.android.metrics.PageEvents
import com.rootstrap.android.metrics.VISIT_SIGN_IN
import com.rootstrap.android.network.models.User
import com.rootstrap.android.ui.view.AuthView
import com.rootstrap.android.util.NetworkState
import com.rootstrap.android.util.permissions.PermissionActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInActivity : PermissionActivity(), AuthView {

    private val viewModel: SignInActivityViewModel by viewModels()
    private lateinit var binding: ActivitySignInBinding
    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)

        setContentView(binding.root)
        Analytics.track(PageEvents.visit(VISIT_SIGN_IN))

        with(binding) {
            signInButton.setOnClickListener { signIn() }
            signUpTextView.setOnClickListener { signUp() }
        }
        setUpFacebookAuthentication()

        lifecycle.addObserver(viewModel)

        setObservers()
    }

    override fun showProfile() {
        startActivityClearTask(ProfileActivity())
    }

    private fun signIn() {
        with(binding) {

            val validationResult = listOf(
                emailFormInput.validateNotEmpty(R.string.missing_email_error) &&
                        emailFormInput.validateIsEmail(R.string.email_not_valid_error),
                passwordFormInput.validateNotEmpty(R.string.missing_password_error)
            )

            if (validationResult.contains(false))
                return

            val user = User(
                email = emailFormInput.value(),
                password = passwordFormInput.value()
            )
            viewModel.signIn(user)
        }
    }

    private fun setUpFacebookAuthentication() {
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    result?.accessToken?.let {
                        viewModel.signInWithFacebook(it.token)
                        return
                    }

                    this.onError(FacebookException())
                }

                override fun onCancel() {}

                override fun onError(error: FacebookException?) {
                    Toast.makeText(
                        this@SignInActivity,
                        getString(
                            R.string.facebook_login_error,
                            error?.message ?: getString(R.string.default_error)
                        ),
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
        with(binding) {
            facebookAuthenticationTextView.setOnClickListener { facebookAuthentication() }
        }
    }

    private fun facebookAuthentication() {
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("email"))
    }

    private fun signUp() {
        startActivity(Intent(this, SignUpActivity::class.java))
    }

    private fun setObservers() {
        viewModel.state.observe(this, Observer {
            when (it) {
                SignInState.signInFailure -> showError(viewModel.error)
                SignInState.signInSuccess -> showProfile()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }
}
