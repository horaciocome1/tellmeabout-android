package io.github.horaciocome1.factsai.ui.screens

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.perf.FirebasePerformance
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.horaciocome1.factsai.data.AuthController
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class EnterOtpViewModel @Inject constructor(
    private val authController: AuthController,
    performance: FirebasePerformance,
) : ViewModel() {

    val digits = List(6) { mutableStateOf("") }

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _error = MutableStateFlow(false to "")
    val error = _error.asStateFlow()

    private val _codeValidated = MutableSharedFlow<Boolean>()
    val codeValidated = _codeValidated.asSharedFlow()

    private val verificationTrace = performance.newTrace("EnterOtpViewModel:validateCode")

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
                verificationTrace.putAttribute("result", result?.name.toString())
                verificationTrace.stop()
            }
        }
    }

    fun validateCode() {
        Timber.i("validateCode digits=$digits")
        verificationTrace.start()
        _loading.value = true
        _error.value = false to ""
        val code = digits.joinToString("") { it.value }
        authController.verifyCode(code)
    }
}
