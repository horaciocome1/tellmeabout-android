package io.github.horaciocome1.factsai.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import io.github.horaciocome1.factsai.R
import io.github.horaciocome1.factsai.ui.components.Background
import io.github.horaciocome1.factsai.ui.screens.destinations.EnterOtpScreenDestination
import io.github.horaciocome1.factsai.ui.theme.FactsAITheme
import io.github.horaciocome1.factsai.util.Constants.DisabledAlpha
import io.github.horaciocome1.factsai.util.Constants.FocusedAlpha
import io.github.horaciocome1.factsai.util.Constants.UnfocusedAlpha
import io.github.horaciocome1.factsai.util.FakeDestinationsNavigator

@Destination
@Composable
fun EnterMobileNumberScreen(
    navigator: DestinationsNavigator,
    viewModel: EnterMobileNumberViewModel = hiltViewModel(),
) {
    val focusManager = LocalFocusManager.current
    val activity = LocalContext.current as Activity

    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val codeSent by viewModel.codeSent.collectAsStateWithLifecycle(
        initialValue = false,
    )

    LaunchedEffect(codeSent) {
        if (codeSent) {
            navigator.navigate(EnterOtpScreenDestination(viewModel.mobileNumber))
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
            OutlinedTextField(
                value = viewModel.mobileNumber,
                onValueChange = viewModel::onMobileNumberChanged,
                singleLine = true,
                supportingText = {
                    val annotatedString = buildAnnotatedString {
                        if (error.first) {
                            append(error.second)
                            return@buildAnnotatedString
                        }
                        append(stringResource(id = R.string.enter_mobile_number))
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
                            append(stringResource(id = R.string.country_code_mozambique))
                            append(stringResource(id = R.string.fake_number_mozambique))
                        }
                        append("'.")
                    }
                    Text(
                        text = annotatedString,
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
                    keyboardType = KeyboardType.Phone,
                ),
                keyboardActions = KeyboardActions(
                    onGo = {
                        if (viewModel.mobileNumber.isNotEmpty()) {
                            viewModel.sendVerificationCode(activity)
                            focusManager.clearFocus()
                        }
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
            Button(
                onClick = {
                    viewModel.sendVerificationCode(activity)
                },
                enabled = !loading && !error.first && viewModel.mobileNumber.isNotEmpty(),
                modifier = Modifier.padding(top = 48.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.get_otp),
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
fun EnterMobileNumberScreenPreview() {
    FactsAITheme(darkTheme = true) {
        EnterMobileNumberScreen(
            navigator = FakeDestinationsNavigator,
        )
    }
}
