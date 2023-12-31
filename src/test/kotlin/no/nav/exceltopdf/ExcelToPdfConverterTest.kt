package no.nav.exceltopdf

import de.redsix.pdfcompare.CompareResultImpl
import de.redsix.pdfcompare.PdfComparator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

class ExcelToPdfConverterTest {
    @ParameterizedTest(name = "should properly convert excel from input {0}")
    @ValueSource(
        strings = [
            "tiny.xlsx",
            "wide.xlsx",
            "long.xlsx",
            "tall-row.xlsx",
            "small-with-clipping-and-breaking.xlsx",
            "two-sheets.xlsx",
            "formulas.xlsx",
            "combined-cells-long.xlsx",
            "combined-cells-short.xlsx",
            "combined-cells-with-breaking.xlsx",
            "cell-with-error.xlsx",
            "image-inside-cell.xlsx",
            "image-over-cells.xlsx",
            "formats-and-styles.xlsx",
        ],
    )
    fun `should be able to convert Excel to PDF`(inputFileName: String, @TempDir tempDir: Path) {
        val expectedFileName = inputFileName.replace(".xlsx", ".pdf")
        val excelFileToConvert = getFile(inputFileName)
        val expectedPdf = getFile(expectedFileName)

        val actualPdfBytes = ExcelToPdfConverter.convertExcelToPdf(excelFileToConvert.readBytes())
        val filePath = tempDir.resolve("test.pdf")
        val actualPdf = Files.write(filePath, actualPdfBytes, StandardOpenOption.CREATE).toFile()

        val comparison = PdfComparator<CompareResultImpl>(expectedPdf, actualPdf).compare()
        if (comparison.isNotEqual) {
            comparison.writeTo("testresult-${inputFileName.replace(".xlsx", "")}")
        }
        assertThat(comparison.isEqual).isTrue()
    }

    private fun getFile(filename: String): File {
        val url = this.javaClass.classLoader.getResource("examplefiles/$filename")?.file
        return File(url!!)
    }
}
