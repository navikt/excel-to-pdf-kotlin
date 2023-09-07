import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

object Versions {
    const val coroutines = "1.7.3"
    const val slf4j = "2.0.9"
    const val junitJupiter = "5.10.0"
}

repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm") version "1.8.0"
    id("java")
    id("maven-publish")
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
}

ktlint {
    this.version.set("0.45.2")
}

dependencies {
    implementation(kotlin("reflect"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
    api("org.slf4j:slf4j-api:${Versions.slf4j}")

    implementation("org.apache.pdfbox:pdfbox:3.0.0")
    implementation("org.apache.poi:poi:5.2.3")
    implementation("org.apache.poi:poi-ooxml:5.2.3")

    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("de.redsix:pdfcompare:1.1.62")
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
