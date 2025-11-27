package com.handylab.ruto.data.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.inject.Inject

class EncryptedPrefsSecureStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : SecureStore {

    companion object {
        private const val PREFS_FILE_NAME = "secure_prefs"
    }

    private val prefs: SharedPreferences by lazy {
        // 첫 접근 시 초기화 시도
        createSafeEncryptedPrefs()
    }


    private fun createSafeEncryptedPrefs(): SharedPreferences {
        return try {
            createEncryptedPrefs()
        } catch (e: Exception) {
            Log.e("SecureStore", "Failed to create EncryptedSharedPreferences. Resetting...", e)

            // 1. 기존 데이터 삭제 (호환성 처리 포함)
            deleteSharedPreferencesFile()

            // 2. Keystore의 Master Key 삭제
            deleteMasterKey()

            // 3. 재시도 (여기서 또 실패하면 앱은 크래시 발생 - 이는 정상적인 동작)
            createEncryptedPrefs()
        }
    }

    /**
     * 실제 EncryptedSharedPreferences 생성 로직
     * (MasterKeys 대신 MasterKey.Builder 사용 - 최신 표준)
     */
    private fun createEncryptedPrefs(): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun deleteSharedPreferencesFile() {
        try {
            context.deleteSharedPreferences(PREFS_FILE_NAME)
        } catch (e: Exception) {
            Log.e("SecureStore", "Failed to delete shared preferences file", e)
        }
    }

    private fun deleteMasterKey() {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            if (keyStore.containsAlias(MasterKey.DEFAULT_MASTER_KEY_ALIAS)) {
                keyStore.deleteEntry(MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            }
        } catch (e: Exception) {
            Log.e("SecureStore", "Failed to delete MasterKey", e)
        }
    }

    override fun putString(key: String, value: String?) {
        prefs.edit {
            if (value == null) remove(key) else putString(key, value)
        }
    }

    override fun getString(key: String): String? = prefs.getString(key, null)

    override fun putBoolean(key: String, value: Boolean?) {
        prefs.edit {
            if (value == null) remove(key) else putBoolean(key, value)
        }
    }

    override fun getBoolean(key: String, default: Boolean): Boolean =
        prefs.getBoolean(key, default)


    override fun clear(key: String) = prefs.edit { remove(key) }

    override fun clearAll() = prefs.edit { clear() }
}