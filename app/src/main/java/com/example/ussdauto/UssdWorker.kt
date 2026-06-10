package com.example.ussdauto

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class UssdWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    companion object {
        private const val TAG = "UssdWorker"
        private const val USSD_NUMBER = "*322*64%23"
    }

    override fun doWork(): Result {
        return try {
            launchUssdCall()
            Log.d(TAG, "Appel USSD lancé : $USSD_NUMBER")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors du lancement USSD", e)
            Result.retry()
        }
    }

    private fun launchUssdCall() {
        val callIntent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$USSD_NUMBER")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(callIntent)
    }
}
