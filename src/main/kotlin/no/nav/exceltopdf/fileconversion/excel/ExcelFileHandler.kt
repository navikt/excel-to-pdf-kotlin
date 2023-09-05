package no.nav.exceltopdf.fileconversion.excel

import org.apache.commons.collections4.IteratorUtils
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.CellValue
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayInputStream
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

object ExcelFileHandler {
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

    private fun processRows(rowIterator: Iterator<Row>, evaluator: XSSFFormulaEvaluator): List<RowWrapper> {
        return IteratorUtils.toList(rowIterator)
            .map { RowWrapper(processCells(it.cellIterator(), evaluator), it as XSSFRow) }
            .toList()
    }

    private fun processCells(cellIterator: Iterator<Cell>, evaluator: XSSFFormulaEvaluator): List<CellWrapper> {
        return IteratorUtils.toList(cellIterator)
            .map { createCellWrapper(it as XSSFCell, evaluator) }
            .toList()
    }

    private fun createCellWrapper(cell: XSSFCell, evaluator: XSSFFormulaEvaluator): CellWrapper {
        return CellWrapper(
            getDataFromCell(cell, evaluator),
            cell
        )
    }

    private fun getDataFromCell(cell: Cell, evaluator: XSSFFormulaEvaluator): String {
        val cellValue = evaluator.evaluate(cell) ?: return ""
        return when (cellValue.cellType) {
            CellType.STRING -> cellValue.stringValue
            CellType.NUMERIC -> processNumericCell(cell, cellValue)
            CellType.BOOLEAN -> cellValue.booleanValue.toString()
            CellType.BLANK -> ""
            CellType.FORMULA -> cell.cellFormula
            CellType.ERROR -> "error"
            else -> throw IllegalArgumentException("Unknown cell type")
        }
    }

    private fun processNumericCell(cell: Cell, cellValue: CellValue): String {
        val isDateCell = DateUtil.isADateFormat(cell.cellStyle.dataFormat.toInt(), cell.cellStyle.dataFormatString)

        return if (isDateCell) {
            val toLocalDate = cell.localDateTimeCellValue.toLocalDate()
            toLocalDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
        } else {
            cellValue.numberValue.toBigDecimal().toString()
        }
    }
}

data class SheetWrapper(
    val rows: List<RowWrapper>,
    val sheet: XSSFSheet
)

data class RowWrapper(
    val cells: List<CellWrapper>,
    val row: XSSFRow
)
data class CellWrapper(
    val data: String,
    val cell: XSSFCell
)
data class CellWrapperWrapper(
    val cell: CellWrapper,
    val cellWidth: Float,
    val cellHeight: Float
)
