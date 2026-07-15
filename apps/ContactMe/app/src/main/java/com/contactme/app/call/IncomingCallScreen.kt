package com.contactme.app.call

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun IncomingCallScreen(
    onCallDismissed: () -> Unit,
    viewModel: IncomingCallViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var hasPermissions by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        val isVideo = uiState.activeCall?.type == CallType.Video
        val cameraGranted = if (isVideo) permissions[Manifest.permission.CAMERA] ?: false else true
        
        hasPermissions = audioGranted && cameraGranted
        if (hasPermissions) {
            viewModel.acceptCall()
        }
    }

    LaunchedEffect(uiState.status) {
        if (uiState.status == CallStatus.Ended || 
            uiState.status == CallStatus.Rejected || 
            uiState.status == CallStatus.Cancelled ||
            uiState.status == CallStatus.Busy ||
            uiState.status == CallStatus.Timeout) {
            onCallDismissed()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        val call = uiState.activeCall
        if (call != null) {
            if (uiState.status == CallStatus.Ringing) {
                IncomingRingingLayout(
                    uiState = uiState,
                    onAccept = {
                        val isVideo = call.type == CallType.Video
                        val hasAudio = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                        val hasCamera = if (isVideo) ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED else true
                        
                        if (hasAudio && hasCamera) {
                            hasPermissions = true
                            viewModel.acceptCall()
                        } else {
                            val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
                            if (isVideo) permissions.add(Manifest.permission.CAMERA)
                            permissionLauncher.launch(permissions.toTypedArray())
                        }
                    },
                    onReject = viewModel::rejectCall
                )
            } else {
                if (call.type == CallType.Video) {
                    IncomingVideoCallLayout(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                } else {
                    IncomingAudioCallLayout(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun IncomingRingingLayout(
    uiState: IncomingCallUiState,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CallPeerInfo(
            displayName = uiState.callerProfile?.displayName ?: "ContactMe User",
            phoneNumber = uiState.callerProfile?.phoneNumber,
            photoUrl = uiState.callerProfile?.photoUrl,
            statusLabel = if (uiState.activeCall?.type == CallType.Video) "Incoming video call..." else "Incoming voice call...",
            durationSeconds = 0
        )

        Spacer(modifier = Modifier.height(64.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FloatingActionButton(
                onClick = onReject,
                containerColor = Color.Red,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(Icons.Default.CallEnd, contentDescription = "Reject")
            }

            FloatingActionButton(
                onClick = onAccept,
                containerColor = Color.Green,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(if (uiState.activeCall?.type == CallType.Video) Icons.Default.Videocam else Icons.Default.Call, contentDescription = "Accept")
            }
        }
    }
}

@Composable
private fun IncomingAudioCallLayout(
    uiState: IncomingCallUiState,
    viewModel: IncomingCallViewModel
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        CallPeerInfo(
            displayName = uiState.callerProfile?.displayName ?: "ContactMe User",
            phoneNumber = uiState.callerProfile?.phoneNumber,
            photoUrl = uiState.callerProfile?.photoUrl,
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
private fun IncomingVideoCallLayout(
    uiState: IncomingCallUiState,
    viewModel: IncomingCallViewModel
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.remoteVideoTrack != null) {
            VideoRenderer(modifier = Modifier.fillMaxSize()) { renderer ->
                viewModel.setupRemoteVideo(renderer)
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        }

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

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = uiState.callerProfile?.displayName ?: "ContactMe User",
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
