package io.github.horaciocome1.factsai.data

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.perf.ktx.trace
import com.google.firebase.perf.metrics.AddTrace
import io.github.horaciocome1.factsai.data.AuthController.Companion.KEY_INSTALLATION_REGISTERED
import io.github.horaciocome1.factsai.data.AuthController.Companion.TIMEOUT_OTP_VERIFICATION_IN_SECONDS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class AuthControllerImpl @Inject constructor(
    override val coroutineContext: CoroutineContext,
    private val auth: FirebaseAuth,
    private val preferencesHelper: PreferencesHelper,
    private val api: Api,
) : AuthController, PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

    private val coroutineScope = CoroutineScope(coroutineContext)

    private val _verificationResult = MutableSharedFlow<AuthController.VerificationResult?>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val verificationResult = _verificationResult.asSharedFlow().distinctUntilChanged()

    private val _signedIn = MutableSharedFlow<Boolean>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val signedIn = _signedIn.asSharedFlow().distinctUntilChanged()

    private var verificationId: String? = null
    private var token: PhoneAuthProvider.ForceResendingToken? = null

    init {
        Timber.i("init")
        auth.addAuthStateListener(this)
        coroutineScope.launch {
            checkInstallation()
        }
    }

    @AddTrace(name = "AuthController:sendVerificationCode")
    override fun sendVerificationCode(activity: Activity, mobileNumber: String) {
        Timber.v("sendVerificationCode mobileNumber=$mobileNumber")
        coroutineScope.launch {
            _verificationResult.emit(null)
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(mobileNumber)
            .setTimeout(TIMEOUT_OTP_VERIFICATION_IN_SECONDS, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(this)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override fun verifyCode(code: String) {
        Timber.v("verifyCode code=$code")
        coroutineScope.launch {
            trace(name = "AuthController:verifyCode") {
                _verificationResult.emit(null)

                val id = verificationId
                if (id == null) {
                    Timber.w("verifyCode verificationId is null")
                    _verificationResult.emit(AuthController.VerificationResult.Failure)
                    return@launch
                }

                val credential = PhoneAuthProvider.getCredential(id, code)
                signInWithPhoneAuthCredential(credential)
            }
        }
    }

    override fun onAuthStateChanged(auth: FirebaseAuth) {
        Timber.d("onAuthStateChanged currentUserUid=${auth.currentUser?.uid}")
        coroutineScope.launch {
            _signedIn.emit(auth.currentUser != null && auth.currentUser?.isAnonymous != true)
        }
    }

    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
        Timber.d("onVerificationCompleted credential=$credential")
        coroutineScope.launch {
            _verificationResult.emit(AuthController.VerificationResult.VerificationCompleted)
        }
        onAuthStateChanged(auth)
    }

    override fun onVerificationFailed(exception: FirebaseException) {
        Timber.e("onVerificationFailed", exception)
        coroutineScope.launch {
            when (exception) {
                is FirebaseAuthInvalidCredentialsException -> {
                    _verificationResult.emit(AuthController.VerificationResult.InvalidCredentials)
                }
                is FirebaseTooManyRequestsException -> {
                    _verificationResult.emit(AuthController.VerificationResult.TooManyRequests)
                }
                else -> {
                    _verificationResult.emit(AuthController.VerificationResult.Failure)
                }
            }
        }
    }

    override fun onCodeAutoRetrievalTimeOut(verificationId: String) {
        Timber.w("onCodeAutoRetrievalTimeOut verificationId=$verificationId")
    }

    override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
        Timber.d("onCodeSent verificationId=$verificationId token=$token")
        coroutineScope.launch {
            _verificationResult.emit(AuthController.VerificationResult.CodeSent)
        }
        this.verificationId = verificationId
        this.token = token
    }

    @AddTrace(name = "AuthController:signInWithPhoneAuthCredential")
    private suspend fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        Timber.i("signInWithPhoneAuthCredential credential=$credential")
        try {
            auth.currentUser?.linkWithCredential(credential)?.await()
            _verificationResult.emit(AuthController.VerificationResult.VerificationCompleted)
            onAuthStateChanged(auth)
        } catch (exception: FirebaseAuthInvalidCredentialsException) {
            Timber.e("signInWithPhoneAuthCredential", exception)
            _verificationResult.emit(AuthController.VerificationResult.InvalidVerificationCode)
        } catch (exception: Exception) {
            Timber.e("signInWithPhoneAuthCredential", exception)
            _verificationResult.emit(AuthController.VerificationResult.Failure)
        }
    }

    @AddTrace(name = "AuthController:checkInstallation")
    private suspend fun checkInstallation() {
        Timber.i("checkInstallation")
        if (preferencesHelper.getString(Api.Constants.KEY_INSTALLATION_ID).isNullOrBlank()) {
            Timber.w("installationId is null or blank")
            preferencesHelper.commit(
                Api.Constants.KEY_INSTALLATION_ID,
                UUID.randomUUID().toString(),
            )
        }
        if (auth.currentUser == null) {
            Timber.w("auth.currentUser is null")
            auth.signInAnonymously().await()
        }
        if (!preferencesHelper.getBoolean(KEY_INSTALLATION_REGISTERED)) {
            Timber.w("installation is not registered")
            api.registerInstallation(
                preferencesHelper.getString(Api.Constants.KEY_INSTALLATION_ID)!!,
            )
            preferencesHelper.commit(KEY_INSTALLATION_REGISTERED, true)
        }
    }
}
