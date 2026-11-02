package com.neuroverse.authapp.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neuroverse.authapp.domain.model.User
import com.neuroverse.authapp.ui.theme.NeuroVerseAuthAppTheme

/** Stateful wrapper: connects the ViewModel to the stateless content. */
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit,
    onLoggedOut: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.loggedOut) {
        if (state.loggedOut) onLoggedOut()
    }

    ProfileContent(
        state = state,
        onBack = onBack,
        onLogout = viewModel::onLogout
    )
}

/** Stateless: renders state and forwards user actions. */
@Composable
fun ProfileContent(
    state: ProfileUiState,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        val user = state.user
        if (user == null) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            }
            return@Column
        }

        Text(
            text = "Your profile",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(24.dp))

        Text(text = "Name", style = MaterialTheme.typography.labelMedium)
        Text(text = user.name, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(16.dp))

        Text(text = "Email", style = MaterialTheme.typography.labelMedium)
        Text(text = user.email, style = MaterialTheme.typography.bodyLarge)

        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onBack,
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Back")
        }

        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onLogout,
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
                Text("Log out")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileContentPreview() {
    NeuroVerseAuthAppTheme {
        ProfileContent(
            state = ProfileUiState(
                user = User(uid = "uid-1", name = "Asha", email = "asha@example.com"),
                isLoading = false
            ),
            onBack = {},
            onLogout = {}
        )
    }
}
