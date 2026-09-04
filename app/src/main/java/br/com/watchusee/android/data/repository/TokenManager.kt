package br.com.watchusee.android.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext context: Context
) {

    private val _onUnauthorized = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val onUnauthorized = _onUnauthorized.asSharedFlow()


    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "auth_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveAuthData(
        id: Long,
        nick: String,
        token: String
    ) {
        prefs.edit()
            .putLong(KEY_ID, id)
            .putString(KEY_NICK, nick)
            .putString(KEY_TOKEN, token)
            .commit()
    }

    fun getToken(): String? =
        prefs.getString(KEY_TOKEN, null)

    fun getNick(): String? =
        prefs.getString(KEY_NICK, null)

    fun getId(): Long =
        prefs.getLong(KEY_ID, -1L)

    fun clear() {
        prefs.edit()
            .clear()
            .commit()
    }

    fun triggerUnauthorized() {
        _onUnauthorized.tryEmit(Unit)
    }
    companion object {
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_NICK = "user_nick"
        private const val KEY_ID = "user_id"
    }
}