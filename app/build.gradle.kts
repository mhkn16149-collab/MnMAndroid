plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.mnm.downloader"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mnm.downloader"
        minSdk = 23
        targetSdk = 35
        versionCode = 3
        versionName = "1.2"
    }
}
