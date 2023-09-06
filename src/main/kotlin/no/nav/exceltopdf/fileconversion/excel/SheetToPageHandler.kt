package no.nav.exceltopdf.fileconversion.excel

import no.nav.exceltopdf.fileconversion.PdfPageSpec
import no.nav.exceltopdf.fileconversion.WritePdfPageOptions
import no.nav.exceltopdf.util.PdfFontUtil.widthInPoints
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType0Font
import java.io.ByteArrayInputStream
import kotlin.math.ceil

private data class Column(
    val cells: List<Cell>,
    val width: Float
)

internal class SheetToPageHandler(
    private val sheetWrapper: SheetWrapper,
    private val document: PDDocument,
    private val options: WritePdfPageOptions
) {
    private var currentPdfPageSpec = PdfPageSpec(currentXLocation = options.lineStartFromEdge)
    private var currentContentStream = PDPageContentStream(document, currentPdfPageSpec.page)

    private val pdFont = PDType0Font.load(document, ByteArrayInputStream(options.fontByteArray))

    fun writeSheetToDocument() {
        document.addPage(currentPdfPageSpec.page)
        val columns = orderRowsIntoColumns()
        val widthByColumn = calculateWidthByColumn(columns)
        val columnGroups = calculateColumnGroups(columns, widthByColumn)

        val groupedColumns = groupColumns(columnGroups, columns, widthByColumn)

        val rows = calculateRows(groupedColumns)

        rows.forEach { row ->
            val rowHeight = row[0].height
            if (currentPdfPageSpec.currentYLocation < rowHeight) {
                currentPdfPageSpec = addPage(rowHeight)
            }
            processRow(rowHeight, row)
        }
        currentContentStream.close()
    }

    private fun groupColumns(
        columnGroups: List<List<Int>>,
        columns: Map<Int, List<Cell>>,
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
    ): List<List<Cell>> {
        val maxPageWidth = currentPdfPageSpec.width
        val rows = mutableListOf<List<Cell>>()

        groupedColumns.forEach { columnGroup ->
            if (columnGroup.isNotEmpty()) {
                val maxRowSize = columnGroup.maxOfOrNull { it.cells.size } ?: 0
                for (rowNumber in 0 until maxRowSize) {
                    val row = mutableListOf<Cell>()
                    columnGroup.forEach { column ->
                        try {
                            val cell = column.cells[rowNumber]
                            val isTooWideForPage = cell.width > maxPageWidth

                            if (isTooWideForPage) {
                                splitIntoSeveralRows(cell, maxPageWidth).forEach {
                                    rows.add(it)
                                }
                            } else {
                                row.add(cell.copy(width = column.width))
                            }
                        } catch (e: Throwable) {
                            // Fail silently. Not all rows have the same amount of columns.
                        }
                    }
                    if (row.isNotEmpty()) {
                        rows.add(row)
                    }
                }
            }
        }
        return rows
    }

    private fun splitIntoSeveralRows(
        cell: Cell,
        maxPageWidth: Float
    ): List<List<Cell>> {
        val rowsNeededForData = ceil(cell.width / maxPageWidth).toInt()
        val charsPerRow = cell.data.length / rowsNeededForData
        val splitData = splitStringIntoEvenLengthSubstrings(cell.data, charsPerRow)
        return splitData.map {
            listOf(
                Cell(
                    data = it,
                    width = maxPageWidth,
                    height = cell.height,
                    columnIndex = cell.columnIndex
                )
            )
        }
    }

    private fun calculateWidthByColumn(columns: Map<Int, List<Cell>>): Map<Int, Float> {
        return columns.mapValues { (_, cells) ->
            cells.maxOfOrNull { it.width } ?: 0f
        }
    }

    private fun calculateColumnGroups(columns: Map<Int, List<Cell>>, widthByColumn: Map<Int, Float>): List<List<Int>> {
        val maxPageWidth = currentPdfPageSpec.width
        val columnsByPage = mutableListOf<MutableList<Int>>()
        columns.forEach { (columnIndex) ->
            val lastPage = columnsByPage.lastOrNull()
            if (lastPage == null) {
                columnsByPage.add(mutableListOf(columnIndex))
            } else {
                val margin = options.columnMargin
                val widthOfColumnsOnLastPage = lastPage.mapNotNull { widthByColumn[it]?.plus(margin) }.sum()
                val widthOfCurrentColumn = widthByColumn[columnIndex]!! + margin
                if (widthOfColumnsOnLastPage + widthOfCurrentColumn <= maxPageWidth) {
                    lastPage.add(columnIndex)
                } else {
                    columnsByPage.add(mutableListOf(columnIndex))
                }
            }
        }
        return columnsByPage
    }

    private fun orderRowsIntoColumns(): Map<Int, List<Cell>> {
        val columns = mutableMapOf<Int, MutableList<Cell>>()
        sheetWrapper.rows.forEach { cells ->
            cells.forEach {
                val columnIndex = it.columnIndex
                val cell = Cell(
                    data = it.data,
                    columnIndex = columnIndex,
                    width = pdFont.widthInPoints(it.data, options.fontSize),
                    height = it.height
                )
                if (columns[columnIndex] != null) {
                    columns[columnIndex]?.add(cell)
                } else {
                    columns[columnIndex] = mutableListOf(cell)
                }
            }
        }
        return columns
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

    private fun addPage(rowHeight: Float) = PdfPageSpec(currentXLocation = options.lineStartFromEdge).apply {
        currentYLocation -= rowHeight
        document.addPage(page)

        currentContentStream.close()
        currentContentStream = PDPageContentStream(document, page)
    }

    private fun processRow(heightInPoints: Float, cells: List<Cell>) {
        with(currentPdfPageSpec) {
            currentYLocation -= heightInPoints
            cells.forEach { processCell(it.data, it.width) }
            currentXLocation = options.lineStartFromEdge
        }
    }

    private fun processCell(data: String, cellWidth: Float) {
        val tx = currentPdfPageSpec.currentXLocation + options.columnMargin
        with(currentContentStream) {
            beginText()
            newLineAtOffset(tx, currentPdfPageSpec.currentYLocation)
            setFont(pdFont, options.fontSize.toFloat())
            showText(data)
            endText()
        }
        currentPdfPageSpec.currentXLocation = tx + cellWidth
    }
}
