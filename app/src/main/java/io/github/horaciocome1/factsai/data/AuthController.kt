package io.github.horaciocome1.factsai.data

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext

interface AuthController : AuthStateListener {

    companion object {
        const val TIMEOUT_OTP_VERIFICATION_IN_SECONDS = 120L
        const val KEY_INSTALLATION_REGISTERED = "installationRegistered"
    }

    val coroutineContext: CoroutineContext

    val verificationResult: Flow<VerificationResult?>
    val signedIn: Flow<Boolean>

    fun sendVerificationCode(activity: Activity, mobileNumber: String)

    fun verifyCode(code: String)

    enum class VerificationResult {
        CodeSent,
        VerificationCompleted,
        Failure,
        TooManyRequests,
        InvalidCredentials,
        InvalidVerificationCode,
    }
}
