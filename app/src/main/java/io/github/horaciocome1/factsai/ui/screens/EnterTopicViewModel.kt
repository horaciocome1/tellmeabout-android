package io.github.horaciocome1.factsai.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.perf.ktx.trace
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.horaciocome1.factsai.data.Api
import io.github.horaciocome1.factsai.data.PreferencesHelper
import io.github.horaciocome1.factsai.util.AnalyticsEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class EnterTopicViewModel @Inject constructor(
    private val api: Api,
    private val preferencesHelper: PreferencesHelper,
    private val analytics: FirebaseAnalytics,
) : ViewModel() {

    companion object {
        const val MAX_TOPIC_LENGTH = 20
    }

    var topic by mutableStateOf("")
        private set

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _error = MutableStateFlow(false)
    val error = _error.asStateFlow()

    private val _factsGenerated = MutableSharedFlow<Boolean>()
    val factsGenerated = _factsGenerated.asSharedFlow()

    init {
        Timber.i("init")
    }

    fun onTopicChange(topic: String) {
        Timber.i("onTopicChanged topic=$topic")
        if (topic.length > MAX_TOPIC_LENGTH) {
            Timber.w("onTopicChanged topic=$topic is too long")
            return
        }
        this.topic = topic
    }

    fun generateFacts() {
        Timber.i("generateFacts")
        viewModelScope.launch {
            trace("EnterTopicScreenViewModel:generateFacts") {
                _loading.value = true

                val installationId = preferencesHelper.getString(Api.Constants.KEY_INSTALLATION_ID)
                if (installationId == null) {
                    Timber.w("generateFacts installationId is null")
                    _error.value = true
                    _loading.value = false
                    return@launch
                }

                when (val result = api.generateFacts(installationId, topic.trim(), Locale.current.toLanguageTag(), 4, 0f)) {
                    is Api.Result.Failure -> {
                        Timber.e("generateFacts error message=${result.errorMessage}")
                        _error.value = true
                        analytics.logEvent(AnalyticsEvent.GenerateFactsFailed.name) {
                            param("topic", topic)
                            param("languageTag", Locale.current.toLanguageTag())
                            param("factsCount", 4)
                            param("factsTemperature", 0)
                            param("errorMessage", result.errorMessage)
                        }
                    }
                    is Api.Result.Success<*> -> {
                        _factsGenerated.emit(value = true)
                        analytics.logEvent(AnalyticsEvent.GenerateFactsSucceeded.name) {
                            param("topic", topic)
                            param("languageTag", Locale.current.toLanguageTag())
                            param("factsCount", 4)
                            param("factsTemperature", 0)
                        }
                    }
                }

                _loading.value = false
            }
        }
        analytics.logEvent(AnalyticsEvent.GenerateFactsAttemptedWithNewTopic.name) {
            param("topic", topic)
            param("languageTag", Locale.current.toLanguageTag())
            param("factsCount", 4)
            param("factsTemperature", 0)
        }
    }
}
