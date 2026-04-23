package com.bydnews.briefing.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.bydnews.briefing.di.ServiceLocator
import java.util.Calendar
import java.util.concurrent.TimeUnit

object Scheduler {

    const val UNIQUE_DAILY = "daily-briefing"
    const val UNIQUE_MANUAL = "manual-briefing"

    fun ensureScheduled(context: Context) {
        val prefs = ServiceLocator.prefs
        scheduleNextAt(context, prefs.scheduleHour, prefs.scheduleMinute)
    }

    fun reschedule(context: Context, hour: Int, minute: Int) {
        scheduleNextAt(context, hour, minute)
    }

    private fun scheduleNextAt(context: Context, hour: Int, minute: Int) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_MONTH, 1)
        }
        val delayMs = target.timeInMillis - now.timeInMillis

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<DailyBriefingWorker>()
            .setConstraints(constraints)
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .addTag(UNIQUE_DAILY)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(UNIQUE_DAILY, ExistingWorkPolicy.REPLACE, request)
    }

    fun enqueueManual(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<DailyBriefingWorker>()
            .setConstraints(constraints)
            .addTag(UNIQUE_MANUAL)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(UNIQUE_MANUAL, ExistingWorkPolicy.REPLACE, request)
    }
}
