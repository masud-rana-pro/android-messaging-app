package com.contactme.app

import android.app.Application
import com.contactme.app.notification.ContactMeNotificationChannels
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ContactMeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ContactMeNotificationChannels.create(this)
    }
}
