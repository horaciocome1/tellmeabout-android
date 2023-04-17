package io.github.horaciocome1.factsai.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import io.github.horaciocome1.factsai.R
import io.github.horaciocome1.factsai.ui.components.Background
import io.github.horaciocome1.factsai.ui.screens.destinations.HomeScreenDestination
import io.github.horaciocome1.factsai.ui.theme.FactsAITheme
import io.github.horaciocome1.factsai.util.Constants.DisabledAlpha
import io.github.horaciocome1.factsai.util.Constants.FocusedAlpha
import io.github.horaciocome1.factsai.util.Constants.UnfocusedAlpha
import io.github.horaciocome1.factsai.util.FakeDestinationsNavigator

@Destination
@Composable
fun EnterOtpScreen(
    mobileNumber: String,
    navigator: DestinationsNavigator,
    viewModel: EnterOtpViewModel = hiltViewModel(),
) {
    val focusManager = LocalFocusManager.current
    val focusRequesters = remember { List(6) { FocusRequester() } }

    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val codeValidated by viewModel.codeValidated.collectAsStateWithLifecycle(
        initialValue = false,
    )

    LaunchedEffect(codeValidated) {
        if (codeValidated) {
            navigator.popBackStack(HomeScreenDestination.route, inclusive = false)
        }
    }

    Background(onClick = focusManager::clearFocus) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(id = R.string.otp_verification),
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(horizontal = 32.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                viewModel.digits.forEachIndexed { index, digit ->
                    OutlinedTextField(
                        value = digit.value,
                        onValueChange = {
                            digit.value = it
                            if (it.length == 1 && index < viewModel.digits.lastIndex) {
                                focusManager.moveFocus(FocusDirection.Right)
                            }
                        },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
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
                            imeAction = if (index == viewModel.digits.lastIndex) {
                                ImeAction.Go
                            } else {
                                ImeAction.Next
                            },
                            keyboardType = KeyboardType.Number,
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                if (index < viewModel.digits.lastIndex) {
                                    focusManager.moveFocus(FocusDirection.Right)
                                }
                            },
                            onGo = {
                                viewModel.validateCode()
                            },
                        ),
                        modifier = Modifier
                            .width(48.dp)
                            .focusRequester(focusRequesters[index]),
                    )
                }
            }
            Text(
                text = buildAnnotatedString {
                    append(stringResource(id = R.string.enter_otp_sent_to))
                    withStyle(
                        style = SpanStyle(
                            color = if (error.first) {
                                MaterialTheme.colorScheme.error.copy(
                                    alpha = FocusedAlpha,
                                )
                            } else {
                                MaterialTheme.colorScheme.primary.copy(
                                    alpha = FocusedAlpha,
                                )
                            },
                        ),
                    ) {
                        append(" ")
                        append(mobileNumber)
                    }
                },
                style = MaterialTheme.typography.labelLarge.copy(
                    color = if (error.first) {
                        MaterialTheme.colorScheme.error.copy(
                            alpha = FocusedAlpha,
                        )
                    } else {
                        MaterialTheme.colorScheme.primary.copy(
                            alpha = UnfocusedAlpha,
                        )
                    },
                ),
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(
                        start = 32.dp,
                        end = 32.dp,
                        top = 16.dp,
                    ),
            )
            Spacer(modifier = Modifier.height(100.dp))
            Button(
                onClick = viewModel::validateCode,
                enabled = !loading && !error.first && viewModel.digits.all { it.value.isNotEmpty() },
            ) {
                Text(
                    text = stringResource(id = R.string.verify_otp),
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
fun EnterOtpScreenPreview() {
    FactsAITheme(darkTheme = true) {
        EnterOtpScreen(
            mobileNumber = "+351 912 345 678",
            navigator = FakeDestinationsNavigator,
        )
    }
}
