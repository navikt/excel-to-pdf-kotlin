package no.nav.exceltopdf.fileconversion

import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.common.PDRectangle

class WritePdfPageOptions(
    val lineStartFromEdge: Float = 10f,
    val columnMargin: Float = 10f,
)

data class PdfPageSpec(
    val page: PDPage = PDPage(PDRectangle.A4),
    val width: Float = page.cropBox.width,
    var fontSize: Short = 11,
    var currentXLocation: Float,
    var currentYLocation: Float = page.cropBox.height,
)
