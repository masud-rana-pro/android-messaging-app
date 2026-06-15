package com.contactme.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.contactme.app.ui.settings.SettingsUiState
import com.contactme.app.ui.settings.SettingsViewModel
import com.contactme.app.ui.theme.ContactMeSpacing
import com.contactme.app.ui.theme.ContactMeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    SettingsContent(
        uiState = uiState,
        onBack = onBack,
        onEditProfile = onEditProfile,
        onSignOut = onSignOut
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onSignOut: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile & Settings",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(text = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(
                    horizontal = ContactMeSpacing.screenHorizontal,
                    vertical = ContactMeSpacing.contentGap
                ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.displayName.profileInitials(),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(
                        text = if (uiState.isLoadingProfile) {
                            "Loading profile..."
                        } else {
                            uiState.displayName
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "@${uiState.username}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f)
                    )
                }
            }
            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onEditProfile
            ) {
                Text(text = "Edit profile")
            }
            SettingsItem(
                title = "Privacy",
                subtitle = "Last seen, profile photo, read receipts"
            )
            SettingsItem(
                title = "Notifications",
                subtitle = "Messages, groups, calls, and channels"
            )
            SettingsItem(
                title = "Storage",
                subtitle = "Media cleanup and local cache"
            )
            SettingsItem(
                title = "Blocked users",
                subtitle = "Manage blocked contacts"
            )
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onSignOut
            ) {
                Text(
                    text = "Log out",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    ContactMeTheme {
        SettingsContent(
            uiState = SettingsUiState(
                displayName = "Masud Rana",
                username = "masud_rana",
                isLoadingProfile = false
            ),
            onBack = {},
            onEditProfile = {},
            onSignOut = {}
        )
    }
}

private fun String.profileInitials(): String {
    val initials = trim()
        .split(" ")
        .filter(String::isNotBlank)
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")

    return initials.ifBlank { "CM" }
}
