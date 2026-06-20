package com.contactme.app.call

import com.contactme.app.BuildConfig
import org.webrtc.PeerConnection

object WebRtcConfig {
    private const val GOOGLE_STUN = "stun:stun.l.google.com:19302"

    fun iceServers(): List<PeerConnection.IceServer> = buildList {
        add(PeerConnection.IceServer.builder(GOOGLE_STUN).createIceServer())
        val turnUrl = BuildConfig.WEBRTC_TURN_URL.trim()
        val username = BuildConfig.WEBRTC_TURN_USERNAME.trim()
        val password = BuildConfig.WEBRTC_TURN_PASSWORD
        if (turnUrl.isNotBlank() && username.isNotBlank() && password.isNotBlank()) {
            add(
                PeerConnection.IceServer.builder(turnUrl)
                    .setUsername(username)
                    .setPassword(password)
                    .createIceServer()
            )
        }
    }

    fun rtcConfiguration() = PeerConnection.RTCConfiguration(iceServers()).apply {
        sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
    }
}
