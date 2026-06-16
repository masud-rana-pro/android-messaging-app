package com.contactme.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.contactme.app.conversation.ConversationPreview
import com.contactme.app.navigation.HomeTab
import com.contactme.app.profile.UserProfile
import com.contactme.app.ui.discovery.ContactDiscoveryUiState
import com.contactme.app.ui.discovery.ContactDiscoveryViewModel
import com.contactme.app.ui.conversation.ConversationListUiState
import com.contactme.app.ui.conversation.ConversationListViewModel
import com.contactme.app.ui.theme.ContactMeSpacing
import com.contactme.app.ui.theme.ContactMeTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onChatSelected: (String) -> Unit,
    onConversationSelected: (String, String) -> Unit,
    onDiscoveredUserSelected: (UserProfile) -> Unit,
    onSettingsSelected: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(HomeTab.Chats) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ContactMe",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    TextButton(onClick = onSettingsSelected) {
                        Text(text = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                HomeTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Text(text = tab.shortLabel.first().toString()) },
                        label = { Text(text = tab.shortLabel) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(
                    horizontal = ContactMeSpacing.screenHorizontal,
                    vertical = ContactMeSpacing.contentGap
                )
        ) {
            when (selectedTab) {
                HomeTab.Chats -> ChatsTab(
                    onChatSelected = onChatSelected,
                    onConversationSelected = onConversationSelected,
                    onDiscoveredUserSelected = onDiscoveredUserSelected
                )
                HomeTab.Status -> PlaceholderTab(
                    title = "Status",
                    subtitle = "24-hour updates will appear here."
                )

                HomeTab.Calls -> PlaceholderTab(
                    title = "Calls",
                    subtitle = "Voice and video call history will appear here."
                )

                HomeTab.Communities -> PlaceholderTab(
                    title = "Communities",
                    subtitle = "Linked groups and announcements will appear here."
                )

                HomeTab.Channels -> PlaceholderTab(
                    title = "Channels",
                    subtitle = "Broadcast channels and posts will appear here."
                )
            }
        }
    }
}

@Composable
private fun ChatsTab(
    onChatSelected: (String) -> Unit,
    onConversationSelected: (String, String) -> Unit,
    onDiscoveredUserSelected: (UserProfile) -> Unit,
    discoveryViewModel: ContactDiscoveryViewModel = hiltViewModel(),
    conversationListViewModel: ConversationListViewModel = hiltViewModel()
) {
    val discoveryState by discoveryViewModel.uiState.collectAsState()
    val conversationListState by conversationListViewModel.uiState.collectAsState()

    ChatsContent(
        discoveryState = discoveryState,
        conversationListState = conversationListState,
        onSearchQueryChanged = discoveryViewModel::onQueryChanged,
        onChatSelected = onChatSelected,
        onConversationSelected = onConversationSelected,
        onDiscoveredUserSelected = onDiscoveredUserSelected
    )
}

@Composable
private fun ChatsContent(
    discoveryState: ContactDiscoveryUiState,
    conversationListState: ConversationListUiState,
    onSearchQueryChanged: (String) -> Unit,
    onChatSelected: (String) -> Unit,
    onConversationSelected: (String, String) -> Unit,
    onDiscoveredUserSelected: (UserProfile) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Chats",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Messenger core UI demo",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
            )
        }
        ContactSearch(
            discoveryState = discoveryState,
            onSearchQueryChanged = onSearchQueryChanged,
            onDiscoveredUserSelected = onDiscoveredUserSelected
        )
        ConversationPreviewList(
            conversationListState = conversationListState,
            onConversationSelected = onConversationSelected
        )
        if (conversationListState.conversations.isEmpty()) {
            ChatPreviewList(onChatSelected = onChatSelected)
        }
    }
}

@Composable
private fun ConversationPreviewList(
    conversationListState: ConversationListUiState,
    onConversationSelected: (String, String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (conversationListState.isLoading) {
            Text(
                text = "Loading conversations...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f)
            )
        }
        conversationListState.message?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f)
            )
        }
        conversationListState.conversations.forEach { conversation ->
            ConversationPreviewItem(
                conversation = conversation,
                onClick = {
                    onConversationSelected(
                        conversation.conversationId,
                        conversation.title
                    )
                }
            )
        }
    }
}

