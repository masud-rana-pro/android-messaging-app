package com.contactme.app.call

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.webrtc.PeerConnection

@Composable
fun OutgoingCallScreen(
    receiverId: String,
    onCallEnded: () -> Unit,
    viewModel: OutgoingCallViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(receiverId) {
        viewModel.startCall(receiverId, CallType.Audio)
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

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Calling...", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Receiver: $receiverId")
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Status: ${uiState.status.name}")
        Text(text = "Connection: ${uiState.connectionState.name}")
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { 
                viewModel.cancelCall()
                onCallEnded()
            }, 
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Cancel")
        }
    }
}
