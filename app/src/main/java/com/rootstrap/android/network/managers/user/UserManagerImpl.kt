package com.rootstrap.android.network.managers.user

import com.rootstrap.android.network.models.FacebookSignIn
import com.rootstrap.android.network.models.User
import com.rootstrap.android.network.models.UserSerializer
import com.rootstrap.android.network.models.UserSignUpRequest
import com.rootstrap.android.network.models.UserSignUpRequestSerializer
import com.rootstrap.android.network.services.ApiService
import com.rootstrap.android.util.extensions.ActionCallback
import com.rootstrap.android.util.extensions.Data
import javax.inject.Inject

/**
 * Singleton class
 * */
class UserManagerImpl @Inject constructor(private val service: ApiService) : UserManager {

    override suspend fun signUp(userSignUpRequest: UserSignUpRequest): Result<Data<UserSerializer>> =
        ActionCallback.call(service.signUp(UserSignUpRequestSerializer(userSignUpRequest)))

    override suspend fun signIn(user: User): Result<Data<UserSerializer>> =
        ActionCallback.call(service.signIn(UserSerializer(user)))

    override suspend fun signInWithFacebook(accessToken: String): Result<Data<UserSerializer>> =
        ActionCallback.call(service.signInWithFacebook(FacebookSignIn(accessToken)))

    override suspend fun signOut(): Result<Data<Void>> =
        ActionCallback.call(service.signOut())
}
