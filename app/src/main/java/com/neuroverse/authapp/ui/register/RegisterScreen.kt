package com.neuroverse.authapp.ui.register

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
import androidx.compose.ui.text.input.KeyboardCapitalization
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
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegistered: () -> Unit,
    onGoToLogin: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.registered) {
        if (state.registered) onRegistered()
    }

    RegisterContent(
        state = state,
        onNameChange = viewModel::onNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onTogglePassword = viewModel::onTogglePasswordVisibility,
        onSubmit = viewModel::onSubmit,
        onGoToLogin = onGoToLogin
    )
}

/** Stateless: renders state and forwards user actions. */
@Composable
fun RegisterContent(
    state: RegisterUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onSubmit: () -> Unit,
    onGoToLogin: () -> Unit
) {
    val nameError = state.nameError
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
            text = "Create account",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = state.name,
            onValueChange = onNameChange,
            label = { Text("Full name") },
            singleLine = true,
            enabled = !state.isLoading,
            isError = nameError != null,
            supportingText = if (nameError != null) {
                { Text(fieldErrorMessage(nameError)) }
            } else null,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

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
                Text("Register")
            }
        }

        Spacer(Modifier.height(8.dp))
        TextButton(
            onClick = onGoToLogin,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Already have an account? Log in")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterContentPreview() {
    NeuroVerseAuthAppTheme {
        RegisterContent(
            state = RegisterUiState(
                name = "A",
                email = "not-an-email",
                password = "secret",
                nameError = FieldError.NAME_TOO_SHORT,
                emailError = FieldError.INVALID_EMAIL,
                passwordError = FieldError.PASSWORD_TOO_SHORT,
                formError = AuthError.EMAIL_ALREADY_IN_USE
            ),
            onNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onTogglePassword = {},
            onSubmit = {},
            onGoToLogin = {}
        )
    }
}
