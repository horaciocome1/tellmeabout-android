package io.github.horaciocome1.factsai.util

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.analytics.FirebaseAnalytics

interface AppStateAnalytics {
    fun registerAppStateAnalytics(lifecycleOwner: LifecycleOwner, analytics: FirebaseAnalytics)
}

class AppStateAnalyticsImpl : AppStateAnalytics, LifecycleEventObserver {

    lateinit var analytics: FirebaseAnalytics

    override fun registerAppStateAnalytics(lifecycleOwner: LifecycleOwner, analytics: FirebaseAnalytics) {
        this.analytics = analytics
        lifecycleOwner.lifecycle.removeObserver(this)
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_START -> {
                analytics.logEvent(AnalyticsEvent.AppOpened.name, null)
            }
            Lifecycle.Event.ON_STOP -> {
                analytics.logEvent(AnalyticsEvent.AppClosed.name, null)
            }
            Lifecycle.Event.ON_PAUSE -> {
                analytics.logEvent(AnalyticsEvent.AppBackgrounded.name, null)
            }
            Lifecycle.Event.ON_RESUME -> {
                analytics.logEvent(AnalyticsEvent.AppForegrounded.name, null)
            }
            else -> Unit
        }
    }
}
