package com.contactme.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Button
import androidx.compose.ui.graphics.Color
import com.contactme.app.ui.theme.ContactMeGreen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.CallReceived
import androidx.compose.material.icons.automirrored.outlined.CallMade
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.contactme.app.R
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import com.contactme.app.ui.theme.ContactMeGreen
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.contactme.app.conversation.ConversationPreview
import com.contactme.app.conversation.ConversationType
import com.contactme.app.navigation.HomeTab
import com.contactme.app.profile.UserProfile
import com.contactme.app.ui.discovery.ContactDiscoveryUiState
import com.contactme.app.ui.discovery.ContactDiscoveryViewModel
import com.contactme.app.ui.conversation.ConversationListUiState
import com.contactme.app.ui.conversation.ConversationListViewModel
import com.contactme.app.ui.contact.ContactListUiState
import com.contactme.app.ui.contact.ContactListViewModel
import com.contactme.app.ui.call.CallsViewModel
import com.contactme.app.ui.call.CallListUiState
import com.contactme.app.call.CallSession
import com.contactme.app.call.CallStatus
import com.contactme.app.call.CallType
import com.contactme.app.ui.theme.ContactMeSpacing
import com.contactme.app.ui.theme.ContactMeTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onConversationSelected: (String, String, String, ConversationType) -> Unit,
    onDiscoveredUserSelected: (UserProfile) -> Unit,
    onStartChatSelected: () -> Unit,
    onCreateGroupSelected: () -> Unit,
    onSettingsSelected: () -> Unit,
    onCallSelected: (String, CallType) -> Unit
) {
    var selectedTab by remember { mutableStateOf(HomeTab.Chats) }
    var newChatRequestCount by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.contactme_logo_v2),
                            contentDescription = null,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(text = "ContactMe", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    if (selectedTab == HomeTab.Chats) {
                        IconButton(onClick = onCreateGroupSelected) {
                            Icon(
                                imageVector = Icons.Outlined.GroupAdd,
                                contentDescription = "New group"
                            )
                        }
                    }
                    IconButton(onClick = onSettingsSelected) {
                        Icon(imageVector = Icons.Outlined.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            if (selectedTab == HomeTab.Chats) {
                FloatingActionButton(
                    onClick = onStartChatSelected,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AddComment,
                        contentDescription = "New chat"
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar {
                listOf(HomeTab.Chats, HomeTab.Calls).forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = when (tab) {
                                    HomeTab.Chats -> Icons.Outlined.Chat
                                    HomeTab.Calls -> Icons.Outlined.Call
                                    HomeTab.Communities -> Icons.Outlined.Groups
                                    else -> Icons.Outlined.Chat
                                },
                                contentDescription = tab.label
                            )
                        },
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
                    newChatRequestCount = newChatRequestCount,
                    onConversationSelected = onConversationSelected,
                    onDiscoveredUserSelected = onDiscoveredUserSelected,
                    onStartChatClick = onStartChatSelected
                )
                HomeTab.Status -> PlaceholderTab(
                    title = "Status"
                )

                HomeTab.Calls -> CallsTab(
                    onOpenChat = { conversationId, name, photoUrl ->
                        onConversationSelected(
                            conversationId,
                            name,
                            photoUrl,
                            ConversationType.Direct
                        )
                    },
                    onStartCall = onCallSelected
                )

                HomeTab.Communities -> PlaceholderTab(
                    title = "Communities"
                )

                HomeTab.Channels -> PlaceholderTab(
                    title = "Channels"
                )
            }
        }
    }
}

