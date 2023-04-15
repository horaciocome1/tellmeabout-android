package io.github.horaciocome1.factsai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.tooling.preview.Preview
import io.github.horaciocome1.factsai.ui.theme.FactsAITheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Background(
    onClick: () -> Unit = {
    },
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surface)
            .pointerInteropFilter {
                onClick()
                false
            },
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize(0.9f)
                .windowInsetsPadding(WindowInsets.systemBars),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.primary.copy(
                alpha = 0.05f,
            ),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                content = content,
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun BackgroundPreview() {
    FactsAITheme(darkTheme = true) {
        Background {
        }
    }
}
