package io.github.horaciocome1.factsai.ui.screens

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class EnterMobileNumberViewModel @Inject constructor(
    private val authController: AuthController,
) : ViewModel() {

    companion object {
        private const val MAX_MOBILE_NUMBER_LENGTH = 15
    }

    var mobileNumber by mutableStateOf("")
        private set

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _error = MutableStateFlow(false to "")
    val error = _error.asStateFlow()

    private val _codeSent = MutableSharedFlow<Boolean>()
    val codeSent = _codeSent.asSharedFlow()

    init {
        Timber.i("init")
        viewModelScope.launch {
            authController.verificationResult.collectLatest { result ->
                Timber.d("init verificationResult=$result")
                when (result) {
                    AuthController.VerificationResult.CodeSent -> {
                        _codeSent.emit(true)
                        _loading.value = false
                    }
                    AuthController.VerificationResult.Failure -> {
                        _error.value = true to "Error sending verification code. Try again later."
                        _loading.value = false
                    }
                    AuthController.VerificationResult.TooManyRequests -> {
                        _error.value = true to "Too many requests. Try again later."
                        _loading.value = false
                    }
                    AuthController.VerificationResult.InvalidCredentials -> {
                        _error.value = true to "Invalid credentials. Check your number"
                        _loading.value = false
                    }
                    else -> Unit
                }
            }
        }
    }

    fun onMobileNumberChanged(mobileNumber: String) {
        Timber.i("onMobileNumberChanged mobileNumber=$mobileNumber")
        _error.value = false to ""
        if (mobileNumber.length > MAX_MOBILE_NUMBER_LENGTH) {
            Timber.w("onMobileNumberChanged mobileNumber=$mobileNumber is too long")
            return
        }
        this.mobileNumber = mobileNumber
        viewModelScope.launch {
            _error.emit(false to "")
        }
    }

    fun sendVerificationCode(activity: Activity) {
        Timber.i("sendVerificationCode mobileNumber=$mobileNumber")
        _loading.value = true
        _error.value = false to ""
        authController.sendVerificationCode(activity, mobileNumber)
    }
}
