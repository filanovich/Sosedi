plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
}


android {
    compileSdk = 31
//    buildToolsVersion = "29.0.3"

    defaultConfig {
        minSdk =24
        targetSdk = 31

        javaCompileOptions {
            annotationProcessorOptions {
                //includeCompileClasspath = true
            }
        }

        kapt {
            arguments {
                arg("room.schemaLocation", "$projectDir/schemas")
            }
        }
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
    api(project(":core"))

    // Kotlin
    implementation(embeddedKotlin("stdlib-jdk8"))

    // Lifecycle
    val lifecycleVersion = "2.2.0"
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")

    // Database
    val roomVersion = "2.4.0"
    implementation("androidx.room:room-ktx:$roomVersion")
    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    // Network
    val okHttpVersion = "4.7.2"
    val jacksonVersion = "2.11.0"
    val retrofitVersion = "2.9.0"
    implementation("com.squareup.okhttp3:logging-interceptor:$okHttpVersion")
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-jackson:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")

    // Koin
    val koinVersion = "2.1.5"
    implementation("org.koin:koin-core:$koinVersion")

    // Auto service
    val autoServiceVersion = "1.0-rc6"
    implementation("com.google.auto.service:auto-service:$autoServiceVersion")
    kapt("com.google.auto.service:auto-service:$autoServiceVersion")

    implementation("commons-net:commons-net:3.6")

}