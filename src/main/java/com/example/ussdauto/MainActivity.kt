package com.example.ussdauto // ⚠️ Remplace par ton package

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

/**
 * Activité principale.
 * Rôle unique : vérifier/demander les permissions et guider l'utilisateur
 * pour activer le service d'accessibilité.
 */
class MainActivity : AppCompatActivity() {

    // Launcher pour la demande de permission CALL_PHONE
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            statusText.text = "✅ Permission accordée. Active le service d'accessibilité ci-dessous."
        } else {
            Toast.makeText(this, "Permission CALL_PHONE refusée — l'app ne peut pas fonctionner.", Toast.LENGTH_LONG).show()
        }
    }

    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Layout minimal inline (crée res/layout/activity_main.xml si tu préfères)
        setContentView(android.R.layout.simple_list_item_2)

        // ── UI minimaliste pour guider l'utilisateur ──
        // Remplace par ton vrai layout si besoin
        statusText = TextView(this).apply { text = "Initialisation…" }

        checkAndRequestPermissions()

        // Bouton pour ouvrir les paramètres d'accessibilité
        Button(this).apply {
            text = "Ouvrir Accessibilité"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }

        // Lance immédiatement un premier achat au démarrage de l'app
        Button(this).apply {
            text = "Lancer maintenant"
            setOnClickListener {
                SchedulerUtils.scheduleNextPurchase(applicationContext)
                Toast.makeText(context, "Premier achat planifié !", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
        } else {
            statusText.text = "✅ Permission déjà accordée."
        }
    }
}