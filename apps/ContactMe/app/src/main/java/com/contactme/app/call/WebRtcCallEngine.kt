package com.contactme.app.call

import android.content.Context
import android.media.AudioManager
import android.util.Log
import org.webrtc.*
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class WebRtcCallEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val factory: WebRtcEngineFactory
) {
    private val audioManager by lazy { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    private var peerConnection: PeerConnection? = null
    private var localAudioTrack: AudioTrack? = null
    private var localAudioSource: AudioSource? = null

    interface Listener {
        fun onLocalDescription(sdp: String)
        fun onIceCandidate(candidate: CallIceCandidate)
        fun onConnectionStateChange(state: PeerConnection.PeerConnectionState)
        fun onAudioTrackAdded()
    }

    private var listener: Listener? = null
    private var localUserId: String = ""

    fun initialize(localUserId: String, listener: Listener) {
        Log.d(TAG, "Initializing WebRtcCallEngine for $localUserId")
        this.localUserId = localUserId
        this.listener = listener
        val observer = object : PeerConnection.Observer {
            override fun onSignalingChange(state: PeerConnection.SignalingState?) {
                Log.d(TAG, "Signaling state change: $state")
            }
            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
                Log.d(TAG, "ICE connection change: $state")
            }
            override fun onIceConnectionReceivingChange(receiving: Boolean) {}
            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {
                Log.d(TAG, "ICE gathering change: $state")
            }
            
            override fun onIceCandidate(candidate: IceCandidate?) {
                candidate?.let {
                    Log.d(TAG, "New local ICE candidate: ${it.sdpMid}")
                    listener.onIceCandidate(
                        CallIceCandidate(
                            senderId = localUserId,
                            sdpMid = it.sdpMid,
                            sdpMLineIndex = it.sdpMLineIndex,
                            candidate = it.sdp
                        )
                    )
                }
            }

            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}
            override fun onAddStream(stream: MediaStream?) {
                Log.d(TAG, "Remote stream added")
            }
            override fun onRemoveStream(stream: MediaStream?) {
                Log.d(TAG, "Remote stream removed")
            }
            override fun onDataChannel(channel: DataChannel?) {}
            override fun onRenegotiationNeeded() {
                Log.d(TAG, "Renegotiation needed")
            }

            override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) {
                Log.d(TAG, "Remote track added: ${receiver?.track()?.kind()}")
                if (receiver?.track()?.kind() == "audio") {
                    listener.onAudioTrackAdded()
                }
            }

            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                Log.d(TAG, "Connection state change: $newState")
                newState?.let { listener.onConnectionStateChange(it) }
            }
        }

        peerConnection = factory.createPeerConnection(observer)
        setupAudioTrack()
    }

    fun setRemoteOffer(sdp: String) {
        Log.d(TAG, "Setting remote offer")
        val remoteSdp = SessionDescription(SessionDescription.Type.OFFER, sdp)
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onSetSuccess() {
                Log.d(TAG, "Remote offer set successfully")
            }
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(error: String?) {
                Log.e(TAG, "Failed to set remote offer: $error")
            }
        }, remoteSdp)
    }

    fun createAnswer() {
        Log.d(TAG, "Creating answer")
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
        }

        peerConnection?.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                Log.d(TAG, "Answer created successfully")
                sdp?.let {
                    peerConnection?.setLocalDescription(object : SdpObserver {
                        override fun onCreateSuccess(p0: SessionDescription?) {}
                        override fun onSetSuccess() {
                            Log.d(TAG, "Local description (answer) set successfully")
                            listener?.onLocalDescription(it.description)
                        }
                        override fun onCreateFailure(p0: String?) {}
                        override fun onSetFailure(error: String?) {
                            Log.e(TAG, "Failed to set local description (answer): $error")
                        }
                    }, it)
                }
            }

            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String?) {
                Log.e(TAG, "Failed to create answer: $error")
            }
            override fun onSetFailure(error: String?) {}
        }, constraints)
    }

    private fun setupAudioTrack() {
        Log.d(TAG, "Setting up local audio track")
        val audioConstraints = MediaConstraints()
        localAudioSource = factory.peerConnectionFactory().createAudioSource(audioConstraints)
        localAudioTrack = factory.peerConnectionFactory().createAudioTrack("ARDAMSa0", localAudioSource)
        localAudioTrack?.setEnabled(true)
        peerConnection?.addTrack(localAudioTrack, listOf("ARDAMS"))
    }

    fun createOffer() {
        Log.d(TAG, "Creating offer")
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
        }

        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                Log.d(TAG, "Offer created successfully")
                sdp?.let {
                    peerConnection?.setLocalDescription(object : SdpObserver {
                        override fun onCreateSuccess(p0: SessionDescription?) {}
                        override fun onSetSuccess() {
                            Log.d(TAG, "Local description (offer) set successfully")
                            listener?.onLocalDescription(it.description)
                        }
                        override fun onCreateFailure(p0: String?) {}
                        override fun onSetFailure(error: String?) {
                            Log.e(TAG, "Failed to set local description (offer): $error")
                        }
                    }, it)
                }
            }

            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String?) {
                Log.e(TAG, "Failed to create offer: $error")
            }
            override fun onSetFailure(error: String?) {}
        }, constraints)
    }

    fun setRemoteDescription(sdp: String) {
        Log.d(TAG, "Setting remote description (answer)")
        val remoteSdp = SessionDescription(SessionDescription.Type.ANSWER, sdp)
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onSetSuccess() {
                Log.d(TAG, "Remote description (answer) set successfully")
            }
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(error: String?) {
                Log.e(TAG, "Failed to set remote description (answer): $error")
            }
        }, remoteSdp)
    }

    fun addIceCandidate(candidate: CallIceCandidate) {
        Log.d(TAG, "Adding remote ICE candidate: ${candidate.sdpMid}")
        peerConnection?.addIceCandidate(
            IceCandidate(candidate.sdpMid, candidate.sdpMLineIndex, candidate.candidate)
        )
    }

    fun setMuted(isMuted: Boolean) {
        Log.d(TAG, "Setting muted: $isMuted")
        localAudioTrack?.setEnabled(!isMuted)
    }

    fun setSpeakerEnabled(enabled: Boolean) {
        Log.d(TAG, "Setting speaker enabled: $enabled")
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.isSpeakerphoneOn = enabled
    }

    fun release() {
        Log.d(TAG, "Releasing WebRtcCallEngine")
        audioManager.mode = AudioManager.MODE_NORMAL
        audioManager.isSpeakerphoneOn = false
        peerConnection?.close()
        peerConnection = null
        localAudioTrack?.dispose()
        localAudioTrack = null
        localAudioSource?.dispose()
        localAudioSource = null
        listener = null
    }

    private companion object {
        const val TAG = "WebRtcCallEngine"
    }
}
