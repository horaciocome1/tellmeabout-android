package io.github.horaciocome1.factsai.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import io.github.horaciocome1.factsai.R
import io.github.horaciocome1.factsai.ui.components.Background
import io.github.horaciocome1.factsai.ui.theme.FactsAITheme

@OptIn(ExperimentalFoundationApi::class)
@Destination
@Composable
fun FactsScreen(
    pagerState: PagerState = rememberPagerState(),
    showLoading: Boolean = false,
    showHint: Boolean = true,
    showClose: Boolean = true,
    facts: List<String> = emptyList(),
    onNextFact: () -> Unit = {
    },
    onClose: () -> Unit = {
    },
) {
    val infiniteTransition = rememberInfiniteTransition()

    val hintScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    val hintColor by infiniteTransition.animateColor(
        initialValue = MaterialTheme.colorScheme.primary.copy(alpha = 0.0f),
        targetValue = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse,
        ),
    )

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
                    AnimatedVisibility(
                        visible = showClose,
                        modifier = Modifier
                            .size(70.dp)
                            .padding(24.dp)
                            .align(Alignment.TopEnd),
                    ) {
                        IconButton(onClick = onClose) {
                            Image(
                                painter = painterResource(id = R.drawable.close),
                                contentDescription = "Close button",
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                            )
                        }
                    }
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
                        visible = showLoading && currentPage == facts.lastIndex,
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
                    AnimatedVisibility(
                        visible = showHint,
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .align(Alignment.BottomCenter),
                    ) {
                        Surface(
                            onClick = onNextFact,
                            shape = MaterialTheme.shapes.medium,
                            color = hintColor,
                            modifier = Modifier.scale(hintScale),
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.arrow_down),
                                contentDescription = "Down arrow",
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(16.dp),
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surface),
                            )
                        }
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
            showLoading = false,
            showHint = true,
            facts = listOf(
                "In some African cultures, " +
                    "a family’s wealth and status is determined by " +
                    "the number of cows they own.",
                "The first o nline transaction was a drug deal.",
            ),
        )
    }
}
