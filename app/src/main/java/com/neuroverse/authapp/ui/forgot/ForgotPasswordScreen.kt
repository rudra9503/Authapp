package com.neuroverse.authapp.ui.forgot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neuroverse.authapp.domain.model.AuthError
import com.neuroverse.authapp.domain.validation.FieldError
import com.neuroverse.authapp.ui.common.authErrorMessage
import com.neuroverse.authapp.ui.common.fieldErrorMessage
import com.neuroverse.authapp.ui.theme.NeuroVerseAuthAppTheme

/** Stateful wrapper: connects the ViewModel to the stateless content. */
@Composable
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel,
    onBackToLogin: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ForgotPasswordContent(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onSubmit = viewModel::onSubmit,
        onBackToLogin = onBackToLogin
    )
}

/** Stateless: renders state and forwards user actions. */
@Composable
fun ForgotPasswordContent(
    state: ForgotPasswordUiState,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val emailError = state.emailError
    val formError = state.formError

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        if (state.resetSent) {
            // Neutral wording: identical whether or not the email has an account.
            Text(
                text = "Check your email",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "If an account exists for this email, we have sent a link to reset the password.",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onBackToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Back to login")
            }
        } else {
            Text(
                text = "Reset password",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Enter your email and we will send you a reset link.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                singleLine = true,
                enabled = !state.isLoading,
                isError = emailError != null,
                supportingText = if (emailError != null) {
                    { Text(fieldErrorMessage(emailError)) }
                } else null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                modifier = Modifier.fillMaxWidth()
            )

            if (formError != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = authErrorMessage(formError),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onSubmit,
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Send reset link")
                }
            }

            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = onBackToLogin,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to login")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ForgotPasswordFormPreview() {
    NeuroVerseAuthAppTheme {
        ForgotPasswordContent(
            state = ForgotPasswordUiState(
                email = "not-an-email",
                emailError = FieldError.INVALID_EMAIL,
                formError = AuthError.NETWORK
            ),
            onEmailChange = {},
            onSubmit = {},
            onBackToLogin = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ForgotPasswordSentPreview() {
    NeuroVerseAuthAppTheme {
        ForgotPasswordContent(
            state = ForgotPasswordUiState(email = "asha@example.com", resetSent = true),
            onEmailChange = {},
            onSubmit = {},
            onBackToLogin = {}
        )
    }
}
