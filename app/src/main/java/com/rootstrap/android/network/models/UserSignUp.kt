package com.rootstrap.android.network.models

import com.squareup.moshi.Json

data class UserSignUpRequest(
    @Json(name = "username") val userName: String = "",
    var email: String = "",
    var gender: String = "",
    val password: String = "",
    @Json(name = "password_confirmation") val passwordConfirmation: String = ""
)

data class UserSignUpRequestSerializer(@Json(name = "user") val user: UserSignUpRequest)
