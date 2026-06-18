package com.contactme.app

import android.app.Application
import com.contactme.app.notification.ContactMeNotificationChannels
import com.contactme.app.notification.NotificationVisibilityTracker
import dagger.hilt.android.HiltAndroidApp
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

@HiltAndroidApp
class ContactMeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ContactMeNotificationChannels.create(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    NotificationVisibilityTracker.setAppForeground(true)
                }

                override fun onStop(owner: LifecycleOwner) {
                    NotificationVisibilityTracker.setAppForeground(false)
                }
            }
        )
    }
}
