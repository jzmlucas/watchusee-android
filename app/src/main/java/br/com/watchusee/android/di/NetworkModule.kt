package br.com.watchusee.android.di

import br.com.watchusee.android.data.api.MovieApi
import br.com.watchusee.android.data.repository.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://watchusee-backend.onrender.com/"

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

            if ((response.code == 401 || response.code == 403) && !token.isNullOrBlank()) {
                tokenManager.triggerUnauthorized()
            }

            response
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: Interceptor,
        loggingInterceptor: HttpLoggingInterceptor
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
            .addInterceptor(loggingInterceptor)
            .build()
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
}