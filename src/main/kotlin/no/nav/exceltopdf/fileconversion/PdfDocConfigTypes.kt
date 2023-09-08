package no.nav.exceltopdf.fileconversion

import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.common.PDRectangle

class WritePdfPageOptions(
    val pageMarginLeft: Float = 10f,
    val pageMarginRight: Float = 10f,
    val pageMarginTop: Float = 5f,
    val pageMarginBottom: Float = 10f,
    val columnMargin: Float = 10f,
)

data class PdfPageSpec(
    val page: PDPage = PDPage(PDRectangle.A4),
    val width: Float = page.cropBox.width,
    val height: Float = page.cropBox.height,
    var fontSize: Short = 11,
    var currentXLocation: Float,
    var currentYLocation: Float,
)
