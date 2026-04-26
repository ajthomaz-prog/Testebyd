package com.bydnews.briefing

import android.app.Application
import androidx.work.Configuration
import com.bydnews.briefing.di.ServiceLocator
import com.bydnews.briefing.notify.Notifications
import com.bydnews.briefing.work.Scheduler

class BriefingApp : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
        Notifications.createChannels(this)
        Scheduler.ensureScheduled(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}
