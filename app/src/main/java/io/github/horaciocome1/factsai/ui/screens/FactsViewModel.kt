package io.github.horaciocome1.factsai.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.horaciocome1.factsai.data.Api
import io.github.horaciocome1.factsai.data.PreferencesHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FactsViewModel @Inject constructor(
    private val api: Api,
    private val preferencesHelper: PreferencesHelper,
) : ViewModel() {

    private val _facts = MutableStateFlow(emptyList<String>())
    val facts = _facts.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    var lastIndexBeforeNextGeneration: Int? = null
        private set

    private var generateFactsJob: Job? = null

    init {
        Timber.i("init")
        viewModelScope.launch {
            api.facts.collect { newFacts ->
                _facts.update { currentState ->
                    currentState + newFacts
                }
            }
        }
    }

    fun onFactRead(index: Int, topic: String) {
        Timber.i("onFactRead index=$index topic=$topic lastIndexBeforeNextGeneration=$lastIndexBeforeNextGeneration")
        val remainingUnreadFacts = _facts.value.lastIndex - index
        if (remainingUnreadFacts < 5 && generateFactsJob?.isActive != true) {
            lastIndexBeforeNextGeneration = _facts.value.lastIndex
            generateFactsJob = viewModelScope.launch {
                _loading.value = true

                val installationId = preferencesHelper.getString(Api.Constants.KEY_INSTALLATION_ID)
                if (installationId == null) {
                    Timber.w("onFactRead installationId is null")
                    _loading.value = false
                    return@launch
                }

                api.generateFacts(installationId, topic, 20, 0.6f)
                _loading.value = false
            }
        }
    }

    fun clearLastIndexBeforeNextGeneration() {
        Timber.i("clearLastIndexBeforeNextGeneration")
        lastIndexBeforeNextGeneration = null
    }
}
