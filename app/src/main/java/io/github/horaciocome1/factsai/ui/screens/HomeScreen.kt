package io.github.horaciocome1.factsai.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import io.github.horaciocome1.factsai.ui.screens.destinations.EnterMobileNumberScreenDestination
import kotlinx.coroutines.launch

@RootNavGraph(start = true)
@Destination
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navigator: DestinationsNavigator,
) {
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()
    val topic = rememberSaveable { mutableStateOf("") }

    BackHandler(enabled = pagerState.currentPage != 0) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(0)
        }
    }

    LaunchedEffect(topic.value) {
        pagerState.animateScrollToPage(0)
    }

    HorizontalPager(
        pageCount = 2,
        modifier = Modifier.fillMaxSize(),
        state = pagerState,
    ) { page ->
        when (page) {
            0 -> FactsScreen(topic = topic.value)
            1 -> EnterTopicScreen(
                close = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(0)
                    }
                },
                topicEntered = { t ->
                    topic.value = t
                },
                login = {
                    navigator.navigate(EnterMobileNumberScreenDestination)
                },
            )
        }
    }
}
