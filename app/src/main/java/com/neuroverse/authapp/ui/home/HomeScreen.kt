package com.neuroverse.authapp.ui.home

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
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenProfile: () -> Unit,
    onLoggedOut: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.loggedOut) {
        if (state.loggedOut) onLoggedOut()
    }

    HomeContent(
        state = state,
        onOpenProfile = onOpenProfile,
        onLogout = viewModel::onLogout
    )
}

/** Stateless: renders state and forwards user actions. */
@Composable
fun HomeContent(
    state: HomeUiState,
    onOpenProfile: () -> Unit,
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
            text = "Welcome, ${user.name}",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = user.email,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onOpenProfile,
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("View profile")
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
private fun HomeContentPreview() {
    NeuroVerseAuthAppTheme {
        HomeContent(
            state = HomeUiState(
                user = User(uid = "uid-1", name = "Asha", email = "asha@example.com"),
                isLoading = false
            ),
            onOpenProfile = {},
            onLogout = {}
        )
    }
}
