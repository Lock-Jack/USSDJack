package com.example.ussdauto // ⚠️ Remplace par ton package

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * Worker exécuté par WorkManager au moment planifié.
 * Lance l'appel USSD *322*64# via un Intent ACTION_CALL.
 *
 * ⚠️ La permission CALL_PHONE doit avoir été accordée au préalable
 *    (demandée dans MainActivity).
 */
class UssdWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    companion object {
        private const val TAG = "UssdWorker"

        // ── Numéro USSD Telma Madagascar pour Yellow One 64 ──
        // Le # est encodé en %23 pour être valide dans un URI tel:
        private const val USSD_NUMBER = "*322*64%23"
    }

    override fun doWork(): Result {
        return try {
            launchUssdCall()
            Log.d(TAG, "Appel USSD lancé : $USSD_NUMBER")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors du lancement USSD", e)
            Result.retry() // WorkManager retentera automatiquement
        }
    }

    /**
     * Lance l'appel USSD via un Intent standard ACTION_CALL.
     * FLAG_ACTIVITY_NEW_TASK requis car on sort du contexte d'une Activity.
     */
    private fun launchUssdCall() {
        val callIntent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$USSD_NUMBER")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(callIntent)
    }
}