@Composable
private fun ChatsTab(
    newChatRequestCount: Int,
    onConversationSelected: (String, String, String, ConversationType) -> Unit,
    onDiscoveredUserSelected: (UserProfile) -> Unit,
    onStartChatClick: () -> Unit,
    discoveryViewModel: ContactDiscoveryViewModel = hiltViewModel(),
    contactListViewModel: ContactListViewModel = hiltViewModel(),
    conversationListViewModel: ConversationListViewModel = hiltViewModel()
) {
    val discoveryState by discoveryViewModel.uiState.collectAsState()
    val contactListState by contactListViewModel.uiState.collectAsState()
    val conversationListState by conversationListViewModel.uiState.collectAsState()

    ChatsContent(
        newChatRequestCount = newChatRequestCount,
        discoveryState = discoveryState,
        contactListState = contactListState,
        conversationListState = conversationListState,
        onSearchQueryChanged = discoveryViewModel::onQueryChanged,
        onConversationSelected = onConversationSelected,
        onDiscoveredUserSelected = onDiscoveredUserSelected,
        onStartChatClick = onStartChatClick
    )
}

@Composable
private fun ChatsContent(
    newChatRequestCount: Int,
    discoveryState: ContactDiscoveryUiState,
    contactListState: ContactListUiState,
    conversationListState: ConversationListUiState,
    onSearchQueryChanged: (String) -> Unit,
    onConversationSelected: (String, String, String, ConversationType) -> Unit,
    onDiscoveredUserSelected: (UserProfile) -> Unit,
    onStartChatClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    var selectedFilter by remember { mutableStateOf(ChatListFilter.All) }

    LaunchedEffect(newChatRequestCount) {
        if (newChatRequestCount > 0) {
            scrollState.animateScrollTo(0)
        }
    }

    Column(
        modifier = Modifier.verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(ContactMeSpacing.md)
    ) {
        ChatSearch(
            newChatRequestCount = newChatRequestCount,
            discoveryState = discoveryState,
            onSearchQueryChanged = onSearchQueryChanged
        )
        ChatFilterRow(
            selectedFilter = selectedFilter,
            onFilterSelected = { selectedFilter = it }
        )
        ConversationPreviewList(
            conversationListState = conversationListState,
            query = discoveryState.query,
            filter = selectedFilter,
            onConversationSelected = onConversationSelected,
            onStartChatClick = onStartChatClick
        )
        if (discoveryState.query.isNotBlank()) {
            PeopleSearchResults(
                discoveryState = discoveryState,
                contactListState = contactListState,
                onUserSelected = onDiscoveredUserSelected
            )
        }
    }
}

@Composable
private fun PeopleSearchResults(
    discoveryState: ContactDiscoveryUiState,
    contactListState: ContactListUiState,
    onUserSelected: (UserProfile) -> Unit
) {
    val normalizedQuery = discoveryState.query.trim().lowercase()
    val matchingContacts = contactListState.contacts.filter { contact ->
        contact.displayName.lowercase().contains(normalizedQuery) ||
            contact.username.lowercase().contains(normalizedQuery) ||
            contact.phoneNumber.contains(normalizedQuery)
    }
    val remoteResults = discoveryState.results.filter { result ->
        matchingContacts.none { it.userId == result.userId }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(title = "People")
        if (discoveryState.isSearching || contactListState.isLoading) {
            SupportingText(text = "Searching...")
        }
        matchingContacts.forEach { contact ->
            ContactRow(
                profile = contact,
                onClick = { onUserSelected(contact) }
            )
        }
        remoteResults.forEach { profile ->
            ContactRow(profile = profile, onClick = { onUserSelected(profile) })
        }
        if (!discoveryState.isSearching && matchingContacts.isEmpty() && remoteResults.isEmpty()) {
            discoveryState.message?.let { SupportingText(text = it) }
        }
    }
}