@Composable
private fun ConversationPreviewItem(
    conversation: ConversationPreview,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ContactAvatar(
            label = conversation.title,
            photoUrl = conversation.photoUrl,
            size = 48
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = conversation.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (conversation.hasUnreadMessages) {
                    FontWeight.Bold
                } else {
                    FontWeight.SemiBold
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = conversation.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(
                    alpha = if (conversation.hasUnreadMessages) 0.86f else 0.68f
                ),
                fontWeight = if (conversation.hasUnreadMessages) {
                    FontWeight.SemiBold
                } else {
                    FontWeight.Normal
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = conversation.updatedAtMillis.formatConversationTime(),
                style = MaterialTheme.typography.labelMedium,
                color = if (conversation.hasUnreadMessages) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.56f)
                },
                fontWeight = if (conversation.hasUnreadMessages) {
                    FontWeight.Bold
                } else {
                    FontWeight.Normal
                }
            )
            if (conversation.hasUnreadMessages) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
private fun ContactSearch(
    discoveryState: ContactDiscoveryUiState,
    onSearchQueryChanged: (String) -> Unit,
    onDiscoveredUserSelected: (UserProfile) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = discoveryState.query,
            onValueChange = onSearchQueryChanged,
            singleLine = true,
            label = { Text(text = "Find people") },
            placeholder = { Text(text = "Search username or phone") }
        )
        if (discoveryState.isSearching) {
            Text(
                text = "Searching...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f)
            )
        }
        discoveryState.message?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f)
            )
        }
        discoveryState.results.forEach { profile ->
            ContactSearchResult(
                profile = profile,
                onClick = { onDiscoveredUserSelected(profile) }
            )
        }
    }
}

@Composable
private fun ContactSearchResult(
    profile: UserProfile,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ContactAvatar(
            label = profile.displayName,
            photoUrl = profile.photoUrl,
            size = 44
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = profile.displayName.ifBlank { "ContactMe User" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "@${profile.username}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f)
            )
            if (profile.phoneNumber.isNotBlank()) {
                Text(
                    text = profile.phoneNumber,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.52f)
                )
            }
        }
        Text(
            text = "Open",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ContactAvatar(
    label: String,
    photoUrl: String,
    size: Int
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (photoUrl.isNotBlank()) {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = photoUrl,
                contentDescription = "$label profile photo",
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = label.profileInitials(),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ChatPreviewList(onChatSelected: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ChatPreviewItem(
            name = "Ayesha Rahman",
            message = "Project scaffold ready?",
            time = "09:10",
            onClick = { onChatSelected("Ayesha Rahman") }
        )
        ChatPreviewItem(
            name = "Team ContactMe",
            message = "Next: auth and real navigation",
            time = "08:45",
            onClick = { onChatSelected("Team ContactMe") }
        )
        ChatPreviewItem(
            name = "Design Notes",
            message = "Primary color: #a605e6",
            time = "Yesterday",
            onClick = { onChatSelected("Design Notes") }
        )
    }
}

@Composable
private fun ChatPreviewItem(
    name: String,
    message: String,
    time: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f)
            )
        }
        Text(
            text = time,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.56f)
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

private fun Long.formatConversationTime(): String {
    if (this <= 0L) return ""

    val messageCalendar = Calendar.getInstance().apply { timeInMillis = this@formatConversationTime }
    val todayCalendar = Calendar.getInstance()

    return when {
        messageCalendar.isSameDay(todayCalendar) -> {
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(this))
        }
        messageCalendar.isYesterday(todayCalendar) -> "Yesterday"
        else -> SimpleDateFormat("M/d/yy", Locale.getDefault()).format(Date(this))
    }
}

private fun Calendar.isSameDay(other: Calendar): Boolean {
    return get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
        get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)
}

private fun Calendar.isYesterday(today: Calendar): Boolean {
    val yesterday = today.clone() as Calendar
    yesterday.add(Calendar.DAY_OF_YEAR, -1)
    return isSameDay(yesterday)
}

@Composable
private fun PlaceholderTab(
    title: String,
    subtitle: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    ContactMeTheme {
        HomeScreen(
            onChatSelected = {},
            onConversationSelected = { _, _ -> },
            onDiscoveredUserSelected = {},
            onSettingsSelected = {}
        )
    }
}
