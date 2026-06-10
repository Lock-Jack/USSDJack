package com.example.ussdauto

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object SchedulerUtils {

    private const val WORK_NAME = "ussd_auto_purchase"
    private const val DELAY_HOURS = 23L
    private const val DELAY_MINUTES = 55L

    fun scheduleNextPurchase(context: Context) {
        val totalDelayMinutes = TimeUnit.HOURS.toMinutes(DELAY_HOURS) + DELAY_MINUTES

        val workRequest = OneTimeWorkRequestBuilder<UssdWorker>()
            .setInitialDelay(totalDelayMinutes, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun cancelScheduled(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
