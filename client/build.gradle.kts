/*
 * This file was generated by the Gradle 'init' task.
 */
import org.gradle.kotlin.dsl.`kotlin-dsl`

plugins {
    id("buildlogic.java-application-conventions")
    id("java-library")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    api(project(":game"))
}

application {
    // Define the main class for the application.
    mainClass = "feup.cpd.client.App";
}
