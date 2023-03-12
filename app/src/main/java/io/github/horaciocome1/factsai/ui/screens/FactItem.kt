package io.github.horaciocome1.factsai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.horaciocome1.factsai.ui.theme.FactsAITheme

@Composable
fun FactItem(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium.copy(
                color = MaterialTheme.colorScheme.primary,
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FactItemPreview() {
    FactsAITheme(darkTheme = true) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
        ) {
            FactItem(
                text = "In some African cultures, " +
                    "a family’s wealth and status is determined by " +
                    "the number of cows they own.",
                modifier = Modifier
                    .fillMaxSize(0.9f)
                    .clip(
                        shape = MaterialTheme.shapes.large,
                    )
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(
                            alpha = 0.05f,
                        ),
                    ),
            )
        }
    }
}
