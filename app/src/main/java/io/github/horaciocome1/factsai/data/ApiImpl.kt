package io.github.horaciocome1.factsai.data

import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.perf.FirebasePerformance
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
    performance: FirebasePerformance,
    private val coroutineContext: CoroutineContext,
) : Api {

    private val _facts = MutableSharedFlow<Pair<List<String>, Boolean>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val facts = _facts.asSharedFlow()

    private var currentTopic = ""

    private val registerInstallationTrace = performance.newTrace("Api:registerInstallation")
    private val generateFactsTrace = performance.newTrace("Api:generateFacts")

    override suspend fun registerInstallation(installationId: String): Api.Result {
        Timber.v("registerInstallation installationId=$installationId")
        registerInstallationTrace.start()

        val result = withContext(coroutineContext) {
            try {
                functions.getHttpsCallable("registerInstallation")
                    .call(mapOf("installationId" to installationId))
                    .await()
            } catch (e: Exception) {
                Timber.e("registerInstallation installationId=$installationId", e)
                return@withContext null
            }
        }

        if (result == null) {
            Timber.w("registerInstallation installationId=$installationId failed result=null")
            registerInstallationTrace.putAttribute("result", "null")
            return Api.Result.Failure("Failed to register installation")
        }

        val data = result.data as? Map<*, *>

        if (data == null) {
            Timber.w("registerInstallation installationId=$installationId failed data=null")
            registerInstallationTrace.putAttribute("data", "null")
            return Api.Result.Failure("Failed to register installation")
        }

        if (data["success"] != true) {
            Timber.w("registerInstallation installationId=$installationId failed data=$data")
            registerInstallationTrace.putAttribute("success", "false")
            return Api.Result.Failure("Failed to register installation")
        }

        registerInstallationTrace.putAttribute("success", "true")
        registerInstallationTrace.stop()

        return Api.Result.Success(Unit)
    }

    override suspend fun generateFacts(installationId: String, languageTag: String, count: Int, temperature: Float): Api.Result {
        Timber.v("generateFacts installationId=$installationId languageTag=$languageTag count=$count temperature=$temperature")
        return generateFacts(installationId, currentTopic, languageTag, count, temperature)
    }

    override suspend fun generateFacts(installationId: String, topic: String, languageTag: String, count: Int, temperature: Float): Api.Result {
        Timber.v("generateFacts installationId=$installationId topic=$topic languageTag=$languageTag count=$count temperature=$temperature")
        generateFactsTrace.start()
        generateFactsTrace.putAttribute("languageTag", languageTag)
        generateFactsTrace.putAttribute("count", count.toString())
        generateFactsTrace.putAttribute("temperature", temperature.toString())

        val result = withContext(coroutineContext) {
            try {
                val data = mapOf(
                    "topic" to topic,
                    "installationId" to "installationId",
                    "languageTag" to languageTag,
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
        }

        if (result == null) {
            Timber.w("generateFacts installationId=$installationId topic=$topic count=$count temperature=$temperature failed result=null")
            generateFactsTrace.putAttribute("result", "null")
            return Api.Result.Failure("Failed to get facts")
        }

        val data = result.data as? Map<*, *>

        if (data == null) {
            Timber.w("generateFacts installationId=$installationId topic=$topic count=$count temperature=$temperature failed data=null")
            generateFactsTrace.putAttribute("data", "null")
            return Api.Result.Failure("Failed to get facts")
        }

        if (data["success"] != true) {
            Timber.w("generateFacts installationId=$installationId topic=$topic count=$count temperature=$temperature failed data=$data")
            generateFactsTrace.putAttribute("success", "false")
            return Api.Result.Failure("Failed to get facts")
        }

        val facts = (data["facts"] as? List<*>)?.filterIsInstance<String>()

        if (facts == null) {
            Timber.w("generateFacts installationId=$installationId topic=$topic count=$count temperature=$temperature failed facts=null")
            generateFactsTrace.putAttribute("facts", "null")
            return Api.Result.Failure("Failed to get facts")
        }

        generateFactsTrace.putAttribute("success", "true")

        _facts.emit(value = facts to (topic != currentTopic))
        currentTopic = topic

        generateFactsTrace.stop()

        return Api.Result.Success(facts)
    }
}
