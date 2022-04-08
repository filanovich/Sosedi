// Top-level build file where you can add configuration options common to all sub-projects/_A"

buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        // Navigation plugin
        classpath(
            group = "androidx.navigation",
            name = "navigation-safe-args-gradle-plugin",
            version = "2.2.0"
        )

        // Gradle plugins
        classpath("com.android.tools.build:gradle:7.1.1")
        //classpath(group = "com.android.tools.build", name = "gradle", version = "3.6.2")
        classpath(embeddedKotlin(module = "gradle-plugin"))
        //classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.20")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.4.1")


        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts files
    }
}

allprojects {
    repositories {
        google()
        jcenter()

    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}