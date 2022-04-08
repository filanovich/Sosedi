plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("android.extensions")
}

android {
    compileSdk = 31
//    buildToolsVersion = "29.0.3"

    defaultConfig {
        minSdk = 24
        targetSdk = 31
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

dependencies {
    // Kotlin
    implementation(embeddedKotlin("stdlib-jdk8"))

    // Kotlin coroutines
    val coroutinesVersion = "1.3.5"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    // Lifecycle
    val lifecycleVersion = "2.2.0"
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")

    // Koin
    val koinVersion = "2.1.5"
    implementation("org.koin:koin-core:$koinVersion")
}