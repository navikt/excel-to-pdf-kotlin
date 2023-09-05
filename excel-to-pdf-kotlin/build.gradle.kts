object Versions {
    const val coroutines = "1.6.4"
    const val slf4j = "1.7.36"
}

plugins {
    id("java")
    id("maven-publish")
}

dependencies {
//    Coroutines
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")

//    Slf4j
    api("org.slf4j:slf4j-api:${Versions.slf4j}")

    // convert to pdf
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
}

publishing {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/navikt/excel-to-pdf-kotlin")
            credentials {
                username = System.getenv("GITHUB_USERNAME")
                password = System.getenv("GITHUB_PASSWORD")
            }
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {

            pom {
                name.set("excel-to-pdf-kotlin")
                description.set("Bibliotek med util-funksjon for å konvertere Excel-fil (.xlsx) til PDF")
                url.set("https://github.com/navikt/excel-to-pdf-kotlin")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                scm {
                    connection.set("scm:git:https://github.com/navikt/excel-to-pdf-kotlin.git")
                    developerConnection.set("scm:git:https://github.com/navikt/excel-to-pdf-kotlin.git")
                    url.set("https://github.com/navikt/excel-to-pdf-kotlin")
                }
            }
            from(components["java"])
        }
    }
}
