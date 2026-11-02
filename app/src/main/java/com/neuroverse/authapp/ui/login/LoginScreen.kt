package com.neuroverse.authapp.ui.login

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoggedIn: () -> Unit,
    onForgotPassword: () -> Unit,
    onGoToRegister: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.loggedIn) {
        if (state.loggedIn) onLoggedIn()
    }

    LoginContent(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onTogglePassword = viewModel::onTogglePasswordVisibility,
        onSubmit = viewModel::onSubmit,
        onForgotPassword = onForgotPassword,
        onGoToRegister = onGoToRegister
    )
}

/** Stateless: renders state and forwards user actions. */
@Composable
fun LoginContent(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onSubmit: () -> Unit,
    onForgotPassword: () -> Unit,
    onGoToRegister: () -> Unit
) {
    val emailError = state.emailError
    val passwordError = state.passwordError
    val formError = state.formError

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Log in",
            style = MaterialTheme.typography.headlineMedium
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
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
            singleLine = true,
            enabled = !state.isLoading,
            isError = passwordError != null,
            supportingText = if (passwordError != null) {
                { Text(fieldErrorMessage(passwordError)) }
            } else null,
            visualTransformation = if (state.isPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                TextButton(onClick = onTogglePassword) {
                    Text(if (state.isPasswordVisible) "Hide" else "Show")
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
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

        Spacer(Modifier.height(8.dp))
        TextButton(
            onClick = onForgotPassword,
            enabled = !state.isLoading
        ) {
            Text("Forgot password?")
        }

        Spacer(Modifier.height(16.dp))
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
                Text("Log in")
            }
        }

        Spacer(Modifier.height(8.dp))
        TextButton(
            onClick = onGoToRegister,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("New here? Create an account")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginContentPreview() {
    NeuroVerseAuthAppTheme {
        LoginContent(
            state = LoginUiState(
                email = "not-an-email",
                emailError = FieldError.INVALID_EMAIL,
                formError = AuthError.INVALID_CREDENTIALS
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onTogglePassword = {},
            onSubmit = {},
            onForgotPassword = {},
            onGoToRegister = {}
        )
    }
}
