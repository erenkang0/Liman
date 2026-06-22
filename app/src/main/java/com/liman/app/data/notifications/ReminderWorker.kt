package com.liman.app.data.notifications

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

/** Planlanan saatte nazik bir hatırlatma bildirimi gönderir. */
class ReminderWorker(
    context: Context,
    params: WorkerParameters,
) : Worker(context, params) {

    override fun doWork(): Result {
        val title = inputData.getString(KEY_TITLE) ?: "Liman"
        val text = inputData.getString(KEY_TEXT).orEmpty()
        val id = inputData.getInt(KEY_ID, 1)
        LimanNotifications.post(applicationContext, title, text, id)
        return Result.success()
    }

    companion object {
        const val KEY_TITLE = "title"
        const val KEY_TEXT = "text"
        const val KEY_ID = "id"
    }
}
