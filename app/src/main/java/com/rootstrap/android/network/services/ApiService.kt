package com.rootstrap.android.network.services

import com.rootstrap.android.network.models.UserSerializer
import com.rootstrap.android.network.models.UserSignUpRequestSerializer
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST

interface ApiService {

    @POST("users/")
    fun signUp(@Body user: UserSignUpRequestSerializer): Call<UserSerializer>

    @POST("users/sign_in")
    fun signIn(@Body user: UserSerializer): Call<UserSerializer>

    @DELETE("users/sign_out")
    fun signOut(): Call<Void>
}
