package com.liman.app.data.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Günlük hatırlatmaları WorkManager ile planlar. Planlama cihaz yeniden
 * başlatıldığında da korunur (WorkManager kalıcıdır).
 */
object ReminderScheduler {

    fun schedule(
        context: Context,
        kind: ReminderKind,
        hour: Int = kind.defaultHour,
        minute: Int = kind.defaultMinute,
    ) {
        val data = workDataOf(
            ReminderWorker.KEY_TITLE to kind.title,
            ReminderWorker.KEY_TEXT to kind.message,
            ReminderWorker.KEY_ID to kind.notifId,
        )
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelayMillis(hour, minute), TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            kind.uniqueName,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun cancel(context: Context, kind: ReminderKind) {
        WorkManager.getInstance(context).cancelUniqueWork(kind.uniqueName)
    }

    private fun initialDelayMillis(hour: Int, minute: Int): Long {
        val now = System.currentTimeMillis()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now) add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis - now
    }
}
