package com.contactme.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.contactme.app.profile.UserProfile
import com.contactme.app.ui.contact.ContactListViewModel
import com.contactme.app.ui.contact.DeviceContactUi
import com.contactme.app.ui.conversation.ConversationViewModel
import com.contactme.app.ui.discovery.ContactDiscoveryUiState
import com.contactme.app.ui.discovery.ContactDiscoveryViewModel
import com.contactme.app.ui.theme.ContactMeSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartChatScreen(
    onBack: () -> Unit,
    onUserSelected: (UserProfile) -> Unit,
    onAudioCall: (UserProfile) -> Unit,
    onVideoCall: (UserProfile) -> Unit,
    discoveryViewModel: ContactDiscoveryViewModel = hiltViewModel(),
    contactListViewModel: ContactListViewModel = hiltViewModel(),
    conversationViewModel: ConversationViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var query by rememberSaveable { mutableStateOf("") }
    val isOpeningChat by conversationViewModel.isOpeningChat.collectAsState()
    val openingError by conversationViewModel.errorMessage.collectAsState()
    val isMatchingContacts by contactListViewModel.isMatchingContacts.collectAsState()
    val matchedUsers by contactListViewModel.matchedContactMeUsers.collectAsState()
    val deviceContacts by contactListViewModel.deviceContacts.collectAsState()

    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                contactListViewModel.findFromPhoneContacts()
            }
        }
    )

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            contactListViewModel.findFromPhoneContacts()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Select contact", fontWeight = FontWeight.Bold)
                        Text("${deviceContacts.size} contacts", style = MaterialTheme.typography.labelMedium)
                    }
                },
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
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search name or phone") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )

                if (deviceContacts.isEmpty()) Button(
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
                    if (isMatchingContacts) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    val results = deviceContacts.filter { contact ->
                        query.isBlank() || contact.name.contains(query, ignoreCase = true) ||
                            contact.phoneNumber.orEmpty().contains(query) || contact.email.orEmpty().contains(query, ignoreCase = true)
                    }

                    if (results.isEmpty() && !isMatchingContacts) {
                        item {
                            Text(
                                text = if (deviceContacts.isEmpty()) "Allow contacts access to show your phone contacts." else "No matching contact found.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    items(results, key = { it.phoneNumber ?: it.email ?: it.name }) { contact ->
                        DeviceContactRow(
                            contact = contact,
                            actionsEnabled = !isOpeningChat,
                            onChat = { contact.contactMeProfile?.let(onUserSelected) },
                            onAudioCall = { contact.contactMeProfile?.let(onAudioCall) },
                            onVideoCall = { contact.contactMeProfile?.let(onVideoCall) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceContactRow(
    contact: DeviceContactUi,
    actionsEnabled: Boolean,
    onChat: () -> Unit,
    onAudioCall: () -> Unit,
    onVideoCall: () -> Unit
) {
    val profile = contact.contactMeProfile
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(52.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (!profile?.photoUrl.isNullOrBlank()) {
                AsyncImage(model = profile?.photoUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Text(contact.name.take(1).uppercase(), color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(contact.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                text = profile?.let { it.phoneNumber.ifBlank { "@${it.username}" } }
                    ?: contact.phoneNumber ?: contact.email ?: "Not on ContactMe",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (profile == null) Text("Not on ContactMe", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (profile != null) {
            IconButton(onClick = onChat, enabled = actionsEnabled) { Icon(Icons.Outlined.Chat, contentDescription = "Chat") }
            IconButton(onClick = onAudioCall, enabled = actionsEnabled) { Icon(Icons.Outlined.Call, contentDescription = "Audio call") }
            IconButton(onClick = onVideoCall, enabled = actionsEnabled) { Icon(Icons.Outlined.Videocam, contentDescription = "Video call") }
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
