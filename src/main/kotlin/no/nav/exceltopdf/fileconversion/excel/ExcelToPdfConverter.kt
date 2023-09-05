package no.nav.exceltopdf.fileconversion.excel

import no.nav.exceltopdf.fileconversion.FileToPdfConverter
import no.nav.exceltopdf.fileconversion.WritePdfPageOptions
import no.nav.exceltopdf.fileconversion.exception.ExcelConversionException
import org.apache.pdfbox.pdmodel.PDDocument
import java.io.ByteArrayOutputStream

object ExcelToPdfConverter : FileToPdfConverter {
    override fun convertExcelToPdf(source: ByteArray) = convertExcelToPdfWithOptions(source, WritePdfPageOptions())

    fun convertExcelToPdfWithOptions(source: ByteArray, options: WritePdfPageOptions): ByteArray {
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
