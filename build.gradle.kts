plugins {
    id("com.android.application") version "8.2.2" // Version compatible avec les outils CLI récents
    id("org.jetbrains.kotlin.android") version "1.9.22"
}

android {
    namespace = "com.exemple.ussd"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.exemple.ussd"
        minSdk = 26 // Android 8.0 minimum pour une bonne gestion de l'accessibilité
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true // Active ProGuard pour réduire la taille au minimum
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
}