plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.eva20"
    compileSdk = 34  // Set to a stable version

    defaultConfig {
        applicationId = "com.example.eva20"
        minSdk = 25
        targetSdk = 34  // Set to a stable version
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    // Prevent duplicate file errors
    packagingOptions {
        resources {
            excludes += listOf(
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0",
                "META-INF/DEPENDENCIES",
                "META-INF/INDEX.LIST"
            )
        }
    }
}

dependencies {
    // Core Android libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    // Navigation component
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // ViewModel and LiveData
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Retrofit for API calls
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    // OkHttp for WebSockets and networking
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Coroutines for asynchronous programming
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Swipe refresh layout
    implementation(libs.androidx.swiperefreshlayout)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Just the Google Auth library for service account auth
    implementation(libs.google.auth.library.oauth2.http)
}

// Correct Kotlin DSL syntax for configurations
configurations.all {
    exclude(group = "org.bouncycastle", module = "bcprov-jdk15on")
    exclude(group = "org.bouncycastle", module = "bcpkix-jdk15on")
}