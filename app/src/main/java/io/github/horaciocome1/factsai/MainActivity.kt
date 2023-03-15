package io.github.horaciocome1.factsai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import com.ramcosta.composedestinations.DestinationsNavHost
import dagger.hilt.android.AndroidEntryPoint
import io.github.horaciocome1.factsai.data.Api
import io.github.horaciocome1.factsai.data.PreferencesHelper
import io.github.horaciocome1.factsai.ui.screens.NavGraphs
import io.github.horaciocome1.factsai.ui.theme.FactsAITheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private const val KEY_INSTALLATION_REGISTERED = "installationRegistered"
    }

    @Inject
    lateinit var preferencesHelper: PreferencesHelper

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var api: Api

    private var checkInstallationJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.i("onCreate")

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            FactsAITheme(darkTheme = true) {
                val systemUiController = rememberSystemUiController()

                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = false,
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    DestinationsNavHost(
                        navGraph = NavGraphs.root,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }

        checkInstallationJob?.cancel()
        checkInstallationJob = lifecycleScope.launch {
            checkInstallation()
        }
    }

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
