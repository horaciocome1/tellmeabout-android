package io.github.horaciocome1.factsai.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import io.github.horaciocome1.factsai.R
import io.github.horaciocome1.factsai.ui.components.Background
import io.github.horaciocome1.factsai.ui.screens.EnterTopicViewModel.Companion.MAX_TOPIC_LENGTH
import io.github.horaciocome1.factsai.ui.theme.FactsAITheme
import io.github.horaciocome1.factsai.util.Constants.DisabledAlpha
import io.github.horaciocome1.factsai.util.Constants.FocusedAlpha
import io.github.horaciocome1.factsai.util.Constants.UnfocusedAlpha

@RootNavGraph(start = true)
@Destination
@Composable
fun EnterTopicScreen(
    error: Pair<Boolean, Int> = Pair(false, R.string.error_something_went_wrong),
    loading: Boolean = false,
    topic: String = "",
    onTopicChange: (String) -> Unit = {
    },
    generateFacts: () -> Unit = {
    },
) {
    val focusManager = LocalFocusManager.current

    Background(onClick = focusManager::clearFocus) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(id = R.string.tell_me_about),
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(horizontal = 32.dp),
            )
            OutlinedTextField(
                value = topic,
                onValueChange = onTopicChange,
                singleLine = true,
                supportingText = {
                    val annotatedString = buildAnnotatedString {
                        if (error.first) {
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.error,
                                ),
                            ) {
                                append(stringResource(id = error.second))
                            }
                            return@buildAnnotatedString
                        }
                        append(stringResource(id = R.string.enter_a_topic_to_get_started))
                        append(" ")
                        append(stringResource(id = R.string.for_example))
                        append(", '")
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.primary.copy(
                                    alpha = FocusedAlpha,
                                ),
                            ),
                        ) {
                            append(stringResource(id = R.string.cats))
                        }
                        append("' ")
                        append(stringResource(id = R.string.or))
                        append(" '")
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.primary.copy(
                                    alpha = FocusedAlpha,
                                ),
                            ),
                        ) {
                            append(stringResource(id = R.string.african_families))
                        }
                        append("'.")
                    }
                    Text(
                        text = annotatedString,
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.primary.copy(
                                alpha = UnfocusedAlpha,
                            ),
                        ),
                    )
                },
                suffix = {
                    Text(
                        text = "${topic.length}/$MAX_TOPIC_LENGTH",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary.copy(
                                alpha = UnfocusedAlpha,
                            ),
                        ),
                    )
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.primary,
                ),
                shape = MaterialTheme.shapes.small,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(
                        alpha = UnfocusedAlpha,
                    ),
                    disabledBorderColor = MaterialTheme.colorScheme.primary.copy(
                        alpha = DisabledAlpha,
                    ),
                ),
                enabled = !loading,
                isError = error.first,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Go,
                ),
                keyboardActions = KeyboardActions(
                    onGo = {
                        if (topic.isNotBlank() && !error.first) {
                            generateFacts()
                        }
                        focusManager.clearFocus()
                    },
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 24.dp,
                        top = 16.dp,
                        end = 24.dp,
                    ),
            )
            Spacer(modifier = Modifier.height(100.dp))
            Button(
                onClick = generateFacts,
                enabled = !error.first && !loading && topic.isNotBlank(),
            ) {
                Text(
                    text = stringResource(id = R.string.get_started),
                    style = MaterialTheme.typography.bodyLarge,
                )
                AnimatedVisibility(visible = loading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .height(16.dp)
                            .width(32.dp)
                            .padding(start = 16.dp)
                            .align(Alignment.CenterVertically),
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
fun EnterTopicScreenPreview() {
    var topic by remember { mutableStateOf("") }
    FactsAITheme(darkTheme = true) {
        EnterTopicScreen(
            loading = false,
            error = false to R.string.error_something_went_wrong,
            topic = topic,
            onTopicChange = { topic = it },
            generateFacts = {
            },
        )
    }
}
