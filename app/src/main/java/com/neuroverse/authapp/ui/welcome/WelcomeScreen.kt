package com.neuroverse.authapp.ui.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.neuroverse.authapp.ui.theme.NeuroVerseAuthAppTheme

@Composable
fun WelcomeScreen(
    onRegister: () -> Unit,
    onLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Create an account or log in to continue.",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onRegister,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Create account")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Log in")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WelcomeScreenPreview() {
    NeuroVerseAuthAppTheme {
        WelcomeScreen(onRegister = {}, onLogin = {})
    }
}
