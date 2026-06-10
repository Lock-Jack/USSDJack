package com.example.ussdauto // ⚠️ Remplace par ton package

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Service d'accessibilité qui surveille les popups USSD du système.
 *
 * Flux attendu Telma Madagascar :
 *   1. Popup "YELOW ONE"           → saisir "1" + clic "Envoyer"
 *   2. Popup de confirmation finale → planifier le prochain achat
 *
 * ⚠️ L'utilisateur doit activer ce service manuellement :
 *    Paramètres → Accessibilité → UssdAuto
 */
class UssdAutomationService : AccessibilityService() {

    companion object {
        private const val TAG = "UssdAutoService"

        // ── Textes à détecter dans les popups USSD ──
        // Adapte ces chaînes si Telma modifie ses libellés
        private const val MENU_TRIGGER = "YELOW ONE"
        private const val CONFIRM_TRIGGER = "L achat de votre Yellow One est reussi"

        // Labels des boutons d'action du clavier USSD système
        private const val BTN_SEND = "Envoyer"
        private const val BTN_SEND_ALT = "Send" // fallback anglais
    }

    // ────────────────────────────────────────────────
    // Cycle de vie du service
    // ────────────────────────────────────────────────

    override fun onServiceConnected() {
        super.onServiceConnected()
        // Configuration programmatique (en complément du XML)
        serviceInfo = serviceInfo.apply {
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
        }
        Log.d(TAG, "Service d'accessibilité connecté")
    }

    // ────────────────────────────────────────────────
    // Point d'entrée principal : chaque événement d'accessibilité
    // ────────────────────────────────────────────────

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        // On ne traite que les changements d'état de fenêtre (nouvelles popups)
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) return

        val rootNode = rootInActiveWindow ?: return
        val screenText = extractAllText(rootNode)

        when {
            // ── Étape 1 : Menu "YELOW ONE" détecté ──
            screenText.contains(MENU_TRIGGER, ignoreCase = true) -> {
                Log.d(TAG, "Menu YELOW ONE détecté — saisie de '1'")
                handleMenuSelection(rootNode, choice = "1")
            }

            // ── Étape 2 : Confirmation d'achat réussie ──
            screenText.contains(CONFIRM_TRIGGER, ignoreCase = true) -> {
                Log.d(TAG, "Confirmation détectée — planification du prochain achat")
                SchedulerUtils.scheduleNextPurchase(applicationContext)
                dismissDialog(rootNode) // Ferme la popup de confirmation
            }
        }

        rootNode.recycle()
    }

    override fun onInterrupt() {
        Log.w(TAG, "Service interrompu")
    }

    // ────────────────────────────────────────────────
    // Helpers privés
    // ────────────────────────────────────────────────

    /**
     * Extrait récursivement tout le texte visible dans l'arbre d'accessibilité.
     */
    private fun extractAllText(node: AccessibilityNodeInfo): String {
        val sb = StringBuilder()
        fun traverse(n: AccessibilityNodeInfo?) {
            n ?: return
            n.text?.let { sb.append(it).append(" ") }
            n.contentDescription?.let { sb.append(it).append(" ") }
            for (i in 0 until n.childCount) traverse(n.getChild(i))
        }
        traverse(node)
        return sb.toString()
    }

    /**
     * Saisit [choice] dans le champ de texte du menu USSD,
     * puis clique sur le bouton "Envoyer".
     */
    private fun handleMenuSelection(root: AccessibilityNodeInfo, choice: String) {
        // 1. Trouver le champ de saisie EditText
        val inputs = root.findAccessibilityNodeInfosByClassName("android.widget.EditText")
        if (inputs.isNullOrEmpty()) {
            Log.w(TAG, "Aucun champ de saisie trouvé")
            return
        }
        val inputField = inputs[0]

        // 2. Y injecter le texte (remplace le contenu existant)
        val args = android.os.Bundle().apply {
            putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                choice
            )
        }
        inputField.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)

        // 3. Cliquer sur le bouton "Envoyer"
        clickButton(root, BTN_SEND) || clickButton(root, BTN_SEND_ALT)
    }

    /**
     * Clique sur le premier bouton dont le texte correspond à [label].
     * Retourne true si le bouton a été trouvé et cliqué.
     */
    private fun clickButton(root: AccessibilityNodeInfo, label: String): Boolean {
        val buttons = root.findAccessibilityNodeInfosByText(label)
        if (!buttons.isNullOrEmpty()) {
            buttons[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Log.d(TAG, "Bouton '$label' cliqué")
            return true
        }
        return false
    }

    /**
     * Ferme une popup USSD en cliquant sur "OK" ou "Fermer".
     */
    private fun dismissDialog(root: AccessibilityNodeInfo) {
        clickButton(root, "OK") || clickButton(root, "Fermer") || clickButton(root, "Close")
    }
}