package io.github.horaciocome1.factsai.data

interface PreferencesHelper {

    fun edit(key: String, value: Any)

    suspend fun commit(key: String, value: Any)

    fun getString(key: String): String?

    fun getBoolean(key: String): Boolean
}
