package com.contactme.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.contactme.app.ui.profile.ProfileSetupUiState
import com.contactme.app.ui.profile.ProfileSetupViewModel
import com.contactme.app.ui.theme.ContactMeSpacing

@Composable
fun ProfileSetupScreen(
    onProfileReady: () -> Unit,
    viewModel: ProfileSetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ProfileSetupContent(
        uiState = uiState,
        onDisplayNameChanged = viewModel::onDisplayNameChanged,
        onUsernameChanged = viewModel::onUsernameChanged,
        onSaveProfile = {
            viewModel.saveProfile(onProfileReady = onProfileReady)
        }
    )
}

@Composable
private fun ProfileSetupContent(
    uiState: ProfileSetupUiState,
    onDisplayNameChanged: (String) -> Unit,
    onUsernameChanged: (String) -> Unit,
    onSaveProfile: () -> Unit
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
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Photo",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(ContactMeSpacing.contentGap))
        Text(
            text = "Set up your profile",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add the name people will see in ContactMe.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
        )
        Spacer(modifier = Modifier.height(ContactMeSpacing.contentGap))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = uiState.displayName,
            onValueChange = onDisplayNameChanged,
            enabled = !uiState.isLoading,
            singleLine = true,
            label = { Text(text = "Display name") }
        )
        Spacer(modifier = Modifier.height(ContactMeSpacing.fieldGap))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = uiState.username,
            onValueChange = onUsernameChanged,
            enabled = !uiState.isLoading,
            singleLine = true,
            label = { Text(text = "Username") },
            placeholder = { Text(text = "your_name") }
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
            onClick = onSaveProfile
        ) {
            Text(
                text = if (uiState.isLoading) {
                    "Saving..."
                } else {
                    "Save and continue"
                }
            )
        }
        Spacer(modifier = Modifier.height(ContactMeSpacing.contentGap))
    }
}
