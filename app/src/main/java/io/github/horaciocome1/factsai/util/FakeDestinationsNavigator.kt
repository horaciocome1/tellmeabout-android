package io.github.horaciocome1.factsai.util

import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.Navigator
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

object FakeDestinationsNavigator : DestinationsNavigator {

    override fun clearBackStack(route: String) = false

    override fun navigate(route: String, onlyIfResumed: Boolean, navOptions: NavOptions?, navigatorExtras: Navigator.Extras?) {
    }

    override fun navigate(route: String, onlyIfResumed: Boolean, builder: NavOptionsBuilder.() -> Unit) {
    }

    override fun navigateUp() = false

    override fun popBackStack() = false

    override fun popBackStack(route: String, inclusive: Boolean, saveState: Boolean) = false
}