@Composable
private fun ConversationPreviewList(
    conversationListState: ConversationListUiState,
    query: String,
    filter: ChatListFilter,
    onConversationSelected: (String, String, String, ConversationType) -> Unit,
    onStartChatClick: () -> Unit
) {
    val normalizedQuery = query.trim().lowercase()
    val visibleConversations = conversationListState.conversations.filter { conversation ->
        val matchesQuery = normalizedQuery.isBlank() ||
            conversation.title.lowercase().contains(normalizedQuery) ||
            conversation.subtitle.lowercase().contains(normalizedQuery)
        val matchesFilter = when (filter) {
            ChatListFilter.All -> true
            ChatListFilter.Unread -> conversation.hasUnreadMessages
            ChatListFilter.Groups -> conversation.type == ConversationType.Group
        }
        matchesQuery && matchesFilter
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (conversationListState.isLoading) {
            SupportingText(
                text = "Loading conversations...",
            )
        }
        conversationListState.message?.takeIf { query.isBlank() }?.let { message ->
            SupportingText(text = message)
        }
        if (!conversationListState.isLoading && visibleConversations.isEmpty() && query.isBlank()) {
            when (filter) {
                ChatListFilter.All -> EmptyConversationState(onStartChatClick = onStartChatClick)
                ChatListFilter.Unread -> SupportingText(text = "No unread chats")
                ChatListFilter.Groups -> SupportingText(text = "No groups")
            }
        }
        visibleConversations.forEach { conversation ->
            ConversationPreviewItem(
                conversation = conversation,
                onClick = {
                    onConversationSelected(
                        conversation.conversationId,
                        conversation.title,
                        conversation.photoUrl,
                        conversation.type
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
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ContactAvatar(
            label = conversation.title,
            photoUrl = conversation.photoUrl,
            size = 52
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
                        alpha = if (conversation.hasUnreadMessages) 0.86f else 0.62f
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
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.54f)
                    },
                    fontWeight = if (conversation.hasUnreadMessages) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    }
                )
                if (conversation.hasUnreadMessages) {
                    UnreadDot()
                }
        }
    }
}

@Composable
private fun UnreadDot() {
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
    )
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.74f),
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun SupportingText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.64f)
    )
}

@Composable
private fun EmptyConversationState(onStartChatClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "No chats yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                SupportingText(text = "Search a username or phone number to start a conversation.")
            }
            Button(
                onClick = onStartChatClick,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Start a chat")
            }
        }
    }
}

@Composable
private fun ChatSearch(
    newChatRequestCount: Int,
    discoveryState: ContactDiscoveryUiState,
    onSearchQueryChanged: (String) -> Unit
) {
    val searchFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(newChatRequestCount) {
        if (newChatRequestCount > 0) {
            searchFocusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(searchFocusRequester),
        value = discoveryState.query,
        onValueChange = onSearchQueryChanged,
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
        leadingIcon = {
            Icon(imageVector = Icons.Outlined.Search, contentDescription = null)
        },
        placeholder = { Text(text = "Search chats or people") }
    )
}

@Composable
private fun ChatFilterRow(
    selectedFilter: ChatListFilter,
    onFilterSelected: (ChatListFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ChatListFilter.entries.forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(text = filter.label) }
            )
        }
    }
}

@Composable
private fun ContactRow(
    profile: UserProfile,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ContactAvatar(
            label = profile.displayName,
            photoUrl = profile.photoUrl,
            size = 46
        )
        Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.displayName.ifBlank { "ContactMe User" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "@${profile.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.66f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (profile.phoneNumber.isNotBlank()) {
                    Text(
                        text = profile.phoneNumber,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.48f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
        }
    }
}

private enum class ChatListFilter(val label: String) {
    All("All"),
    Unread("Unread"),
    Groups("Groups")
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
private fun CallsTab(
    onOpenChat: (String, String, String) -> Unit,
    onStartCall: (String, CallType) -> Unit,
    viewModel: com.contactme.app.ui.call.CallsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        CallsQuickActions()
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        Text(
            text = "Recent",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (uiState.isLoading) {
            Box(modifier = Modifier.padding(16.dp)) {
                SupportingText(text = "Loading calls...")
            }
        } else if (uiState.calls.isEmpty()) {
            EmptyCallsState(message = uiState.message ?: "No calls yet")
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(uiState.calls) { session ->
                    CallRow(
                        session = session,
                        currentUserId = uiState.currentUserId,
                        peerProfile = uiState.profiles[if (session.callerId == uiState.currentUserId) session.receiverId else session.callerId]
                        ,onOpenChat = { peerId, name, photoUrl ->
                            onOpenChat(
                                listOf(uiState.currentUserId, peerId).sorted().joinToString("__"),
                                name,
                                photoUrl
                            )
                        },
                        onStartCall = onStartCall
                    )
                }
            }
        }
    }
}

