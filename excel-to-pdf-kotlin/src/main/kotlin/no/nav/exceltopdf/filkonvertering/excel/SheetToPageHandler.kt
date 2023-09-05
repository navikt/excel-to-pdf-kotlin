package no.nav.exceltopdf.filkonvertering.excel

import no.nav.exceltopdf.filkonvertering.PdfPageSpec
import no.nav.exceltopdf.filkonvertering.WritePdfPageOptions
import no.nav.exceltopdf.util.PdfFontUtil.breddeIPunkter
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType0Font
import java.io.ByteArrayInputStream
import kotlin.math.ceil

private data class Column(
    val cells: List<CellWrapperWrapper>,
    val width: Float
)

internal class SheetToPageHandler(
    private val sheetWrapper: SheetWrapper,
    private val dokument: PDDocument,
    private val options: WritePdfPageOptions
) {
    private var currentPdfPageSpec = PdfPageSpec(currentXLocation = options.lineStartFromEdge)
    private var currentContentStream = PDPageContentStream(dokument, currentPdfPageSpec.page)

    private val pdFont = PDType0Font.load(dokument, ByteArrayInputStream(options.fontByteArray))

    fun skrivSheetTilDokument() {
        dokument.addPage(currentPdfPageSpec.page)
        val columns = orderRowsIntoColumns()
        val widthByColumn = calculateWidthByColumn(columns)
        val columnGroups = calculateColumnGroups(columns, widthByColumn)

        val groupedColumns = groupColumns(columnGroups, columns, widthByColumn)

        val rows = calculateRows(groupedColumns)

        rows.forEach { row ->
            val rowHeight = row[0].cellHeight
            if (currentPdfPageSpec.currentYLocation < rowHeight) {
                currentPdfPageSpec = leggTilSide(rowHeight)
            }
            behandleRad(rowHeight, row)
        }
        currentContentStream.close()
    }

    private fun groupColumns(
        columnGroups: List<List<Int>>,
        columns: Map<Int, List<CellWrapperWrapper>>,
        widthByColumn: Map<Int, Float>
    ): List<List<Column>> {
        val groupedColumns = columnGroups.map { group ->
            group.mapNotNull { columnIndex ->
                columns[columnIndex]?.let {
                    Column(
                        cells = it,
                        width = widthByColumn[columnIndex]!!
                    )
                }
            }
        }
        return groupedColumns
    }

    private fun calculateRows(
        groupedColumns: List<List<Column>>
    ): List<List<CellWrapperWrapper>> {
        val maxPageWidth = currentPdfPageSpec.width
        val rader = mutableListOf<List<CellWrapperWrapper>>()

        groupedColumns.forEach { columnGroup ->
            if (columnGroup.isNotEmpty()) {
                val maxRowSize = columnGroup.maxOfOrNull { it.cells.size } ?: 0
                for (rowNumber in 0 until maxRowSize) {
                    val rad = mutableListOf<CellWrapperWrapper>()
                    columnGroup.forEach { column ->
                        try {
                            val celle = column.cells[rowNumber]
                            val isTooWideForPage = celle.cellWidth > maxPageWidth

                            if (isTooWideForPage) {
                                splitIntoSeveralRows(celle, maxPageWidth).forEach {
                                    rader.add(it)
                                }
                            } else {
                                rad.add(celle.copy(cellWidth = column.width))
                            }
                        } catch (e: Throwable) {
                            // Fail silently. Not all rows have the same amount of columns.
                        }
                    }
                    if (rad.isNotEmpty()) {
                        rader.add(rad)
                    }
                }
            }
        }
        return rader
    }

    private fun splitIntoSeveralRows(
        celle: CellWrapperWrapper,
        maxPageWidth: Float
    ): List<List<CellWrapperWrapper>> {
        val antallRaderSomTrengsForCelle = ceil(celle.cellWidth / maxPageWidth).toInt()
        val antallCharsPerRad = celle.cell.data.length / antallRaderSomTrengsForCelle
        val splittetData = splitStringIntoEvenLengthSubstrings(celle.cell.data, antallCharsPerRad)
        return splittetData.map {
            listOf(
                CellWrapperWrapper(
                    cellWidth = maxPageWidth,
                    cellHeight = celle.cellHeight,
                    cell = CellWrapper(data = it, cell = celle.cell.cell)
                )
            )
        }
    }

    private fun calculateWidthByColumn(columns: Map<Int, List<CellWrapperWrapper>>): Map<Int, Float> {
        return columns.mapValues { (_, celler) ->
            val storsteBredde = celler.maxOfOrNull { it.cellWidth } ?: 0f
            storsteBredde
        }
    }

    private fun calculateColumnGroups(columns: Map<Int, List<CellWrapperWrapper>>, widthByColumn: Map<Int, Float>): List<List<Int>> {
        val maxPageWidth = currentPdfPageSpec.width
        val kolonnerBySide = mutableListOf<MutableList<Int>>()
        columns.forEach { (kolonneIndex) ->
            val sisteSide = kolonnerBySide.lastOrNull()
            if (sisteSide == null) {
                kolonnerBySide.add(mutableListOf(kolonneIndex))
            } else {
                val kolonneNumreSisteSide = sisteSide
                val margin = options.columnMargin
                val breddeKolonnerSisteSide = kolonneNumreSisteSide.mapNotNull { widthByColumn[it]?.plus(margin) }.sum()
                val breddeCurrentKolonne = widthByColumn[kolonneIndex]!! + margin
                if (breddeKolonnerSisteSide + breddeCurrentKolonne > maxPageWidth) {
                    kolonnerBySide.add(mutableListOf(kolonneIndex))
                } else {
                    sisteSide.add(kolonneIndex)
                }
            }
        }
        return kolonnerBySide
    }

    private fun orderRowsIntoColumns(): Map<Int, List<CellWrapperWrapper>> {
        val kolonner = mutableMapOf<Int, MutableList<CellWrapperWrapper>>()
        sheetWrapper.rows.forEach { rowWrapper ->
            val rowHeight = rowWrapper.row.heightInPoints
            rowWrapper.cells.forEach {
                val kolonnenummer = it.cell.columnIndex
                val cellWrapperWrapper = CellWrapperWrapper(
                    cell = it,
                    cellWidth = pdFont.breddeIPunkter(it.data, options.fontSize),
                    cellHeight = rowHeight
                )
                if (kolonner[kolonnenummer] != null) {
                    kolonner[kolonnenummer]?.add(cellWrapperWrapper)
                } else {
                    kolonner[kolonnenummer] = mutableListOf(cellWrapperWrapper)
                }
            }
        }
        return kolonner
    }

    private fun splitStringIntoEvenLengthSubstrings(input: String, length: Int): List<String> {
        val result = mutableListOf<String>()
        var currentIndex = 0

        while (currentIndex < input.length) {
            val endIndex = currentIndex + length
            if (endIndex <= input.length) {
                val substring = input.substring(currentIndex, endIndex)
                result.add(substring)
            }
            currentIndex = endIndex
        }

        return result
    }

    private fun leggTilSide(rowHeight: Float) = PdfPageSpec(currentXLocation = options.lineStartFromEdge).apply {
        currentYLocation -= rowHeight
        dokument.addPage(page)

        currentContentStream.close()
        currentContentStream = PDPageContentStream(dokument, page)
    }

    private fun behandleRad(heightInPoints: Float, cells: List<CellWrapperWrapper>) {
        with(currentPdfPageSpec) {
            currentYLocation -= heightInPoints
            cells.forEach { behandleCelle(it.cell, it.cellWidth) }
            currentXLocation = options.lineStartFromEdge
        }
    }

    private fun behandleCelle(cellWrapper: CellWrapper, cellWidth: Float) {
        val tx = currentPdfPageSpec.currentXLocation + options.columnMargin
        with(currentContentStream) {
            beginText()
            newLineAtOffset(tx, currentPdfPageSpec.currentYLocation)
            setFont(pdFont, options.fontSize.toFloat())
            showText(cellWrapper.data)
            endText()
        }
        currentPdfPageSpec.currentXLocation = tx + cellWidth
    }
}
