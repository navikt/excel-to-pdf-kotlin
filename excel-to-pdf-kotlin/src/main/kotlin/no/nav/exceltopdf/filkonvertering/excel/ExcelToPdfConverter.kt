package no.nav.exceltopdf.filkonvertering.excel

import no.nav.exceltopdf.filkonvertering.FilTilPdfConverter
import no.nav.exceltopdf.filkonvertering.WritePdfPageOptions
import no.nav.exceltopdf.filkonvertering.exception.ExcelKonverteringException
import org.apache.pdfbox.pdmodel.PDDocument
import java.io.ByteArrayOutputStream

object ExcelToPdfConverter : FilTilPdfConverter {
    override fun konverterTilPdf(source: ByteArray) = konverterTilPdfWithOptions(source, WritePdfPageOptions())

    fun konverterTilPdfWithOptions(source: ByteArray, options: WritePdfPageOptions): ByteArray {
        try {
            val doc = PDDocument()
            val sheets = ExcelFileHandler.hentDataFraSource(source)

            sheets.forEach { sheetWrapper ->
                if (sheetWrapper.rows.isNotEmpty()) {
                    SheetToPageHandler(sheetWrapper, doc, options).skrivSheetTilDokument()
                }
            }

            return ByteArrayOutputStream().run {
                doc.save(this)
                doc.close()
                toByteArray()
            }
        } catch (e: Exception) {
            throw ExcelKonverteringException("Konvertering av excel-fil feilet", e)
        }
    }
}
