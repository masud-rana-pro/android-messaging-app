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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import org.webrtc.PeerConnection
import com.contactme.app.ui.theme.ContactMeGreen
import java.util.Locale

@Composable
fun IncomingCallScreen(
    onCallDismissed: () -> Unit,
    viewModel: IncomingCallViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.acceptCall()
        }
    }

    LaunchedEffect(uiState.activeCall, uiState.status) {
        if (uiState.activeCall == null && uiState.status == CallStatus.Ended) {
            onCallDismissed()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            CallPeerInfo(
                displayName = uiState.callerProfile?.displayName ?: "ContactMe User",
                phoneNumber = uiState.callerProfile?.phoneNumber,
                photoUrl = uiState.callerProfile?.photoUrl,
                statusLabel = if (uiState.status == CallStatus.Ringing) "Incoming voice call" else getCallStatusLabel(uiState.status, uiState.connectionState),
                durationSeconds = uiState.durationSeconds
            )

            Spacer(modifier = Modifier.weight(1f))

            if (uiState.status == CallStatus.Accepted || uiState.status == CallStatus.Connected || uiState.status == CallStatus.Connecting) {
                CallControlBar(
                    isMuted = uiState.isMuted,
                    isSpeakerEnabled = uiState.isSpeakerEnabled,
                    onToggleMute = viewModel::toggleMute,
                    onToggleSpeaker = viewModel::toggleSpeaker,
                    onEndCall = viewModel::endCall
                )
            } else {
                IncomingCallActions(
                    onReject = viewModel::rejectCall,
                    onAccept = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                            viewModel.acceptCall()
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun IncomingCallActions(
    onReject: () -> Unit,
    onAccept: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            FloatingActionButton(
                onClick = onReject,
                containerColor = Color.Red,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(imageVector = Icons.Default.CallEnd, contentDescription = "Reject")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Reject", style = MaterialTheme.typography.labelSmall)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            FloatingActionButton(
                onClick = onAccept,
                containerColor = ContactMeGreen,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(imageVector = Icons.Default.Call, contentDescription = "Accept")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Accept", style = MaterialTheme.typography.labelSmall)
        }
    }
}
