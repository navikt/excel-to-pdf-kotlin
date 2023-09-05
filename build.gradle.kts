import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

object Versions {
    const val coroutines = "1.6.4"
    const val slf4j = "1.7.36"
    const val junitJupiter = "5.9.2"
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

    implementation("org.apache.pdfbox:pdfbox:2.0.27")
    implementation("org.apache.poi:poi:5.2.3")
    implementation("org.apache.poi:poi-ooxml:5.2.3")
    implementation("org.apache.xmlgraphics:fop:2.8")
    implementation("org.docx4j:docx4j-JAXB-MOXy:11.4.9")
    implementation("org.docx4j:docx4j-JAXB-ReferenceImpl:11.4.9")
    implementation("org.docx4j:docx4j-export-fo:11.4.9")
    implementation("org.apache.commons:commons-csv:1.10.0")

    testImplementation("org.assertj:assertj-core:3.23.1")
    testImplementation("de.redsix:pdfcompare:1.1.61")
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
