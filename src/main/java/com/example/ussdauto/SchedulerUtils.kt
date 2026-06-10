package com.example.ussdauto // ⚠️ Remplace par ton package

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object SchedulerUtils {

    // Nom unique du travail — permet à REPLACE de cibler le bon Worker
    private const val WORK_NAME = "ussd_auto_purchase"

    // Délai avant le prochain achat : 23h55m
    private const val DELAY_HOURS = 23L
    private const val DELAY_MINUTES = 55L

    /**
     * Planifie le prochain appel USSD dans 23h55.
     * Si une demande précédente est déjà en attente, elle est remplacée (REPLACE).
     * Appelle cette fonction dès que la confirmation d'achat est détectée.
     */
    fun scheduleNextPurchase(context: Context) {
        val totalDelayMinutes = TimeUnit.HOURS.toMinutes(DELAY_HOURS) + DELAY_MINUTES

        val workRequest = OneTimeWorkRequestBuilder<UssdWorker>()
            .setInitialDelay(totalDelayMinutes, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE, // Annule et remplace si déjà planifié
            workRequest
        )
    }

    /**
     * Annule le prochain achat planifié (utile pour un bouton Stop dans l'UI).
     */
    fun cancelScheduled(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}