package com.contactme.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BatterySaver
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.contactme.app.profile.PrivacyVisibility
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
        onSignOut = onSignOut,
        onLastSeenChanged = viewModel::setLastSeenVisibility,
        onProfilePhotoChanged = viewModel::setProfilePhotoVisibility,
        onReadReceiptsClick = viewModel::toggleReadReceipts
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onSignOut: () -> Unit,
    onLastSeenChanged: (PrivacyVisibility) -> Unit,
    onProfilePhotoChanged: (PrivacyVisibility) -> Unit,
    onReadReceiptsClick: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(
                    horizontal = ContactMeSpacing.screenHorizontal,
                    vertical = ContactMeSpacing.contentGap
                ),
            verticalArrangement = Arrangement.spacedBy(ContactMeSpacing.md)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(18.dp),
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(ContactMeSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.photoUrl.isNotBlank()) {
                            AsyncImage(
                                model = uiState.photoUrl,
                                contentDescription = "Profile photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = uiState.displayName.profileInitials(),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Column {
                        Text(
                            text = if (uiState.isLoadingProfile) "Loading..." else uiState.displayName,
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
            }
            
            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            FilledTonalButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onEditProfile
            ) {
                Icon(Icons.Outlined.Edit, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(text = "Edit profile")
            }
            
            Text(
                text = "Privacy",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            PrivacyChoiceItem(
                title = "Last seen",
                value = uiState.privacySettings.lastSeenVisibility.toDisplayText(),
                enabled = !uiState.isSavingPrivacy,
                onValueChanged = onLastSeenChanged
            )
            PrivacyChoiceItem(
                title = "Profile photo",
                value = uiState.privacySettings.profilePhotoVisibility.toDisplayText(),
                enabled = !uiState.isSavingPrivacy,
                onValueChanged = onProfilePhotoChanged
            )
            PrivacyToggleItem(
                title = "Read receipts",
                checked = uiState.privacySettings.readReceiptsEnabled,
                enabled = !uiState.isSavingPrivacy,
                onClick = onReadReceiptsClick
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Text(
                text = "Calling Optimization",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            ) {
                Icon(Icons.Outlined.Notifications, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Open Notification Settings")
            }

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    runCatching { context.startActivity(intent) }
                }
            ) {
                Icon(Icons.Outlined.BatterySaver, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Optimize Battery for Calls")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onSignOut,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Outlined.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(text = "Log out")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrivacyChoiceItem(
    title: String,
    value: String,
    enabled: Boolean,
    onValueChanged: (PrivacyVisibility) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { if (enabled) expanded = !expanded }) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(title) },
            trailingIcon = { Icon(Icons.Outlined.ExpandMore, contentDescription = "Choose $title") },
            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            PrivacyVisibility.entries.forEach { visibility ->
                DropdownMenuItem(
                    text = { Text(visibility.toDisplayText()) },
                    onClick = { expanded = false; onValueChanged(visibility) }
                )
            }
        }
    }
}

@Composable
private fun PrivacyToggleItem(
    title: String,
    checked: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = { onClick() }
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
                photoUrl = "",
                isLoadingProfile = false
            ),
            onBack = {},
            onEditProfile = {},
            onSignOut = {},
            onLastSeenChanged = {},
            onProfilePhotoChanged = {},
            onReadReceiptsClick = {}
        )
    }
}

private fun PrivacyVisibility.toDisplayText(): String {
    return when (this) {
        PrivacyVisibility.Everyone -> "Everyone"
        PrivacyVisibility.Contacts -> "Contacts"
        PrivacyVisibility.Nobody -> "Nobody"
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
