package com.contactme.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.contactme.app.profile.UserProfile
import com.contactme.app.ui.group.GroupCreationViewModel
import com.contactme.app.ui.theme.ContactMeSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupCreationScreen(
    onBack: () -> Unit,
    onGroupCreated: () -> Unit,
    viewModel: GroupCreationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "New group", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text(text = "Back") }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = ContactMeSpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.title,
                onValueChange = viewModel::onTitleChanged,
                label = { Text(text = "Group name") },
                singleLine = true
            )
            Text(
                text = "${uiState.selectedUserIds.size} selected",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            uiState.errorMessage?.let { message ->
                Text(text = message, color = MaterialTheme.colorScheme.error)
            }
            when {
                uiState.isLoadingContacts -> CircularProgressIndicator()
                uiState.contacts.isEmpty() -> Text(text = "Save at least two contacts to create a group.")
                else -> LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(uiState.contacts, key = UserProfile::userId) { contact ->
                        GroupContactRow(
                            contact = contact,
                            selected = contact.userId in uiState.selectedUserIds,
                            onClick = { viewModel.toggleContact(contact.userId) }
                        )
                    }
                }
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                enabled = !uiState.isCreating && uiState.contacts.isNotEmpty(),
                onClick = { viewModel.createGroup(onGroupCreated) }
            ) {
                Text(text = if (uiState.isCreating) "Creating..." else "Create group")
            }
        }
    }
}

@Composable
private fun GroupContactRow(
    contact: UserProfile,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Checkbox(checked = selected, onCheckedChange = { onClick() })
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.displayName.ifBlank { contact.username },
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "@${contact.username}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
