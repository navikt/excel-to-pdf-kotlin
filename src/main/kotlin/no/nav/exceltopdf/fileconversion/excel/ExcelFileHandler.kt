package no.nav.exceltopdf.fileconversion.excel

import org.apache.commons.collections4.IteratorUtils
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayInputStream
import org.apache.poi.ss.usermodel.Cell as POICell
import org.apache.poi.ss.usermodel.Row as POIRow

internal object ExcelFileHandler {
    fun getDataFromSource(source: ByteArray): List<SheetWrapper> {
        val byteArrayInputStream = ByteArrayInputStream(source)
        val workbook = XSSFWorkbook(byteArrayInputStream)
        val evaluator = workbook.creationHelper.createFormulaEvaluator()

        return processSheets(workbook.sheetIterator(), evaluator)
    }

    private fun processSheets(sheetIterator: Iterator<Sheet>, evaluator: XSSFFormulaEvaluator): List<SheetWrapper> {
        return IteratorUtils.toList(sheetIterator)
            .map { SheetWrapper(processRows(it.rowIterator(), evaluator), it as XSSFSheet) }
            .toList()
    }

    private fun processRows(rowIterator: Iterator<POIRow>, evaluator: XSSFFormulaEvaluator): List<RowWrapper> {
        return IteratorUtils.toList(rowIterator)
            .map { RowWrapper(processCells(it.cellIterator(), evaluator)) }
            .toList()
    }

    private fun processCells(cellIterator: Iterator<POICell>, evaluator: XSSFFormulaEvaluator): List<CellWithoutWidth> {
        return IteratorUtils.toList(cellIterator)
            .map { createCellWrapper(it as XSSFCell, evaluator) }
            .toList()
    }

    private fun createCellWrapper(cell: XSSFCell, evaluator: XSSFFormulaEvaluator): CellWithoutWidth {
        val data = getDataFromCell(cell, evaluator)
        return CellWithoutWidth(
            data = data,
            columnIndex = cell.columnIndex,
        )
    }

    private fun getDataFromCell(cell: POICell, evaluator: XSSFFormulaEvaluator): String =
        runCatching { DataFormatter().formatCellValue(cell, evaluator) }.getOrElse { "Error" }
}

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
