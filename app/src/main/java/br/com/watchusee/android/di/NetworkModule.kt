package br.com.watchusee.android.di

import br.com.watchusee.android.data.api.MovieApi
import br.com.watchusee.android.data.api.SocialApi
import br.com.watchusee.android.data.repository.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.logging.HttpLoggingInterceptor
import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "http://10.0.2.2:8080/"
    //private const val BASE_URL = "https://watchusee-backend.onrender.com/"

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(
        tokenManager: TokenManager
    ): Interceptor {

        return Interceptor { chain ->

            val token = tokenManager.getToken()

            val requestBuilder = chain.request()
                .newBuilder()
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 13; Pixel 7)"
                )
                .header(
                    "Accept",
                    "application/json"
                )

            if (!token.isNullOrBlank()) {
                requestBuilder.header(
                    "Authorization",
                    "Bearer $token"
                )
            }

            val request = requestBuilder.build()

            val response = chain.proceed(request)

            response
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: Interceptor,
        loggingInterceptor: HttpLoggingInterceptor,
        tokenManager: TokenManager
    ): OkHttpClient {

        return OkHttpClient.Builder()
            .connectTimeout(
                60,
                TimeUnit.SECONDS
            )
            .readTimeout(
                60,
                TimeUnit.SECONDS
            )
            .writeTimeout(
                60,
                TimeUnit.SECONDS
            )
            .addInterceptor(authInterceptor)
            .authenticator { _, response ->
                // Rotaciona o refresh token apenas uma vez por cadeia de erro,
                // usando um cliente sem o próprio authenticator para evitar loop.
                if (responseCount(response) > 1) return@authenticator null

                synchronized(tokenManager) {
                    val refreshToken = tokenManager.getRefreshToken()
                    if (refreshToken.isNullOrBlank()) {
                        tokenManager.invalidateSession()
                        return@synchronized null
                    }

                    val refreshRequest = Request.Builder()
                        .url("${BASE_URL.removeSuffix("/")}/api/v1/auth/refresh")
                        .post(
                            Gson().toJson(
                                mapOf("refreshToken" to refreshToken)
                            ).toRequestBody("application/json".toMediaType())
                        )
                        .build()

                    val refreshResponse = OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .build()
                        .newCall(refreshRequest)
                        .execute()

                    if (!refreshResponse.isSuccessful) {
                        refreshResponse.close()
                        tokenManager.invalidateSession()
                        return@synchronized null
                    }

                    val payload = refreshResponse.body?.string()
                    refreshResponse.close()
                    val loginResponse = payload?.let {
                        Gson().fromJson(
                            it,
                            br.com.watchusee.android.data.dto.LoginResponse::class.java
                        )
                    }
                    if (loginResponse == null) {
                        tokenManager.invalidateSession()
                        return@synchronized null
                    }

                    tokenManager.saveAuthData(
                        loginResponse.id,
                        loginResponse.nick,
                        loginResponse.token,
                        loginResponse.refreshToken
                    )

                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${loginResponse.token}")
                        .build()
                }
            }
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private fun responseCount(response: okhttp3.Response): Int {
        var result = 1
        var prior = response.priorResponse
        while (prior != null) {
            result++
            prior = prior.priorResponse
        }
        return result
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit {

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideMovieApi(
        retrofit: Retrofit
    ): MovieApi {

        return retrofit.create(MovieApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSocialApi(
        retrofit: Retrofit
    ): SocialApi {
        return retrofit.create(SocialApi::class.java)
    }
}
