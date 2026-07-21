package com.contactme.app.call

import com.contactme.app.BuildConfig
import org.webrtc.PeerConnection

object WebRtcConfig {
    private const val METERED_STUN = "stun:stun.relay.metered.ca:80"
    private const val GOOGLE_STUN = "stun:stun.l.google.com:19302"

    fun iceServers(): List<PeerConnection.IceServer> = buildList {
        val turnUrls = BuildConfig.WEBRTC_TURN_URL
            .split(',')
            .map(String::trim)
            .filter(String::isNotBlank)
        val username = BuildConfig.WEBRTC_TURN_USERNAME.trim()
        val password = BuildConfig.WEBRTC_TURN_PASSWORD
        if (turnUrls.isNotEmpty() && username.isNotBlank() && password.isNotBlank()) {
            add(
                PeerConnection.IceServer.builder(turnUrls)
                    .setUsername(username)
                    .setPassword(password)
                    .createIceServer()
            )
        }
        add(PeerConnection.IceServer.builder(METERED_STUN).createIceServer())
        add(PeerConnection.IceServer.builder(GOOGLE_STUN).createIceServer())
    }

    fun hasTurnRelay(): Boolean =
        BuildConfig.WEBRTC_TURN_URL.split(',').any { it.isNotBlank() } &&
            BuildConfig.WEBRTC_TURN_USERNAME.isNotBlank() &&
            BuildConfig.WEBRTC_TURN_PASSWORD.isNotBlank()

    fun rtcConfiguration() = PeerConnection.RTCConfiguration(iceServers()).apply {
        sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        iceTransportsType = if (hasTurnRelay()) {
            PeerConnection.IceTransportsType.RELAY
        } else {
            PeerConnection.IceTransportsType.ALL
        }
        tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.ENABLED
        bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
        rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
        iceCandidatePoolSize = 4
    }
}
