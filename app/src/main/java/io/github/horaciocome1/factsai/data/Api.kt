package io.github.horaciocome1.factsai.data

import io.github.horaciocome1.factsai.R
import kotlinx.coroutines.flow.Flow

interface Api {

    val facts: Flow<Pair<List<String>, Boolean>>

    val hasInternetConnection: Flow<Boolean>

    suspend fun registerInstallation(installationId: String): Result

    suspend fun generateFacts(installationId: String, topic: String, languageTag: String, count: Int, temperature: Float): Result

    suspend fun generateFacts(installationId: String, languageTag: String, count: Int, temperature: Float): Result

    sealed interface Result {
        sealed interface Failure : Result {
            val messageRes: Int

            object NoInternet : Failure {
                override val messageRes = R.string.error_no_internet
            }

            object CouldNotReachServer : Failure {
                override val messageRes = R.string.error_could_not_reach_server
            }

            object Generic : Failure {
                override val messageRes = R.string.error_something_went_wrong
            }
        }
        data class Success<T>(val data: T) : Result
    }

    object Constants {
        const val KEY_INSTALLATION_ID = "installationId"
    }
}
