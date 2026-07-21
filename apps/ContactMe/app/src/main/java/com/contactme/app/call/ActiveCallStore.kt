package com.contactme.app.call

import android.content.Context

object ActiveCallStore {
    private const val PREFS = "active_call"
    private const val KEY_CALL_ID = "call_id"
    private const val KEY_ROLE = "role"
    private const val KEY_PEER_ID = "peer_id"
    private const val KEY_TYPE = "type"

    const val ROLE_OUTGOING = "outgoing"
    const val ROLE_INCOMING = "incoming"

    fun save(context: Context, callId: String, role: String, peerId: String, type: CallType) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CALL_ID, callId)
            .putString(KEY_ROLE, role)
            .putString(KEY_PEER_ID, peerId)
            .putString(KEY_TYPE, type.firestoreValue)
            .apply()
    }

    fun read(context: Context): ActiveCallInfo? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val callId = prefs.getString(KEY_CALL_ID, null)?.takeIf(String::isNotBlank) ?: return null
        val role = prefs.getString(KEY_ROLE, null)?.takeIf(String::isNotBlank) ?: return null
        val peerId = prefs.getString(KEY_PEER_ID, null).orEmpty()
        val type = CallType.fromFirestore(prefs.getString(KEY_TYPE, null))
        return ActiveCallInfo(callId = callId, role = role, peerId = peerId, type = type)
    }

    fun clear(context: Context, callId: String? = null) {
        val current = read(context)
        if (callId != null && current?.callId != callId) return
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }
}

data class ActiveCallInfo(
    val callId: String,
    val role: String,
    val peerId: String,
    val type: CallType
)