@Composable
private fun CallsQuickActions() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        QuickActionItem(icon = Icons.Outlined.Call, label = "Call")
        QuickActionItem(icon = Icons.Outlined.CalendarMonth, label = "Schedule")
        QuickActionItem(icon = Icons.Outlined.Dialpad, label = "Keypad")
        QuickActionItem(icon = Icons.Outlined.FavoriteBorder, label = "Favorites")
    }
}

@Composable
private fun QuickActionItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = label)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun EmptyCallsState(message: String) {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Outlined.Call, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            Text(text = message, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun CallRow(
    session: com.contactme.app.call.CallSession,
    currentUserId: String,
    peerProfile: com.contactme.app.profile.UserProfile?,
    onOpenChat: (String, String, String) -> Unit,
    onStartCall: (String, CallType) -> Unit
) {
    val isOutgoing = session.callerId == currentUserId
    val peerName = peerProfile?.displayName ?: "ContactMe User"
    val peerId = if (isOutgoing) session.receiverId else session.callerId
    val icon = when {
        session.status == com.contactme.app.call.CallStatus.Missed -> Icons.Default.CallMissed
        isOutgoing -> Icons.Default.CallMade
        else -> Icons.Default.CallReceived
    }
    val iconColor = when {
        session.status == com.contactme.app.call.CallStatus.Missed -> Color.Red
        isOutgoing -> ContactMeGreen
        else -> ContactMeGreen
    }

    Row(
        modifier = Modifier.fillMaxWidth().clickable {
            onOpenChat(peerId, peerName, peerProfile?.photoUrl.orEmpty())
        }.padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ContactAvatar(photoUrl = peerProfile?.photoUrl ?: "", label = peerName, size = 48)

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = peerName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (session.status == com.contactme.app.call.CallStatus.Missed) Color.Red else MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = iconColor)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${session.status.displayLabel()} • ${session.createdAtMillis.formatConversationTime()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        IconButton(onClick = { onOpenChat(peerId, peerName, peerProfile?.photoUrl.orEmpty()) }) {
            Icon(imageVector = Icons.Outlined.Chat, contentDescription = "Message", tint = MaterialTheme.colorScheme.primary)
        }
        IconButton(onClick = { onStartCall(peerId, session.type) }) {
            Icon(
                imageVector = if (session.type == CallType.Video) Icons.Outlined.Videocam else Icons.Outlined.Call,
                contentDescription = if (session.type == CallType.Video) "Video call" else "Audio call",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


private fun CallStatus.displayLabel(): String {
    return when (this) {
        CallStatus.Ringing -> "Ringing"
        CallStatus.Connecting -> "Connecting"
        CallStatus.Accepted -> "Ongoing"
        CallStatus.Connected -> "Connected"
        CallStatus.Rejected -> "Rejected"
        CallStatus.Ended -> "Ended"
        CallStatus.Cancelled -> "Cancelled"
        CallStatus.Busy -> "Busy"
        CallStatus.Timeout -> "Timed out"
        CallStatus.Missed -> "Missed"
    }
}

@Composable
private fun PlaceholderTab(
    title: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Coming soon",
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
            onConversationSelected = { _, _, _, _ -> },
            onDiscoveredUserSelected = {},
            onStartChatSelected = {},
            onCreateGroupSelected = {},
            onSettingsSelected = {},
            onCallSelected = { _, _ -> }
        )
    }
}
