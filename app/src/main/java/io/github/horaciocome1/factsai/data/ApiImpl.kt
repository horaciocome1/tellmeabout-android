package io.github.horaciocome1.factsai.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.core.content.ContextCompat.getSystemService
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.perf.FirebasePerformance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class ApiImpl @Inject constructor(
    private val context: Context,
    private val functions: FirebaseFunctions,
    performance: FirebasePerformance,
    private val coroutineContext: CoroutineContext,
) : Api, ConnectivityManager.NetworkCallback() {

    private val coroutineScope = CoroutineScope(coroutineContext)

    private val _facts = MutableSharedFlow<Pair<List<String>, Boolean>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val facts = _facts.asSharedFlow()

    private val _hasInternetConnection = MutableSharedFlow<Boolean>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val hasInternetConnection = _hasInternetConnection.asSharedFlow().distinctUntilChanged()

    private var currentTopic = ""

    private val registerInstallationTrace = performance.newTrace("Api:registerInstallation")
    private val generateFactsTrace = performance.newTrace("Api:generateFacts")

    init {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        val connectivityManager = getSystemService(context, ConnectivityManager::class.java) as ConnectivityManager
        connectivityManager.requestNetwork(networkRequest, this)
        if (connectivityManager.activeNetwork != null) {
            onAvailable(connectivityManager.activeNetwork!!)
        } else {
            onUnavailable()
        }
    }

    override suspend fun registerInstallation(installationId: String): Api.Result {
        Timber.v("registerInstallation installationId=$installationId")
        registerInstallationTrace.start()

        val (result, error) = withContext(coroutineContext) {
            try {
                functions.getHttpsCallable("registerInstallation")
                    .call(mapOf("installationId" to installationId))
                    .await() to null
            } catch (e: IOException) {
                Timber.e("registerInstallation", e)
                return@withContext null to Api.Result.Failure.NoInternet
            } catch (e: FirebaseNetworkException) {
                Timber.e("registerInstallation exception=$e")
                return@withContext null to Api.Result.Failure.NoInternet
            } catch (e: FirebaseFunctionsException) {
                Timber.e("registerInstallation exception=$e")
                return@withContext null to Api.Result.Failure.CouldNotReachServer
            } catch (e: Exception) {
                Timber.e("registerInstallation exception=$e")
                return@withContext null to Api.Result.Failure.Generic
            }
        }

        val success = (result?.data as? Map<*, *>)?.get("success") as? Boolean

        when {
            error != null -> {
                registerInstallationTrace.putAttribute("error", context.getString(error.messageRes))
                registerInstallationTrace.stop()
                return error
            }
            success != true -> {
                registerInstallationTrace.putAttribute("success", "false")
                registerInstallationTrace.stop()
                return Api.Result.Failure.Generic
            }
            else -> {
                registerInstallationTrace.putAttribute("success", "true")
                registerInstallationTrace.stop()
                return Api.Result.Success(Unit)
            }
        }
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

        val (result, error) = withContext(coroutineContext) {
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
                    .await() to null
            } catch (e: IOException) {
                Timber.e("generateFacts", e)
                return@withContext null to Api.Result.Failure.NoInternet
            } catch (e: FirebaseNetworkException) {
                Timber.e("generateFacts exception=$e")
                return@withContext null to Api.Result.Failure.NoInternet
            } catch (e: FirebaseFunctionsException) {
                Timber.e("generateFacts exception=$e")
                return@withContext null to Api.Result.Failure.CouldNotReachServer
            } catch (e: Exception) {
                Timber.e("generateFacts exception=$e")
                return@withContext null to Api.Result.Failure.Generic
            }
        }

        val facts = ((result?.data as? Map<*, *>)?.get("facts") as? List<*>)?.filterIsInstance<String>()

        when {
            error != null -> {
                generateFactsTrace.putAttribute("error", context.getString(error.messageRes))
                generateFactsTrace.stop()
                return error
            }
            facts == null -> {
                generateFactsTrace.putAttribute("facts", "null")
                generateFactsTrace.stop()
                return Api.Result.Failure.Generic
            }
            else -> {
                generateFactsTrace.putAttribute("success", "true")
                _facts.emit(value = facts to (topic != currentTopic))
                currentTopic = topic
                generateFactsTrace.stop()
                return Api.Result.Success(facts)
            }
        }
    }

    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        coroutineScope.launch {
            _hasInternetConnection.emit(true)
        }
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        coroutineScope.launch {
            _hasInternetConnection.emit(false)
        }
    }

    override fun onUnavailable() {
        super.onUnavailable()
        coroutineScope.launch {
            _hasInternetConnection.emit(false)
        }
    }
}
