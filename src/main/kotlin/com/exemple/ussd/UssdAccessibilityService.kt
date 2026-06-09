package com.exemple.ussd

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class UssdAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        // Configuration dynamique du service pour éviter la dépendance aux fichiers XML lourds
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
            // Cibler spécifiquement les fenêtres système pour intercepter les dialogues USSD
            packageNames = arrayOf("com.android.phone", "com.android.server.telecom", "com.mediatek.telephony")
        }
        this.serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val sourceNode = event.source ?: return

        // Recherche du texte spécifique au pop-up TELMA Yellow One
        if (findNodeByText(sourceNode, "YELLOW ONE!") || findNodeByText(sourceNode, "En profiter?")) {
            Log.d("USSD_SERVICE", "Dialogue USSD détecté !")
            handleUssdDialog(sourceNode)
        }
    }

    private fun handleUssdDialog(rootNode: AccessibilityNodeInfo) {
        // 1. Trouver le champ de texte (EditText)
        val inputNode = findInputNode(rootNode)
        
        if (inputNode != null) {
            // Insérer "1" dans le champ de saisie
            val arguments = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "1")
            }
            inputNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)

            // 2. Trouver et cliquer sur le bouton "Envoyer" ou "Sended"
            val sendButton = findButtonByText(rootNode, "Envoyer") ?: findButtonByText(rootNode, "Send")
            sendButton?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
    }

    private fun findNodeByText(node: AccessibilityNodeInfo, text: String): Boolean {
        val nodes = node.findAccessibilityNodeInfosByText(text)
        return nodes != null && nodes.isNotEmpty()
    }

    private fun findInputNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.className == "android.widget.EditText") {
            return node
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findInputNode(child)
            if (result != null) return result
        }
        return null
    }

    private fun findButtonByText(node: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        val nodes = node.findAccessibilityNodeInfosByText(text)
        if (nodes != null) {
            for (target in nodes) {
                if (target.className == "android.widget.Button") {
                    return target
                }
            }
        }
        return null
    }

    override fun onInterrupt() {
        // Gestion de l'interruption du service si nécessaire
    }
}