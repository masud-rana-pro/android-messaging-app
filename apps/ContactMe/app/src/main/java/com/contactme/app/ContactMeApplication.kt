package com.contactme.app

import android.app.Application
import com.contactme.app.notification.ContactMeNotificationChannels
import com.contactme.app.notification.NotificationVisibilityTracker
import dagger.hilt.android.HiltAndroidApp
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import javax.inject.Inject

@HiltAndroidApp
class ContactMeApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

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
