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
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.perf.metrics.AddTrace
import com.ramcosta.composedestinations.DestinationsNavHost
import dagger.hilt.android.AndroidEntryPoint
import io.github.horaciocome1.factsai.ui.screens.NavGraphs
import io.github.horaciocome1.factsai.ui.theme.FactsAITheme
import io.github.horaciocome1.factsai.util.AppStateAnalytics
import io.github.horaciocome1.factsai.util.AppStateAnalyticsImpl
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity(), AppStateAnalytics by AppStateAnalyticsImpl() {

    @Inject
    lateinit var analytics: FirebaseAnalytics

    @AddTrace(name = "MainActivity:onCreate")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.i("onCreate")
        registerAppStateAnalytics(this, analytics)

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
    }
}
