# UssdAuto - Automatisation USSD (Telma Madagascar)

Ce projet Android permet d'automatiser l'achat récurrent du forfait **Yellow One** via les codes USSD de l'opérateur Telma Madagascar.

## 🚀 Fonctionnement

L'application utilise deux composants majeurs du système Android pour contourner l'absence d'API USSD officielle :

1.  **WorkManager (`UssdWorker`)** : Planifie le lancement de l'appel USSD (`*322*64#`) à intervalles réguliers (toutes les 23h55).
2.  **Accessibility Service (`UssdAutomationService`)** : Surveille l'écran. Lorsqu'il détecte la popup système USSD, il interagit automatiquement avec elle (saisie du choix "1", clic sur "Envoyer", et fermeture de la confirmation).

## 🛠 Installation & Configuration

### 1. Prérequis techniques
- Modifier le `package` (`com.example.ussdauto`) dans les fichiers Java/Kotlin et le `AndroidManifest.xml` pour correspondre à votre projet.
- Compiler et installer l'APK sur l'appareil cible.

### 2. Permissions
Au lancement, l'application demandera la permission suivante :
- **Appels téléphoniques (`CALL_PHONE`)** : Nécessaire pour initier le code USSD sans intervention manuelle.

### 3. Activation du service d'accessibilité (CRITIQUE)
Pour que l'automatisation des menus fonctionne, l'utilisateur doit activer manuellement le service :
1. Aller dans les **Paramètres** du téléphone.
2. Rechercher **Accessibilité**.
3. Sous "Services installés" ou "Services téléchargés", sélectionner **USSD Auto Service**.
4. Activer le commutateur.

## 📂 Structure du projet

- `MainActivity` : Gère l'interface de démarrage et la demande de permissions.
- `UssdWorker` : Déclenche l'appel via un Intent `ACTION_CALL`.
- `UssdAutomationService` : Analyse l'arbre de vue (Accessibility Node Info) pour remplir les champs de texte et cliquer sur les boutons.
- `SchedulerUtils` : Logique de planification du prochain achat via `WorkManager`.

## ⚠️ Avertissements

- **Interface Opérateur** : Si Telma change le libellé de ses menus (ex: "YELOW ONE" devient "Yellow 1"), les constantes dans `UssdAutomationService.kt` devront être mises à jour.
- **Batterie** : L'utilisation de `WorkManager` est optimisée pour la batterie, mais certains constructeurs (Xiaomi, Huawei, Samsung) peuvent tuer les services en arrière-plan. Il est conseillé de désactiver l'optimisation de batterie pour cette application.
- **Sécurité** : Les services d'accessibilité sont puissants. Ce service ne lit que les popups contenant des mots-clés spécifiques liés aux USSD.

## 📝 Licence
Projet réalisé à des fins d'automatisation personnelle.
