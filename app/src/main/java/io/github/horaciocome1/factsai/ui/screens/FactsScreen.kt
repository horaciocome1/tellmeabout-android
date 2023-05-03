package io.github.horaciocome1.factsai.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import io.github.horaciocome1.factsai.ui.components.Background
import io.github.horaciocome1.factsai.ui.theme.FactsAITheme

@OptIn(ExperimentalFoundationApi::class)
@Destination
@Composable
fun FactsScreen(
    pagerState: PagerState = rememberPagerState(),
    loading: Boolean = false,
    facts: List<String> = emptyList(),
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        VerticalPager(
            pageCount = facts.size,
            state = pagerState,
            key = { facts[it] },
            modifier = Modifier.fillMaxSize(),
        ) { currentPage ->
            Background {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = facts[currentPage],
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                        ),
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(16.dp),
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
}

@OptIn(ExperimentalFoundationApi::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FactsScreenPreview() {
    FactsAITheme(darkTheme = true) {
        FactsScreen(
            pagerState = rememberPagerState(),
            loading = true,
            facts = listOf(
                "In some African cultures, " +
                    "a family’s wealth and status is determined by " +
                    "the number of cows they own.",
                "The first o nline transaction was a drug deal.",
            ),
        )
    }
}
