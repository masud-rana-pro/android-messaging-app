package com.contactme.app.call

import android.Manifest
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import org.webrtc.PeerConnection
import java.util.Locale
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.compose.ui.text.style.TextAlign
import com.contactme.app.ui.theme.ContactMeGreen

@Composable
fun OutgoingCallScreen(
    receiverId: String,
    onCallEnded: () -> Unit,
    viewModel: OutgoingCallViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var hasMicrophonePermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicrophonePermission = isGranted
        if (isGranted) {
            viewModel.startCall(receiverId, CallType.Audio)
        }
    }

    LaunchedEffect(Unit) {
        val permissionState = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
        if (permissionState == PackageManager.PERMISSION_GRANTED) {
            hasMicrophonePermission = true
            viewModel.startCall(receiverId, CallType.Audio)
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
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

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (!hasMicrophonePermission) {
            PermissionRequiredView(onGrant = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) })
        } else {
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
    }
}

@Composable
private fun PermissionRequiredView(onGrant: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Microphone permission is required for voice calls.", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onGrant) {
            Text("Grant Permission")
        }
    }
}
