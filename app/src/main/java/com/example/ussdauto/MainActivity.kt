package com.example.ussdauto

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
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

        val btnAccessibility = Button(this).apply {
            text = "Ouvrir Accessibilité"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }

        val btnStart = Button(this).apply {
            text = "Lancer le premier achat"
            setOnClickListener {
                SchedulerUtils.scheduleNextPurchase(applicationContext)
                Toast.makeText(context, "Premier achat planifié !", Toast.LENGTH_SHORT).show()
            }
        }

        val btnStop = Button(this).apply {
            text = "Arrêter"
            setOnClickListener {
                SchedulerUtils.cancelScheduled(applicationContext)
                Toast.makeText(context, "Planification annulée.", Toast.LENGTH_SHORT).show()
            }
        }

        layout.addView(statusText)
        layout.addView(btnAccessibility)
        layout.addView(btnStart)
        layout.addView(btnStop)
        setContentView(layout)

        checkPermissions()
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
        } else {
            statusText.text = "✅ Permission déjà accordée. Active le service d'accessibilité."
        }
    }
}
