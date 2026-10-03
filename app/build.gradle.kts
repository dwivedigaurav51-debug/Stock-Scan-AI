plugins { id("com.android.application") }

android {
    namespace = "com.gaurav.stockscanai"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.gaurav.stockscanai"
        minSdk = 24
        targetSdk = 35
        versionCode = 4
        versionName = "4.0"
    }
}
dependencies { implementation("androidx.core:core:1.15.0") }