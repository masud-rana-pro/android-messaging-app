package com.contactme.app.notification

interface DeviceTokenRepository {
    suspend fun syncCurrentDeviceToken(userId: String): DeviceTokenResult

    suspend fun syncRefreshedToken(token: String): DeviceTokenResult

    suspend fun removeCurrentDeviceToken(userId: String): DeviceTokenResult
}
