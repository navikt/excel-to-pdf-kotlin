package no.nav.exceltopdf.fileconversion.excel

import no.nav.exceltopdf.fileconversion.WritePdfPageOptions
import no.nav.exceltopdf.fileconversion.exception.ExcelConversionException
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
    fun convertExcelToPdf(source: ByteArray, options: WritePdfPageOptions = WritePdfPageOptions()): ByteArray {
        try {
            val doc = PDDocument()
            val sheets = ExcelFileHandler.getDataFromSource(source)

            sheets.forEach { sheetWrapper ->
                if (sheetWrapper.rows.isNotEmpty()) {
                    SheetToPageHandler(sheetWrapper, doc, options).writeSheetToDocument()
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
