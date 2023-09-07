import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

object Versions {
    // Document reading and generation dependencies
    const val apachePdfbox = "3.0.0"
    const val apachePoi = "5.2.3"

    // Test dependencies
    const val junitJupiter = "5.10.0"
    const val assertJ = "3.24.2"
    const val pdfCompare = "1.1.62"
}

repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm") version "1.9.10"
    id("java")
    id("maven-publish")
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
}

ktlint {
    this.version.set("0.45.2")
}

dependencies {
    // Document reading and generation dependencies
    implementation("org.apache.pdfbox:pdfbox:${Versions.apachePdfbox}")
    implementation("org.apache.poi:poi:${Versions.apachePoi}")
    implementation("org.apache.poi:poi-ooxml:${Versions.apachePoi}")

    // Test dependencies
    testImplementation("org.assertj:assertj-core:${Versions.assertJ}")
    testImplementation("de.redsix:pdfcompare:${Versions.pdfCompare}")
    testImplementation("org.junit.jupiter:junit-jupiter:${Versions.junitJupiter}")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
        }
    }

    withType<Test> {
        useJUnitPlatform()
    }
}
