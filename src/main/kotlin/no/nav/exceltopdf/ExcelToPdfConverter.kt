package no.nav.exceltopdf

import org.apache.pdfbox.pdmodel.PDDocument
import java.io.ByteArrayOutputStream

object ExcelToPdfConverter {
    /**
     * Accepts a ByteArray representation of an Excel file and returns a {@code ByteArray} representation of
     * the resulting PDF file.
     *
     * @param source
     *        A ByteArray representation of the Excel file that should be converted
     * @return A ByteArray representation of the resulting PDF file
     */
    fun convertExcelToPdf(
        source: ByteArray,
        fontSize: Short = 11,
        columnMargin: Float = 10f,
        pageMarginLeft: Float = 10f,
        pageMarginRight: Float = 10f,
        pageMarginTop: Float = 5f,
        pageMarginBottom: Float = 10f,
    ): ByteArray {
        try {
            val doc = PDDocument()
            val sheets = ExcelFileReader.getDataFromSource(source)

            sheets.forEach { sheetWrapper ->
                if (sheetWrapper.rows.isNotEmpty()) {
                    SheetToDocumentWriter(
                        sheetWrapper = sheetWrapper,
                        document = doc,
                        fontSize = fontSize,
                        columnMargin = columnMargin,
                        pageMarginLeft = pageMarginLeft,
                        pageMarginRight = pageMarginRight,
                        pageMarginTop = pageMarginTop,
                        pageMarginBottom = pageMarginBottom,
                    ).writeSheetToDocument()
                }
            }

            return ByteArrayOutputStream().run {
                doc.save(this)
                doc.close()
                toByteArray()
            }
        } catch (e: Exception) {
            throw ExcelConversionException("Conversion of Excel file failed", e)
        }
    }
}
