package io.github.horaciocome1.factsai.ui.screens

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.horaciocome1.factsai.data.AuthController
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class EnterOtpViewModel @Inject constructor(
    private val authController: AuthController,
) : ViewModel() {

    val digits = List(6) { mutableStateOf("") }

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _error = MutableStateFlow(false to "")
    val error = _error.asStateFlow()

    private val _codeValidated = MutableSharedFlow<Boolean>()
    val codeValidated = _codeValidated.asSharedFlow()

    init {
        viewModelScope.launch {
            authController.verificationResult.collectLatest { result ->
                Timber.d("init verificationResult=$result")
                when (result) {
                    AuthController.VerificationResult.VerificationCompleted -> {
                        _codeValidated.emit(true)
                        _loading.value = false
                    }
                    AuthController.VerificationResult.Failure -> {
                        _error.value = true to "Error validating verification code. Try again later."
                        _loading.value = false
                    }
                    AuthController.VerificationResult.InvalidVerificationCode,
                    AuthController.VerificationResult.InvalidCredentials,
                    -> {
                        _error.value = true to "Invalid credentials. Check your code"
                        _loading.value = false
                    }
                    else -> Unit
                }
            }
        }
        combine(flows = digits.map { snapshotFlow { it.value } }) {
            _error.value = false to ""
        }.launchIn(viewModelScope)
    }

    fun validateCode() {
        Timber.i("validateCode digits=$digits")
        _loading.value = true
        _error.value = false to ""
        val code = digits.joinToString("") { it.value }
        authController.verifyCode(code)
    }
}
