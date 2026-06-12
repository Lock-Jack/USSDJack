package com.example.ussdauto

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var timerText: TextView
    private var countDownTimer: CountDownTimer? = null

    // 23h55 en millisecondes
    private val DELAY_MS = (23 * 60 + 55) * 60 * 1000L

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        statusText.text = if (granted)
            "✅ Permission accordée. Active le service d'accessibilité."
        else
            "❌ Permission CALL_PHONE refusée."
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }

        statusText = TextView(this).apply {
            text = "Vérification des permissions..."
            textSize = 16f
        }

        // Chronomètre — affiche le temps restant avant le prochain achat
        timerText = TextView(this).apply {
            text = "⏱ Prochain achat : --:--:--"
            textSize = 20f
            setPadding(0, 24, 0, 24)
        }

        val btnAccessibility = Button(this).apply {
            text = "Ouvrir Accessibilité"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }

        val btnStart = Button(this).apply {
            text = "Lancer le premier achat"
            setOnClickListener {
                if (ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CALL_PHONE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // 1. Lance l'appel USSD immédiatement
                    launchUssdNow()
                    // 2. Planifie le suivant dans 23h55
                    SchedulerUtils.scheduleNextPurchase(applicationContext)
                    // 3. Démarre le chronomètre
                    startCountdown(DELAY_MS)
                    Toast.makeText(context, "Appel USSD lancé !", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Permission CALL_PHONE manquante !", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val btnStop = Button(this).apply {
            text = "Arrêter"
            setOnClickListener {
                SchedulerUtils.cancelScheduled(applicationContext)
                countDownTimer?.cancel()
                timerText.text = "⏱ Arrêté"
                Toast.makeText(context, "Planification annulée.", Toast.LENGTH_SHORT).show()
            }
        }

        layout.addView(statusText)
        layout.addView(timerText)
        layout.addView(btnAccessibility)
        layout.addView(btnStart)
        layout.addView(btnStop)
        setContentView(layout)

        checkPermissions()
    }

    // Lance l'appel USSD directement sans passer par WorkManager
    private fun launchUssdNow() {
        val callIntent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:*322*64%23")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(callIntent)
    }

    // Démarre le compte à rebours affiché à l'écran
    private fun startCountdown(millisTotal: Long) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(millisTotal, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val h = millisUntilFinished / 3600000
                val m = (millisUntilFinished % 3600000) / 60000
                val s = (millisUntilFinished % 60000) / 1000
                timerText.text = "⏱ Prochain achat dans : %02d:%02d:%02d".format(h, m, s)
            }
            override fun onFinish() {
                timerText.text = "⏱ Lancement en cours..."
            }
        }.start()
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
        } else {
            statusText.text = "✅ Permission accordée. Active le service d'accessibilité."
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
