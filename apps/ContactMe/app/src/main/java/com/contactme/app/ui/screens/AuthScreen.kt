package com.contactme.app.ui.screens

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.contactme.app.navigation.AuthMode
import com.contactme.app.ui.auth.AuthUiState
import com.contactme.app.ui.auth.AuthViewModel
import com.contactme.app.ui.theme.ContactMeSpacing

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    AuthContent(
        uiState = uiState,
        onEmailOrPhoneChanged = viewModel::onEmailOrPhoneChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onAuthModeChanged = viewModel::onAuthModeChanged,
        onSubmit = { viewModel.submit(onSuccess = onAuthSuccess) }
    )
}

@Composable
private fun AuthContent(
    uiState: AuthUiState,
    onEmailOrPhoneChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onAuthModeChanged: (AuthMode) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .navigationBarsPadding()
            .padding(
                horizontal = ContactMeSpacing.screenHorizontal,
                vertical = ContactMeSpacing.screenVertical
            ),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = uiState.authMode.title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Use email to continue with ContactMe.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
        )
        Spacer(modifier = Modifier.height(ContactMeSpacing.sectionGap))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = uiState.emailOrPhone,
            onValueChange = onEmailOrPhoneChanged,
            enabled = !uiState.isLoading,
            singleLine = true,
            label = { Text(text = "Email") }
        )
        Spacer(modifier = Modifier.height(ContactMeSpacing.fieldGap))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = uiState.password,
            onValueChange = onPasswordChanged,
            enabled = !uiState.isLoading,
            singleLine = true,
            label = { Text(text = "Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        uiState.errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(ContactMeSpacing.fieldGap))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
        Spacer(modifier = Modifier.height(ContactMeSpacing.contentGap))
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading,
            onClick = onSubmit
        ) {
            Text(
                text = if (uiState.isLoading) {
                    "Please wait..."
                } else {
                    uiState.authMode.actionLabel
                }
            )
        }
        Spacer(modifier = Modifier.height(ContactMeSpacing.fieldGap))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                enabled = !uiState.isLoading,
                onClick = { onAuthModeChanged(AuthMode.Login) }
            ) {
                Text(text = "Login")
            }
            OutlinedButton(
                modifier = Modifier.weight(1f),
                enabled = !uiState.isLoading,
                onClick = { onAuthModeChanged(AuthMode.Register) }
            ) {
                Text(text = "Register")
            }
        }
        Spacer(modifier = Modifier.height(ContactMeSpacing.contentGap))
    }
}
