package com.contactme.app.call

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun OutgoingCallScreen(
    receiverId: String,
    callType: CallType = CallType.Audio,
    startNewCall: Boolean = true,
    onCallEnded: () -> Unit,
    viewModel: OutgoingCallViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(receiverId, callType, startNewCall) {
        if (startNewCall) {
            viewModel.startCall(receiverId, callType)
        }
    }

    LaunchedEffect(uiState.status) {
        if (uiState.status == CallStatus.Ended || 
            uiState.status == CallStatus.Rejected || 
            uiState.status == CallStatus.Cancelled ||
            uiState.status == CallStatus.Busy ||
            uiState.status == CallStatus.Timeout) {
            onCallEnded()
        }
    }

    CallScreenSurface {
        if (callType == CallType.Video && (uiState.status == CallStatus.Connected || uiState.status == CallStatus.Accepted || uiState.status == CallStatus.Connecting)) {
            VideoCallLayout(
                uiState = uiState,
                viewModel = viewModel
            )
        } else {
            AudioCallLayout(
                uiState = uiState,
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun AudioCallLayout(
    uiState: OutgoingCallUiState,
    viewModel: OutgoingCallViewModel
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        CallPeerInfo(
            displayName = uiState.receiverProfile?.displayName ?: "ContactMe User",
            phoneNumber = uiState.receiverProfile?.phoneNumber,
            photoUrl = uiState.receiverProfile?.photoUrl,
            statusLabel = getCallStatusLabel(uiState.status, uiState.connectionState),
            durationSeconds = uiState.durationSeconds
        )

        Spacer(modifier = Modifier.weight(1f))

        CallControlBar(
            isMuted = uiState.isMuted,
            isSpeakerEnabled = uiState.isSpeakerEnabled,
            onToggleMute = viewModel::toggleMute,
            onToggleSpeaker = viewModel::toggleSpeaker,
            onEndCall = viewModel::endCall
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun VideoCallLayout(
    uiState: OutgoingCallUiState,
    viewModel: OutgoingCallViewModel
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.remoteVideoTrack != null) {
            VideoRenderer(modifier = Modifier.fillMaxSize()) { renderer ->
                viewModel.setupRemoteVideo(renderer)
            }
        } else {
            CallVideoPlaceholder(modifier = Modifier.fillMaxSize())
        }

        LocalVideoPreviewContainer(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(width = 120.dp, height = 180.dp)
        ) {
            VideoRenderer(modifier = Modifier.fillMaxSize()) { renderer ->
                viewModel.setupLocalVideo(renderer)
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = uiState.receiverProfile?.displayName ?: "ContactMe User",
                style = MaterialTheme.typography.titleLarge,
                color = CallTextPrimary
            )
            Text(
                text = getCallStatusLabel(uiState.status, uiState.connectionState),
                style = MaterialTheme.typography.bodyMedium,
                color = CallTextSecondary
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = viewModel::switchCamera) {
                    Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Switch Camera", tint = CallTextPrimary)
                }
                
                CallControlBar(
                    isMuted = uiState.isMuted,
                    isSpeakerEnabled = uiState.isSpeakerEnabled,
                    onToggleMute = viewModel::toggleMute,
                    onToggleSpeaker = viewModel::toggleSpeaker,
                    onEndCall = viewModel::endCall
                )
            }
        }
    }
}
