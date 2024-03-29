package io.github.horaciocome1.factsai

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.ktx.performance
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.horaciocome1.factsai.data.Api
import io.github.horaciocome1.factsai.data.ApiImpl
import io.github.horaciocome1.factsai.data.AuthController
import io.github.horaciocome1.factsai.data.AuthControllerImpl
import io.github.horaciocome1.factsai.data.PreferencesHelper
import io.github.horaciocome1.factsai.data.PreferencesHelperImpl
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Module
@InstallIn(SingletonComponent::class)
object FactsAiModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context) = context

    @Provides
    @Singleton
    fun provideApi(impl: ApiImpl): Api = impl

    @Provides
    @Singleton
    fun provideFunctions(): FirebaseFunctions = Firebase.functions

    @Provides
    @Singleton
    fun provideCoroutineContext(): CoroutineContext = Dispatchers.IO

    @Provides
    @Singleton
    fun providePreferencesHelper(impl: PreferencesHelperImpl): PreferencesHelper = impl

    @Provides
    @Singleton
    fun provideAuth(): FirebaseAuth = Firebase.auth.apply { useAppLanguage() }

    @Provides
    @Singleton
    fun provideAuthController(impl: AuthControllerImpl): AuthController = impl

    @Provides
    @Singleton
    fun providePerformance(): FirebasePerformance = Firebase.performance

    @Provides
    @Singleton
    fun provideAnalytics(): FirebaseAnalytics = Firebase.analytics
}
