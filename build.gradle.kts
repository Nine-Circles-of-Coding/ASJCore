
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    // The GTNH convention has no auto-applying Kotlin module (only Scala), but it does put the
    // Kotlin plugin on the buildscript classpath, so apply it without a version to use the
    // version the convention pins (2.2.21 as of gtnhgradle 2.0.20).
    kotlin("jvm")
    id("com.gtnewhorizons.gtnhconvention")
}

kotlin {
    compilerOptions {
        // 1.7.10 targets Java 8, same as the old build's compileKotlin.kotlinOptions.jvmTarget
        jvmTarget = JvmTarget.JVM_1_8
    }
}

dependencies {
    // Shipped inside the jar, unrelocated: ASJCore's KotlinAdapter provides the Kotlin
    // runtime for this mod and its dependents, so the real kotlin.** names must survive.
    // Version is taken from the Kotlin plugin above so the two cannot drift apart.
    "shadowImplementation"(kotlin("stdlib-jdk8"))
}

tasks.named<Jar>("shadowJar") {
    // Matches the exclusions from the old build.gradle's embed-into-jar step.
    // The annotations are compile-time only (CLASS retention), so dropping them is safe.
    exclude("org/intellij/lang/annotations/**")
    exclude("org/jetbrains/annotations/**")
    exclude("META-INF/maven/**")
    exclude("META-INF/versions/**")
}
