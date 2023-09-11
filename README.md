excel-to-pdf-kotlin
================

Kotlin utility for transforming an Excel file (`.xlsx`) to PDF.

The goal of this library is to not lose any data, even if it comes at the cost of formatting. When exporting an Excel
file to PDF from Excel, the formatting is kept intact, meaning for example that the text in clipped cells are lost.

Excel sheets can also be wider than an A4 page. If an Excel sheet is too wide to fit on an A4 page, the library will first take as many columns as fit the width of a page,
finish writing all rows for those columns, then move to the
next columns. If a column is wider than an entire page the column will span several lines.

Given an Excel file that looks like this, let us assume that the first two columns take up almost the full width of an
A4 page.

| Month    | Budget | Spending | Sum  |
|----------|--------|----------|------|
| January  | 123    | 1        | 122  | 
| February | 456    | 2        | 454  |
| March    | 789    | 3        | 786  |
| April    | 1337   | 4        | 1333 |

The resulting PDF file would look something like this, where the lines denote the size of the A4 page.
Note that all rows from the first two columns are all printed before moving on to the next columns and their rows.

```
---------------------------
|  Month     Budget       |
|  January   123          |
|  February  456          |
|  March     789          |
|  April     1337         |
|  Spending  Sum          |
|  1         122          |
|  2         454          |
|  3         786          |
|  4         1333         |
---------------------------
```

See `src/test/resources/examplefiles` for examples with both input `.xlsx` files and output `.pdf` files.

## Shortcomings

* Only supports `.xlsx` files
* Images, formatting, colors, styles, fonts and font sizes are all ignored
* The sheet name and number is not included anywhere on the page
* Does **not** aim to support all functionalities of Excel. See `src/test/resources/examplefiles` for input and output files of all tested and supported cases.


# Installation

**NOTE:** Requires JDK 17

```
// TODO
```

# Usage

The only required parameter is `source` of type `ByteArray`:
```kotlin
val source: ByteArray = yourExcelFileAsByteArray
val pdfByteArray: ByteArray = ExcelToPdfConverter.convertExcelToPdf(source = source)
```

There are also several optional parameters you can pass to configure the PDF result:
```kotlin
val source: ByteArray = yourExcelFileAsByteArray
val pdfByteArray: ByteArray = ExcelToPdfConverter.convertExcelToPdf(
  source = source,
  fontSize = 11,
  columnMargin = 5f,
  pageMarginLeft = 10f,
  pageMarginRight = 10f,
  pageMarginTop = 10f,
  pageMarginBottom = 10f,
)
```

# Technologies:
* Kotlin
* JDK 17
* Gradle
* PDFBox
* Apache POI
* JUnit 5

# Tests

The tests are written in JUnit 5 and are run on every build on GitHub Actions, but can also be through IDEA or in the terminal using
```
./gradlew test
```

The tests use [pdfcompare](https://github.com/red6/pdfcompare) to compare input .xslx files to predefined .pdf files.
If a test fails a PDF document showing the differences are written to the project root with the file name following the
naming scheme `-testresult-<input_file_name>.pdf`.

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

Change IDEA autoformat configuration for this project:
* `./gradlew ktlintApplyToIdea`

Add pre-commit check/format hooks:
* `./gradlew addKtlintCheckGitPreCommitHook`
* `./gradlew addKtlintFormatGitPreCommitHook`
