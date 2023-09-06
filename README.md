[![Publish artifacts](https://github.com/navikt/excel-to-pdf-kotlin/actions/workflows/release.yml/badge.svg)](https://github.com/navikt/excel-to-pdf-kotlin/actions/workflows/release.yml)

excel-to-pdf-kotlin
================

Kotlin utility for transforming an Excel file (`.xlsx`) to PDF.

If an Excel sheet is too wide to fit on an A4 page, it
will first take as many columns as fit the width of a page, finish writing all rows for those columns, then move to the
next columns. If a column is wider than an entire page the column will span several lines.

See `src/test/resources/examplefiles` for conversion examples.

# Installation

**NOTE:** Requires JDK 17

```
// TODO
```

# Usage

```
val data: byte[] = yourExcelFileAsByteArray
val pdfByteArray: byte[] = ExcelToPdfConverter.convertExcelToPdf(data)
```

# Shortcomings

* Only supports `.xlsx` files
* Images in the Excel files are ignored and left out
* The sheet name or number is not included anywhere on the page of the resulting PDF.

# Technologies:
* Kotlin
* Coroutines
* JDK 17
* Gradle
* PDFBox
* Apache POI
* JUnit 5

# Tests

The tests are written in JUnit 5 and are run on every build on Github Actions, but can also be through IDEA or in the terminal using
```
./gradlew test
```

The tests use [pdfcompare](https://github.com/red6/pdfcompare) to compare input .xslx files to predefined .pdf files.
If a test fails a PDF document showing the differences are written to the project root with the file name following the
naming scheme `<test_name>-pdf-convert-test-result.pdf`.

Adding a new test case is really simple. When adding new functionality you should consider adding another test case:
1. Add an input `.xslx` file and an expected output `.pdf` file with the same name to `src/test/resources/examplefiles`
2. Add the name of the `.xslx` file to the list of the parameterized test in `ExcelToPdfConverterTest.kt`

# Ktlint
How to run Ktlint:
* From IDEA: Run Gradle Task: excel-to-pdf-kotlin -> Tasks -> formatting -> ktlintFormat
* From terminal:
    * Format only: `./gradlew ktlintFormat`
    * Format and build: `./gradlew ktlintFormat build`
* Troubleshooting if IDEA starts complaining `./gradlew clean ktlintFormat build`

Change IDEA autoformat configuratino for this project:
* `./gradlew ktlintApplyToIdea`

Add pre-commit check/format hooks:
* `./gradlew addKtlintCheckGitPreCommitHook`
* `./gradlew addKtlintFormatGitPreCommitHook`
