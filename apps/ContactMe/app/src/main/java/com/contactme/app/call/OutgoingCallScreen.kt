package com.contactme.app.call

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import org.webrtc.SurfaceViewRenderer

@Composable
fun OutgoingCallScreen(
    receiverId: String,
    callType: CallType = CallType.Audio,
    onCallEnded: () -> Unit,
    viewModel: OutgoingCallViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var hasPermissions by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        val cameraGranted = if (callType == CallType.Video) permissions[Manifest.permission.CAMERA] ?: false else true
        
        hasPermissions = audioGranted && cameraGranted
        if (hasPermissions) {
            viewModel.startCall(receiverId, callType)
        }
    }

    LaunchedEffect(Unit) {
        val hasAudio = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        val hasCamera = if (callType == CallType.Video) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        } else true
        
        if (hasAudio && hasCamera) {
            hasPermissions = true
            viewModel.startCall(receiverId, callType)
        } else {
            val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
            if (callType == CallType.Video) permissions.add(Manifest.permission.CAMERA)
            permissionLauncher.launch(permissions.toTypedArray())
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

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (!hasPermissions) {
            PermissionRequiredView(
                isVideo = callType == CallType.Video,
                onGrant = {
                    val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
                    if (callType == CallType.Video) permissions.add(Manifest.permission.CAMERA)
                    permissionLauncher.launch(permissions.toTypedArray())
                }
            )
        } else {
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
        // Remote Video (Full Screen)
        if (uiState.remoteVideoTrack != null) {
            VideoRenderer(modifier = Modifier.fillMaxSize()) { renderer ->
                viewModel.setupRemoteVideo(renderer)
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        // Local Video (PIP)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(width = 120.dp, height = 180.dp)
                .background(Color.Black, shape = MaterialTheme.shapes.medium)
        ) {
            VideoRenderer(modifier = Modifier.fillMaxSize()) { renderer ->
                viewModel.setupLocalVideo(renderer)
            }
        }

        // Overlay Info & Controls
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = uiState.receiverProfile?.displayName ?: "ContactMe User",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
            Text(
                text = getCallStatusLabel(uiState.status, uiState.connectionState),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = viewModel::switchCamera) {
                    Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Switch Camera", tint = Color.White)
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

@Composable
private fun PermissionRequiredView(isVideo: Boolean, onGrant: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (isVideo) Icons.Default.Videocam else Icons.Default.Mic,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isVideo) "Camera and microphone permissions are required for video calls." else "Microphone permission is required for voice calls.",
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onGrant) {
            Text("Grant Permission")
        }
    }
}
