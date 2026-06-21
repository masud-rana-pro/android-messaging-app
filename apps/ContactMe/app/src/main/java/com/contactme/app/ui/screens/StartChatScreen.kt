package com.contactme.app.ui.screens

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.contactme.app.profile.UserProfile
import com.contactme.app.ui.contact.ContactListViewModel
import com.contactme.app.ui.conversation.ConversationViewModel
import com.contactme.app.ui.discovery.ContactDiscoveryUiState
import com.contactme.app.ui.discovery.ContactDiscoveryViewModel
import com.contactme.app.ui.theme.ContactMeSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartChatScreen(
    onBack: () -> Unit,
    onUserSelected: (UserProfile) -> Unit,
    discoveryViewModel: ContactDiscoveryViewModel = hiltViewModel(),
    contactListViewModel: ContactListViewModel = hiltViewModel(),
    conversationViewModel: ConversationViewModel = hiltViewModel()
) {
    val discoveryState by discoveryViewModel.uiState.collectAsState()
    val isOpeningChat by conversationViewModel.isOpeningChat.collectAsState()
    val openingError by conversationViewModel.errorMessage.collectAsState()
    val isMatchingContacts by contactListViewModel.isMatchingContacts.collectAsState()
    val matchedUsers by contactListViewModel.matchedContactMeUsers.collectAsState()

    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                contactListViewModel.findFromPhoneContacts()
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Start a chat", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = ContactMeSpacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = discoveryState.query,
                    onValueChange = discoveryViewModel::onQueryChanged,
                    placeholder = { Text("Search username or phone") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS) },
                    enabled = !isMatchingContacts,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isMatchingContacts) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Matching...")
                    } else {
                        Icon(Icons.Outlined.PersonAdd, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Find from phone contacts")
                    }
                }

                if (openingError != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            text = openingError!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (discoveryState.isSearching) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    val results = if (discoveryState.query.isBlank()) matchedUsers else discoveryState.results

                    if (results.isEmpty() && !discoveryState.isSearching) {
                        item {
                            Text(
                                text = if (discoveryState.query.isBlank()) 
                                    "Search a ContactMe user to start chatting." 
                                else "No ContactMe user found.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    items(results) { user ->
                        StartChatUserRow(
                            user = user,
                            onClick = { 
                                if (!isOpeningChat) {
                                    Log.d("StartChatScreen", "User selected: ${user.userId}")
                                    onUserSelected(user) 
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StartChatUserRow(
    user: UserProfile,
    onClick: () -> Unit
) {
    Log.d("StartChatScreen", "Rendering StartChatUserRow for: ${user.username}")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                Log.d("StartChatScreen", "Row clicked for: ${user.userId}")
                onClick()
            }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (user.photoUrl.isNotBlank()) {
                AsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    model = user.photoUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = user.displayName.take(1).uppercase(),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.displayName.ifBlank { "ContactMe User" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "@${user.username}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
