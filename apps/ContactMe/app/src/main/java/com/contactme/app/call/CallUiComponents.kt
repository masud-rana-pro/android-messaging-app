package com.contactme.app.call

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.webrtc.PeerConnection
import java.util.Locale
import com.contactme.app.ui.theme.ContactMeCharcoal
import com.contactme.app.ui.theme.ContactMeGreenDark
import androidx.compose.ui.viewinterop.AndroidView
import org.webrtc.SurfaceViewRenderer
import org.webrtc.RendererCommon

private val CallBackgroundTop = Color(0xFF061513)
private val CallBackgroundMiddle = Color(0xFF0A2320)
private val CallBackgroundBottom = Color(0xFF111619)
val CallTextPrimary = Color(0xFFF5FFFC)
val CallTextSecondary = Color(0xFFC6D7D3)
val CallPanelColor = Color(0xFF0F2E2A)

@Composable
fun CallScreenSurface(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CallBackgroundTop,
                        CallBackgroundMiddle,
                        CallBackgroundBottom
                    )
                )
            ),
        content = content
    )
}

@Composable
fun CallVideoPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF081412),
                        Color(0xFF102B27),
                        Color(0xFF111619)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun VideoRenderer(
    modifier: Modifier = Modifier,
    onSurfaceReady: (SurfaceViewRenderer) -> Unit
) {
    AndroidView(
        factory = { context ->
            SurfaceViewRenderer(context).apply {
                init(WebRtcEngineFactory.staticEglContext, null)
                setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                setMirror(true)
                onSurfaceReady(this)
            }
        },
        modifier = modifier
    )
}

@Composable
fun CallPeerInfo(
    displayName: String,
    phoneNumber: String?,
    photoUrl: String?,
    statusLabel: String,
    durationSeconds: Long
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (!photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = displayName.take(1).uppercase(),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = ContactMeCharcoal
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = displayName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = CallTextPrimary
        )
        
        if (!phoneNumber.isNullOrBlank()) {
            Text(text = phoneNumber, style = MaterialTheme.typography.bodyLarge, color = CallTextSecondary)
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(text = statusLabel, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        
        if (durationSeconds > 0) {
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60
            Text(
                text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds),
                style = MaterialTheme.typography.bodyLarge,
                color = CallTextPrimary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

fun getCallStatusLabel(status: CallStatus, connectionState: PeerConnection.PeerConnectionState): String {
    if (status == CallStatus.Accepted || status == CallStatus.Connected || status == CallStatus.Connecting) {
        return when (connectionState) {
            PeerConnection.PeerConnectionState.CONNECTED -> "Connected"
            PeerConnection.PeerConnectionState.CONNECTING -> "Connecting..."
            PeerConnection.PeerConnectionState.FAILED -> "Connection failed"
            PeerConnection.PeerConnectionState.DISCONNECTED -> "Reconnecting..."
            else -> "Connecting..."
        }
    }
    return when(status) {
        CallStatus.Ringing -> "Ringing..."
        CallStatus.Ended -> "Call ended"
        CallStatus.Rejected -> "Declined"
        CallStatus.Busy -> "User busy"
        CallStatus.Timeout -> "No answer"
        CallStatus.Cancelled -> "Cancelled"
        CallStatus.Missed -> "Missed call"
        else -> "Calling..."
    }
}

@Composable
fun CallControlBar(
    isMuted: Boolean,
    isSpeakerEnabled: Boolean,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onEndCall: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ControlIconButton(
            onClick = onToggleMute,
            icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
            label = if (isMuted) "Unmute" else "Mute",
            containerColor = if (isMuted) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
            contentColor = if (isMuted) MaterialTheme.colorScheme.onErrorContainer else ContactMeGreenDark
        )

        FloatingActionButton(
            onClick = onEndCall,
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.size(72.dp)
        ) {
            Icon(imageVector = Icons.Default.CallEnd, contentDescription = "End Call", modifier = Modifier.size(32.dp))
        }

        ControlIconButton(
            onClick = onToggleSpeaker,
            icon = if (isSpeakerEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
            label = if (isSpeakerEnabled) "Speaker" else "Earpiece",
            containerColor = if (isSpeakerEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
            contentColor = if (isSpeakerEnabled) MaterialTheme.colorScheme.onPrimary else ContactMeGreenDark
        )
    }
}

@Composable
private fun ControlIconButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledIconButton(
            onClick = onClick,
            modifier = Modifier.size(56.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = containerColor,
                contentColor = contentColor
            )
        ) {
            Icon(imageVector = icon, contentDescription = label)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = CallTextPrimary)
    }
}

@Composable
fun LocalVideoPreviewContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CallPanelColor.copy(alpha = 0.88f)),
        content = content
    )
}
