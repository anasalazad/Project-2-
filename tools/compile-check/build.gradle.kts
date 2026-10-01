/*
 * Compile + unit-test check for environments WITHOUT the Android SDK (e.g. cloud sessions).
 *
 * Compiles all app Kotlin against JetBrains' desktop Compose (same androidx.compose APIs as the
 * app's Compose BOM) using small stand-ins in stubs/ for Android-only APIs, then runs the JVM
 * unit tests in app/src/test. It does NOT run Room's annotation processor or build the APK; the
 * real build is still Android Studio / ./gradlew from the project root.
 *
 * Run from this folder:  ./run.sh   (or: gradle test)
 * This is a separate Gradle build; Android Studio ignores it.
 */
plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.compose") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
}
repositories { mavenCentral() }
sourceSets {
    main {
        kotlin.srcDirs("stubs", "../../app/src/main/java")
    }
    test { kotlin.srcDirs("../../app/src/test/java") }
}
tasks.test { testLogging { events("failed"); exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL } }
kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-opt-in=kotlin.RequiresOptIn")
    }
}
configurations.all {
    exclude(group = "androidx.annotation")
    exclude(group = "androidx.collection")
    exclude(group = "androidx.arch.core")
    exclude(group = "androidx.lifecycle")
}
dependencies {
    val cmp = "1.7.3"
    implementation("org.jetbrains.compose.runtime:runtime-desktop:$cmp")
    implementation("org.jetbrains.compose.ui:ui-desktop:$cmp")
    implementation("org.jetbrains.compose.foundation:foundation-desktop:$cmp")
    implementation("org.jetbrains.compose.material3:material3-desktop:$cmp")
    implementation("org.jetbrains.compose.material3:material3-adaptive-navigation-suite-desktop:$cmp")
    implementation("org.jetbrains.compose.material:material-icons-extended-desktop:$cmp")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
}
