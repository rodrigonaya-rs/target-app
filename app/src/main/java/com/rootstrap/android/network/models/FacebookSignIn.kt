package com.rootstrap.android.network.models

import com.squareup.moshi.Json

data class FacebookSignIn(
    @field:Json(name = "access_token") val accessToken: String
)
