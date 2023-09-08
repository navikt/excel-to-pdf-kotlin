package no.nav.exceltopdf.fileconversion.excel

import no.nav.exceltopdf.fileconversion.PdfPageSpec
import no.nav.exceltopdf.fileconversion.WritePdfPageOptions
import no.nav.exceltopdf.util.PdfFontUtil
import no.nav.exceltopdf.util.PdfFontUtil.widthInPoints
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType0Font
import java.io.ByteArrayInputStream

private data class Column(
    val cells: List<Cell>,
    val width: Float,
)

internal class SheetToPageHandler(
    private val sheetWrapper: SheetWrapper,
    private val document: PDDocument,
    private val options: WritePdfPageOptions,
) {
    private var currentPdfPageSpec = PdfPageSpec(
        currentXLocation = options.pageMarginLeft,
        currentYLocation = options.pageMarginTop,
    )
    private var currentContentStream = PDPageContentStream(document, currentPdfPageSpec.page)
    private val maxPageContentWidth = currentPdfPageSpec.width - options.pageMarginLeft - options.pageMarginRight
    private val maxPageContentHeight = currentPdfPageSpec.height - options.pageMarginTop - options.pageMarginBottom

    private val pdFont = PDType0Font.load(document, ByteArrayInputStream(PdfFontUtil.getDefaultFontBytes()))

    fun writeSheetToDocument() {
        document.addPage(currentPdfPageSpec.page)
        val columns = orderRowsIntoColumns()
        val widthByColumn = calculateWidthByColumn(columns)
        val columnGroups = calculateColumnGroups(columns, widthByColumn)

        val groupedColumns = groupColumns(columnGroups, columns, widthByColumn)

        val rows = calculateRows(groupedColumns)

        rows.forEach { row ->
            val rowHeight = row.firstOrNull()?.height ?: 0f
            if ((currentPdfPageSpec.currentYLocation + rowHeight) > maxPageContentHeight) {
                currentPdfPageSpec = addPage()
            }
            processRow(rowHeight, row)
        }
        currentContentStream.close()
    }

    private fun groupColumns(
        columnGroups: List<List<Int>>,
        columns: Map<Int, List<Cell>>,
        widthByColumn: Map<Int, Float>,
    ): List<List<Column>> {
        val groupedColumns = columnGroups.map { group ->
            group.mapNotNull { columnIndex ->
                columns[columnIndex]?.let {
                    Column(
                        cells = it,
                        width = widthByColumn[columnIndex]!!,
                    )
                }
            }
        }
        return groupedColumns
    }

    private fun calculateRows(
        groupedColumns: List<List<Column>>,
    ): List<List<Cell>> {
        val rows = mutableListOf<List<Cell>>()

        groupedColumns.forEach { columnGroup ->
            if (columnGroup.isNotEmpty()) {
                val maxRowSize = columnGroup.maxOfOrNull { it.cells.size } ?: 0
                for (rowNumber in 0 until maxRowSize) {
                    val row = mutableListOf<Cell>()
                    columnGroup.forEach { column ->
                        try {
                            val cell = column.cells[rowNumber]
                            val isTooWideForPage = cell.width > maxPageContentWidth

                            if (isTooWideForPage) {
                                rows.addAll(splitIntoSeveralRows(cell))
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

    private fun splitIntoSeveralRows(cell: Cell): List<List<Cell>> {
        val splitData = splitCellIntoLines(cell.data)
        return splitData.map {
            listOf(
                Cell(
                    data = it,
                    width = maxPageContentWidth,
                    height = cell.height,
                    columnIndex = cell.columnIndex,
                ),
            )
        }
    }

    private fun calculateWidthByColumn(columns: Map<Int, List<Cell>>): Map<Int, Float> {
        return columns.mapValues { (_, cells) ->
            cells.maxOfOrNull { it.width } ?: 0f
        }
    }

    private fun calculateColumnGroups(columns: Map<Int, List<Cell>>, widthByColumn: Map<Int, Float>): List<List<Int>> {
        val columnsByPage = mutableListOf<MutableList<Int>>()
        columns.forEach { (columnIndex) ->
            val lastPage = columnsByPage.lastOrNull()
            if (lastPage == null) {
                columnsByPage.add(mutableListOf(columnIndex))
            } else {
                val margin = options.columnMargin
                val widthOfColumnsOnLastPage = lastPage.mapNotNull { widthByColumn[it]?.plus(margin) }.sum()
                val widthOfCurrentColumn = widthByColumn[columnIndex]!! + margin
                if (widthOfColumnsOnLastPage + widthOfCurrentColumn <= maxPageContentWidth) {
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
        val longestRowLength = sheetWrapper.rows.maxOfOrNull { it.cells.size } ?: 0
        sheetWrapper.rows.forEach { row ->
            for (columnIndex in 0 until longestRowLength) {
                val cellWithoutWidth = row.cells.find { it.columnIndex == columnIndex }
                    ?: CellWithoutWidth(
                        data = "",
                        columnIndex = columnIndex,
                        height = row.height,
                    )
                val cell = Cell(
                    data = cellWithoutWidth.data,
                    columnIndex = columnIndex,
                    width = widthInPoints(cellWithoutWidth.data),
                    height = cellWithoutWidth.height,
                )
                if (columns[columnIndex] != null) {
                    columns[columnIndex]?.add(cell)
                } else {
                    columns[columnIndex] = mutableListOf(cell)
                }
            }
        }
        return columns.toSortedMap()
    }

    private fun widthInPoints(text: String): Float = pdFont.widthInPoints(text, currentPdfPageSpec.fontSize)

    private fun splitToLinesNoWiderThanPageWidth(input: List<String>, addSpaceBetweenEntriesInLine: Boolean = true): List<String> {
        val result = mutableListOf<String>()
        var line = ""
        var lineWidth = 0f

        for (lol in input) {
            val text = lol.let {
                if (addSpaceBetweenEntriesInLine && line.isNotEmpty()) " $it" else it
            }
            val wordWidth = widthInPoints(text)
            val lineWidthWithCurrentWord = lineWidth + wordWidth
            if (wordWidth > maxPageContentWidth) {
                result.addAll(splitWordIntoLinesNoWiderThanPageWidth(text))
            } else if (lineWidthWithCurrentWord <= maxPageContentWidth) {
                line += text
                lineWidth = lineWidthWithCurrentWord
            } else {
                result.add(line)
                line = text
                lineWidth = wordWidth
            }
        }
        if (line.isNotEmpty() && line.isNotBlank()) {
            result.add(line)
        }
        return result
    }

    private fun splitWordIntoLinesNoWiderThanPageWidth(input: String): List<String> {
        val characters = input.split("")

        return splitToLinesNoWiderThanPageWidth(characters, false)
    }

    private fun splitCellIntoLines(input: String): List<String> {
        val words = input.split("\\s+".toRegex())
        return splitToLinesNoWiderThanPageWidth(words)
    }

    private fun addPage() = PdfPageSpec(
        currentXLocation = options.pageMarginLeft,
        currentYLocation = options.pageMarginTop,
    ).apply {
        document.addPage(page)

        currentContentStream.close()
        currentContentStream = PDPageContentStream(document, page)
    }

    private fun processRow(heightInPoints: Float, cells: List<Cell>) {
        with(currentPdfPageSpec) {
            currentYLocation += heightInPoints
            cells.forEach { processCell(it.data, it.width) }
            currentXLocation = options.pageMarginLeft
        }
    }

    private fun processCell(data: String, cellWidth: Float) {
        val tx = currentPdfPageSpec.currentXLocation + options.columnMargin
        with(currentContentStream) {
            beginText()
            newLineAtOffset(tx, currentPdfPageSpec.height - currentPdfPageSpec.currentYLocation)
            setFont(pdFont, currentPdfPageSpec.fontSize.toFloat())
            showText(data)
            endText()
        }
        currentPdfPageSpec.currentXLocation = tx + cellWidth
    }
}
