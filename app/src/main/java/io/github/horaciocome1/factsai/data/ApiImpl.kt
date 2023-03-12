package io.github.horaciocome1.factsai.data

import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class ApiImpl @Inject constructor(
    private val functions: FirebaseFunctions,
    private val coroutineContext: CoroutineContext,
) : Api {

    private val _facts = MutableSharedFlow<List<String>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val facts = _facts.asSharedFlow()

    override suspend fun registerInstallation(installationId: String): Api.Result {
        Timber.v("registerInstallation installationId=$installationId")
        val result = withContext(coroutineContext) {
            try {
                functions.getHttpsCallable("registerInstallation")
                    .call(mapOf("installationId" to installationId))
                    .await()
            } catch (e: Exception) {
                Timber.e("registerInstallation installationId=$installationId", e)
                return@withContext null
            }
        } ?: return Api.Result.Failure("Failed to register installation")

        val data = result.data as? Map<*, *> ?: return Api.Result.Failure("Failed to register installation")

        if (data["success"] != true) {
            Timber.w("registerInstallation installationId=$installationId failed data=$data")
            return Api.Result.Failure("Failed to register installation")
        }

        return Api.Result.Success(Unit)
    }

    override suspend fun generateFacts(installationId: String, topic: String, count: Int, temperature: Float): Api.Result {
        Timber.v("generateFacts installationId=$installationId topic=$topic count=$count temperature=$temperature")
        val result = withContext(coroutineContext) {
            try {
                val data = mapOf(
                    "topic" to topic,
                    "installationId" to "installationId",
                    "count" to count,
                    "temperature" to temperature,
                )
                functions.getHttpsCallable("generateFacts")
                    .call(data)
                    .await()
            } catch (e: Exception) {
                Timber.e("generateFacts installationId=$installationId topic=$topic count=$count temperature=$temperature", e)
                return@withContext null
            }
        } ?: return Api.Result.Failure("Failed to get facts")

        val data = result.data as? Map<*, *> ?: return Api.Result.Failure("Failed to get facts")

        if (data["success"] != true) {
            Timber.w("generateFacts installationId=$installationId topic=$topic count=$count temperature=$temperature failed data=$data")
            return Api.Result.Failure("Failed to get facts")
        }

        val facts = (data["facts"] as? List<*>)?.filterIsInstance<String>() ?: return Api.Result.Failure("Failed to get facts")
        _facts.emit(value = facts)

        return Api.Result.Success(facts)
    }
}
