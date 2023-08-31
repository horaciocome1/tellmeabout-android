package io.github.horaciocome1.factsai.data

import android.annotation.SuppressLint
import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.firebase.perf.metrics.AddTrace
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class PreferencesHelperImpl @Inject constructor(@ApplicationContext context: Context) : PreferencesHelper {

    private val sharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "secret_shared_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    @AddTrace(name = "PreferencesHelper:edit")
    override fun edit(key: String, value: Any) {
        Timber.i("edit key=$key value=$value")
        when (value) {
            is String -> sharedPreferences.edit().putString(key, value).apply()
            is Boolean -> sharedPreferences.edit().putBoolean(key, value).apply()
            is Int -> sharedPreferences.edit().putInt(key, value).apply()
            is Long -> sharedPreferences.edit().putLong(key, value).apply()
            is Float -> sharedPreferences.edit().putFloat(key, value).apply()
            else -> Timber.e("edit value type not supported")
        }
    }

    @SuppressLint("ApplySharedPref")
    @AddTrace(name = "PreferencesHelper:commit")
    override suspend fun commit(key: String, value: Any) {
        Timber.i("edit key=$key value=$value")
        when (value) {
            is String -> sharedPreferences.edit().putString(key, value).commit()
            is Boolean -> sharedPreferences.edit().putBoolean(key, value).commit()
            is Int -> sharedPreferences.edit().putInt(key, value).commit()
            is Long -> sharedPreferences.edit().putLong(key, value).commit()
            is Float -> sharedPreferences.edit().putFloat(key, value).commit()
            else -> Timber.e("edit value type not supported")
        }
    }

    @AddTrace(name = "PreferencesHelper:getString")
    override fun getString(key: String): String? {
        Timber.i("getString key=$key")
        return sharedPreferences.getString(key, null)
    }

    @AddTrace(name = "PreferencesHelper:getBoolean")
    override fun getBoolean(key: String): Boolean {
        Timber.i("getBoolean key=$key")
        return sharedPreferences.getBoolean(key, false)
    }
}
