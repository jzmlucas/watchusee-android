package br.com.watchusee.android.data.repository

import br.com.watchusee.android.data.api.MovieApi
import br.com.watchusee.android.data.dto.UserProfileResponse

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val movieApi: MovieApi
) {
    suspend fun getProfile(): UserProfileResponse {
        return movieApi.getProfile()
    }
}
