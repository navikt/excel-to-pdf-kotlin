import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

object Versions {
    // Test only
    const val junitJupiter = "5.9.2"
}

repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm") version "1.8.0"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
}

ktlint {
    this.version.set("0.45.2")
}

allprojects {
    group = "no.nav.exceltopdfkotlin"
    version = properties["version"] ?: "local-build"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    ktlint {
        this.version.set("0.45.2")
    }

    configurations {
        "testImplementation" {
            exclude(group = "junit", module = "junit")
        }
    }

    dependencies {
        implementation(kotlin("reflect"))

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
            testLogging {
                events("passed", "skipped", "failed")
            }
        }
    }
}
