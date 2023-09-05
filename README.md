[![Publish artifacts](https://github.com/navikt/excel-to-pdf-kotlin/actions/workflows/release.yml/badge.svg)](https://github.com/navikt/excel-to-pdf-kotlin/actions/workflows/release.yml)

excel-to-pdf-kotlin
================

Felles-komponenter for applikasjoner som tilhører teamdigisos.

---

## Henvendelser
Spørsmål knyttet til koden eller teamet kan stilles til teamdigisos@nav.no.

### For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #team_digisos.

# Innhold

Felles teknologi:
* Kotlin
* JDK 17
* Gradle

Krav:
* JDK 17

### excel-to-pdf-kotlin
Felles hjelpemetoder for logging og retry. 

Konvertering av utvalgte filtyper til PDF. Se egen README i pakken.

[PDF-konvertering](excel-to-pdf-kotlin/src/main/kotlin/no/nav/exceltopdf/pdf.md)

Teknologi:
* Coroutines

## Ktlint
Hvordan kjøre Ktlint:
* Fra IDEA: Kjør Gradle Task: excel-to-pdf-kotlin -> Tasks -> formatting -> ktlintFormat
* Fra terminal:
    * Kun formater: `./gradlew ktlintFormat`
    * Formater og bygg: `./gradlew ktlintFormat build`
    * Hvis IntelliJ begynner å hikke, kan en kjøre `./gradlew clean ktlintFormat build`

Endre IntelliJ autoformateringskonfigurasjon for dette prosjektet:
* `./gradlew ktlintApplyToIdea`

Legg til pre-commit check/format hooks:
* `./gradlew addKtlintCheckGitPreCommitHook`
* `./gradlew addKtlintFormatGitPreCommitHook`
