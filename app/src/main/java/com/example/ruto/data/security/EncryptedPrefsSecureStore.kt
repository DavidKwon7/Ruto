package com.example.ruto.data.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.getValue

class EncryptedPrefsSecureStore @Inject constructor(
    @ApplicationContext context: Context
) : SecureStore {

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override fun putString(key: String, value: String?) {
        prefs.edit().apply {
            if (value == null) remove(key) else putString(key, value)
        }.apply()
    }

    override fun getString(key: String): String? = prefs.getString(key, null)

    override fun putBoolean(key: String, value: Boolean?) {
        prefs.edit().apply {
            if (value == null) remove(key) else putBoolean(key, value)
        }.apply()
    }

    override fun getBoolean(key: String, default: Boolean): Boolean =
        prefs.getBoolean(key, default)


    override fun clear(key: String) = prefs.edit().remove(key).apply()

    override fun clearAll() = prefs.edit().clear().apply()
}