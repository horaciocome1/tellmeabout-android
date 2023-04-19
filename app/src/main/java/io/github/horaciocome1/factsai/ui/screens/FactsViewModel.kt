package io.github.horaciocome1.factsai.ui.screens

import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.perf.ktx.trace
import com.google.firebase.perf.metrics.AddTrace
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.horaciocome1.factsai.data.Api
import io.github.horaciocome1.factsai.data.PreferencesHelper
import io.github.horaciocome1.factsai.util.AnalyticsEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FactsViewModel @Inject constructor(
    private val api: Api,
    private val preferencesHelper: PreferencesHelper,
    private val analytics: FirebaseAnalytics,
) : ViewModel() {

    private val _facts = MutableStateFlow(emptyList<String>())
    val facts = _facts.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _jumpToIndex = MutableSharedFlow<Int>()
    val jumpToIndex = _jumpToIndex.asSharedFlow()

    var lastIndexBeforeNextGeneration: Int? = null
        private set

    private var generateFactsJob: Job? = null

    private var readingDuration = 0L

    init {
        Timber.i("init")
        viewModelScope.launch {
            api.facts.collectLatest { (newFacts, newTopic) ->
                val currentLastIndex = _facts.value.lastIndex
                _facts.update { currentState ->
                    currentState + newFacts
                }
                if (newTopic) {
                    _jumpToIndex.emit(value = currentLastIndex + 1)
                }
            }
        }
    }

    @AddTrace(name = "FactsViewModel:onFactRead")
    fun onFactRead(index: Int, topic: String) {
        Timber.i("onFactRead index=$index topic=$topic lastIndexBeforeNextGeneration=$lastIndexBeforeNextGeneration")
        onFactReadingStarted()
        val remainingUnreadFacts = _facts.value.lastIndex - index
        if (remainingUnreadFacts < 20 && generateFactsJob?.isActive != true) {
            lastIndexBeforeNextGeneration = _facts.value.lastIndex
            generateFactsJob = viewModelScope.launch {
                trace("FactsViewModel:generateFacts") {
                    _loading.value = true

                    val installationId = preferencesHelper.getString(Api.Constants.KEY_INSTALLATION_ID)
                    if (installationId == null) {
                        Timber.w("onFactRead installationId is null")
                        _loading.value = false
                        return@launch
                    }

                    when (val result = api.generateFacts(installationId, topic, Locale.current.toLanguageTag(), 20, 0.6f)) {
                        is Api.Result.Success<*> -> {
                            Timber.i("onFactRead generateFacts success")
                            analytics.logEvent(AnalyticsEvent.GenerateFactsSucceeded.name) {
                                param("topic", topic)
                                param("languageTag", Locale.current.toLanguageTag())
                                param("factsCount", 20)
                                param("factsTemperature", 0.6)
                            }
                        }
                        is Api.Result.Failure -> {
                            Timber.w("onFactRead generateFacts failure errorMessage=${result.errorMessage}")
                            analytics.logEvent(AnalyticsEvent.GenerateFactsFailed.name) {
                                param("topic", topic)
                                param("languageTag", Locale.current.toLanguageTag())
                                param("factsCount", 20)
                                param("factsTemperature", 0.6)
                                param("errorMessage", result.errorMessage)
                            }
                        }
                    }
                    _loading.value = false
                }
            }
            analytics.logEvent(AnalyticsEvent.GenerateFactsAttempted.name) {
                param("topic", topic)
                param("languageTag", Locale.current.toLanguageTag())
                param("factsCount", 20)
                param("factsTemperature", 0.6)
            }
        }
        facts.value.getOrNull(index - 1)?.let { fact ->
            analytics.logEvent(AnalyticsEvent.FactRead.name) {
                param("topic", topic)
                param("fact", fact)
                param("readingDuration", readingDuration)
            }
        }
    }

    fun clearLastIndexBeforeNextGeneration() {
        Timber.i("clearLastIndexBeforeNextGeneration")
        lastIndexBeforeNextGeneration = null
    }

    private fun onFactReadingStarted() {
        readingDuration = 0L
        viewModelScope.launch {
            while (true) {
                delay(1000)
                readingDuration += 1000
            }
        }
    }
}
