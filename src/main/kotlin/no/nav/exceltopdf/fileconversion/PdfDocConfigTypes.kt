package no.nav.exceltopdf.fileconversion

import no.nav.exceltopdf.util.PdfFontUtil.getDefaultFontBytes
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.common.PDRectangle

class WritePdfPageOptions(
    var fontByteArray: ByteArray = getDefaultFontBytes(),
    var fontSize: Short = 11,
    val lineStartFromEdge: Float = 10f,
    val columnMargin: Float = 10f,
    val rowMargin: Float = 3f,
)

data class PdfPageSpec(
    val page: PDPage = PDPage(PDRectangle.A4),
    val width: Float = page.cropBox.width,
    var currentXLocation: Float,
    var currentYLocation: Float = page.cropBox.height,
)
