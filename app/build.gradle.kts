plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    id("androidx.navigation.safeargs.kotlin")
    kotlin("kapt")
    id("kotlin-android")
}

android {

    signingConfigs {
        create("release") {
            keyAlias = "AppTsd"
            keyPassword = "prostotak"
            storePassword = "prostotak"
            storeFile = file("D:/Develop/Android/keys/mykeys.jks")
        }
    }

    packagingOptions {
        exclude ("META-INF/DEPENDENCIES")
        exclude ("META-INF/LICENSE")
        exclude ("META-INF/LICENSE.md")
        exclude ("META-INF/LICENSE.txt")
        exclude ("META-INF/license.txt")
        exclude ("META-INF/NOTICE")
        exclude ("META-INF/NOTICE.md")
        exclude ("META-INF/NOTICE.txt")
        exclude ("META-INF/notice.txt")
        exclude ("META-INF/ASL2.0")
        exclude ("META-INF/*.kotlin_module")
    }

    compileSdk = 31
//    buildToolsVersion = "29.0.3"

    defaultConfig {
        applicationId = "by.imlab.sosedi"

        minSdk = 24
        targetSdk = 31

        versionCode = 101036
        versionName = "1.1.36"

        setProperty("archivesBaseName", "$applicationId-v$versionName")

        javaCompileOptions {
            annotationProcessorOptions {
                //includeCompileClasspath = true
            }
        }
    }

    buildTypes {
        getByName("debug") {
            isDebuggable = true
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

/*    flavorDimensions("default") */

/*    productFlavors {
        create("release") {
        }
    }
*/

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    android {
        viewBinding.isEnabled = true
        dataBinding.isEnabled = true
    }
}

allprojects {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}

dependencies {
    api(project(":core"))
    api(project(":data"))

// JAX-B dependencies for JDK 9+
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:2.3.2")
    implementation("org.glassfish.jaxb:jaxb-runtime:2.3.2")

    // Zebra printer SDK
    implementation(files("libs/ZSDK_ANDROID_API.jar"))

    // Newland SDK
    implementation(files("libs/nlscan_sdk_master_user_v2.5.0.jar"))

    // Kotlin
    implementation(embeddedKotlin("stdlib-jdk8"))
    implementation("androidx.preference:preference:1.1.1")

    // Kotlin coroutines
    val coroutinesVersion = "1.3.5"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

    // Zxing
    val zxingVersion = "3.4.0"
    implementation("com.google.zxing:core:$zxingVersion")

    // UI
    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("com.google.android.material:material:1.6.0-alpha02")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation("com.github.razir.progressbutton:progressbutton:2.1.0")
    implementation("androidx.drawerlayout:drawerlayout:1.1.0-rc01")
    //implementation("com.github.satyajiit:TheSpotsDialog:0.94")
    implementation("com.github.d-max:spots-dialog:1.1@aar")
    //implementation("com.github.d-max:spots-dialog:1.2@aar")

    // Koin
    val koinVersion = "2.1.5"
    implementation("org.koin:koin-androidx-scope:$koinVersion")
    implementation("org.koin:koin-androidx-viewmodel:$koinVersion")
    implementation("org.koin:koin-androidx-fragment:$koinVersion")
    implementation("org.koin:koin-androidx-ext:$koinVersion")

    // Navigation
    val navigationVersion = "2.4.1"
    implementation("androidx.navigation:navigation-ui-ktx:$navigationVersion")
    implementation("androidx.navigation:navigation-fragment-ktx:$navigationVersion")
//    implementation("androidx.navigation:navigation-safe-args-gradle-plugin:$navigationVersion")

    // List
    val pagingVersion = "2.1.2"
    implementation("androidx.paging:paging-runtime:$pagingVersion")

    // Lifecycle
    val lifecycleVersion = "2.2.0"
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-extensions:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")

    // Permissions
    implementation("com.github.fondesa:kpermissions-coroutines:3.1.2")

    // Extentions
    implementation("androidx.core:core-ktx:1.2.0")

    // Auto service
    val autoServiceVersion = "1.0-rc6"
    implementation("com.google.auto.service:auto-service:$autoServiceVersion")
    kapt("com.google.auto.service:auto-service:$autoServiceVersion")

    implementation("org.apache.commons:commons-lang3:3.7")
}