plugins { id("com.android.application") }
android { namespace = "com.gaurav.stockscanai"; compileSdk = 35; defaultConfig { applicationId = "com.gaurav.stockscanai"; minSdk = 24; targetSdk = 35; versionCode = 9; versionName = "7.2" } }
dependencies { implementation("androidx.core:core:1.15.0") }
