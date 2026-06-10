package com.example.ussdauto

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class UssdAutomationService : AccessibilityService() {

    companion object {
        private const val TAG = "UssdAutoService"
        private const val MENU_TRIGGER = "YELOW ONE"
        private const val CONFIRM_TRIGGER = "L achat de votre Yellow One est reussi"
        private const val BTN_SEND = "Envoyer"
        private const val BTN_SEND_ALT = "Send"
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = serviceInfo.apply {
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
        }
        Log.d(TAG, "Service d'accessibilité connecté")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) return

        val rootNode = rootInActiveWindow ?: return
        val screenText = extractAllText(rootNode)

        when {
            screenText.contains(MENU_TRIGGER, ignoreCase = true) -> {
                Log.d(TAG, "Menu YELOW ONE détecté — saisie de '1'")
                handleMenuSelection(rootNode, choice = "1")
            }
            screenText.contains(CONFIRM_TRIGGER, ignoreCase = true) -> {
                Log.d(TAG, "Confirmation détectée — planification du prochain achat")
                SchedulerUtils.scheduleNextPurchase(applicationContext)
                dismissDialog(rootNode)
            }
        }

        rootNode.recycle()
    }

    override fun onInterrupt() {
        Log.w(TAG, "Service interrompu")
    }

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

    // Parcourt l'arbre pour trouver tous les EditText
    private fun findEditTexts(node: AccessibilityNodeInfo): List<AccessibilityNodeInfo> {
        val result = mutableListOf<AccessibilityNodeInfo>()
        fun traverse(n: AccessibilityNodeInfo?) {
            n ?: return
            if (n.className == "android.widget.EditText") result.add(n)
            for (i in 0 until n.childCount) traverse(n.getChild(i))
        }
        traverse(node)
        return result
    }

    private fun handleMenuSelection(root: AccessibilityNodeInfo, choice: String) {
        val inputs = findEditTexts(root)
        if (inputs.isEmpty()) {
            Log.w(TAG, "Aucun champ de saisie trouvé")
            return
        }
        val args = android.os.Bundle().apply {
            putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                choice
            )
        }
        inputs[0].performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        clickButton(root, BTN_SEND) || clickButton(root, BTN_SEND_ALT)
    }

    private fun clickButton(root: AccessibilityNodeInfo, label: String): Boolean {
        val buttons = root.findAccessibilityNodeInfosByText(label)
        if (!buttons.isNullOrEmpty()) {
            buttons[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Log.d(TAG, "Bouton '$label' cliqué")
            return true
        }
        return false
    }

    private fun dismissDialog(root: AccessibilityNodeInfo) {
        clickButton(root, "OK") || clickButton(root, "Fermer") || clickButton(root, "Close")
    }
}