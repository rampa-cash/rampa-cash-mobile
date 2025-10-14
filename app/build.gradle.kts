plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.dagger.hilt.android)
}

android {
    namespace = "com.example.rampacashmobile"
    compileSdk = 36
    android.buildFeatures.buildConfig = true

    defaultConfig {
        applicationId = "com.example.rampacashmobile"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "RPC_URI", "\"https://api.devnet.solana.com\"")
            buildConfigField("String", "NETWORK_TYPE", "\"devnet\"")
            buildConfigField("Boolean", "ENABLE_SIMULATION", "false")
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
        release {
            buildConfigField("String", "RPC_URI", "\"https://api.devnet.solana.com\"")
            buildConfigField("String", "NETWORK_TYPE", "\"devnet\"")
            buildConfigField("Boolean", "ENABLE_SIMULATION", "false")
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
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
}

dependencies {
    implementation(libs.mobile.wallet.adapter.clientlib.ktx)
    implementation(libs.web3.solana)
    implementation(libs.rpc.core)
    implementation(libs.multimult)
    implementation(libs.sol4k)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("io.mockk:mockk:1.13.8")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.navigation.compose)
    // Splash Screen
    implementation(libs.androidx.core.splashscreen)
    // QR Code generation
    implementation(libs.zxing.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.android)
    implementation(libs.dagger.hilt.android)
    implementation(libs.web3auth.android.sdk)
    implementation(libs.core)
    implementation(libs.timber)
    ksp(libs.dagger.hilt.compiler)
}
