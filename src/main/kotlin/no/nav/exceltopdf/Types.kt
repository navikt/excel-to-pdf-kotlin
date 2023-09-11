package no.nav.exceltopdf

import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.poi.xssf.usermodel.XSSFSheet

class WritePdfPageOptions(
    val pageMarginLeft: Float = 10f,
    val pageMarginRight: Float = 10f,
    val pageMarginTop: Float = 5f,
    val pageMarginBottom: Float = 10f,
    val columnMargin: Float = 10f,
    val fontSize: Short = 11,
)

internal data class PdfPageSpec(
    val page: PDPage = PDPage(PDRectangle.A4),
    val width: Float = page.cropBox.width,
    val height: Float = page.cropBox.height,
    var currentXLocation: Float,
    var currentYLocation: Float,
)

internal data class CellWithoutWidth(
    val data: String,
    val columnIndex: Int,
)

internal data class RowWrapper(
    val cells: List<CellWithoutWidth>,
)

internal data class SheetWrapper(
    val rows: List<RowWrapper>,
    val sheet: XSSFSheet,
)

internal data class Cell(
    val data: String,
    val columnIndex: Int,
    val width: Float,
    val height: Float,
)
