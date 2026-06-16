package com.contactme.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.contactme.app.message.ChatMessage
import com.contactme.app.ui.chat.ChatDetailUiState
import com.contactme.app.ui.chat.ChatDetailViewModel
import com.contactme.app.ui.theme.ContactMeSpacing
import com.contactme.app.ui.theme.ContactMeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chatName: String,
    conversationId: String? = null,
    onBack: () -> Unit,
    viewModel: ChatDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(conversationId) {
        viewModel.openConversation(conversationId)
    }

    ChatDetailContent(
        chatName = chatName,
        conversationId = conversationId,
        uiState = uiState,
        onBack = onBack,
        onMessageTextChanged = viewModel::onMessageTextChanged,
        onSendMessage = viewModel::sendMessage
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatDetailContent(
    chatName: String,
    conversationId: String?,
    uiState: ChatDetailUiState,
    onBack: () -> Unit,
    onMessageTextChanged: (String) -> Unit,
    onSendMessage: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = chatName, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (conversationId == null) {
                                "online"
                            } else {
                                "last seen recently"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f)
                        )
                    }
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
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val messages = if (conversationId == null) {
                    demoMessages(currentUserId = uiState.currentUserId)
                } else {
                    uiState.messages
                }

                if (messages.isEmpty()) {
                    Text(
                        text = "No messages yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f)
                    )
                }

                messages.forEach { message ->
                    MessageBubble(
                        text = message.text,
                        isMine = message.senderId == uiState.currentUserId
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.errorMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = uiState.messageText,
                        onValueChange = onMessageTextChanged,
                        enabled = conversationId != null && !uiState.isSending,
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = if (conversationId == null) {
                                    "Open a contact to message"
                                } else {
                                    "Message"
                                }
                            )
                        }
                    )
                    Button(
                        enabled = conversationId != null && !uiState.isSending,
                        onClick = onSendMessage
                    ) {
                        Text(text = "Send")
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    text: String,
    isMine: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .background(
                    color = if (isMine) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

private fun demoMessages(currentUserId: String): List<ChatMessage> {
    return listOf(
        ChatMessage(
            id = "demo-1",
            senderId = "other",
            text = "Hey, is the ContactMe UI demo ready?",
            sentAtMillis = 0L
        ),
        ChatMessage(
            id = "demo-2",
            senderId = currentUserId,
            text = "The first screen map is almost done.",
            sentAtMillis = 0L
        ),
        ChatMessage(
            id = "demo-3",
            senderId = currentUserId,
            text = "Next we can connect real auth.",
            sentAtMillis = 0L
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun ChatDetailScreenPreview() {
    ContactMeTheme {
        ChatDetailScreen(
            chatName = "Ayesha Rahman",
            conversationId = null,
            onBack = {}
        )
    }
}
