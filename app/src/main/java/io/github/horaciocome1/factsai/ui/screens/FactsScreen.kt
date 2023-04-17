package io.github.horaciocome1.factsai.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.horaciocome1.factsai.ui.theme.FactsAITheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FactsScreen(
    topic: String,
    viewModel: FactsViewModel = hiltViewModel(),
) {
    val facts by viewModel.facts.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val jumpToIndex by viewModel.jumpToIndex.collectAsStateWithLifecycle(
        initialValue = null,
    )

    val pagerState = rememberPagerState()

    LaunchedEffect(topic, pagerState.currentPage) {
        viewModel.onFactRead(pagerState.currentPage, topic)
    }

    LaunchedEffect(loading) {
        if (pagerState.currentPage == viewModel.lastIndexBeforeNextGeneration && !loading) {
            pagerState.animateScrollToPage(pagerState.currentPage + 1)
            viewModel.clearLastIndexBeforeNextGeneration()
        }
    }

    LaunchedEffect(jumpToIndex) {
        if (jumpToIndex != null) {
            pagerState.animateScrollToPage(jumpToIndex!!)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedVisibility(
                visible = facts.isEmpty(),
                modifier = Modifier
                    .height(16.dp)
                    .width(16.dp),
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp,
                )
            }
        }
        VerticalPager(
            pageCount = facts.size,
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { currentPage ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                FactItem(
                    text = facts[currentPage],
                    modifier = Modifier
                        .fillMaxSize(0.9f)
                        .windowInsetsPadding(WindowInsets.systemBars)
                        .clip(
                            shape = MaterialTheme.shapes.large,
                        )
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.05f,
                            ),
                        ),
                )
                AnimatedVisibility(
                    visible = loading && currentPage == facts.lastIndex,
                    modifier = Modifier
                        .height(32.dp)
                        .width(16.dp)
                        .padding(bottom = 16.dp)
                        .align(Alignment.BottomCenter),
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FactsScreenPreview() {
    FactsAITheme(darkTheme = true) {
        FactsScreen(
            topic = "Test",
        )
    }
}
