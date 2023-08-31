package io.github.horaciocome1.factsai

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.perf.metrics.AddTrace
import dagger.hilt.android.HiltAndroidApp
import io.github.horaciocome1.factsai.data.AuthController
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class FactsAiApplication : Application() {

    @Inject
    lateinit var authController: AuthController

    @AddTrace(name = "FactsAiApplication:onCreate")
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        FirebaseApp.initializeApp(this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        val factory = PlayIntegrityAppCheckProviderFactory.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(factory)
        authController.setup()
    }
}
