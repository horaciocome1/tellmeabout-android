package io.github.horaciocome1.factsai.data

import kotlinx.coroutines.flow.Flow

interface Api {

    val facts: Flow<List<String>>

    suspend fun registerInstallation(installationId: String): Result

    suspend fun generateFacts(installationId: String, topic: String, count: Int, temperature: Float): Result

    sealed interface Result {
        data class Failure(val errorMessage: String) : Result
        data class Success<T>(val data: T) : Result
    }

    object Constants {
        const val KEY_INSTALLATION_ID = "installationId"
    }
}
