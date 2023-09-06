package no.nav.exceltopdf.fileconversion.excel

import org.apache.commons.collections4.IteratorUtils
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.CellValue
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayInputStream
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import org.apache.poi.ss.usermodel.Cell as POICell
import org.apache.poi.ss.usermodel.Row as POIRow

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

    private fun processRows(rowIterator: Iterator<POIRow>, evaluator: XSSFFormulaEvaluator): List<RowWithoutWidth> {
        return IteratorUtils.toList(rowIterator)
            .map { processCells(it.cellIterator(), evaluator, (it as XSSFRow).heightInPoints) }
            .toList()
    }

    private fun processCells(cellIterator: Iterator<POICell>, evaluator: XSSFFormulaEvaluator, rowHeight: Float): RowWithoutWidth {
        return IteratorUtils.toList(cellIterator)
            .map { createCellWrapper(it as XSSFCell, evaluator, rowHeight) }
            .toList()
    }

    private fun createCellWrapper(cell: XSSFCell, evaluator: XSSFFormulaEvaluator, rowHeight: Float): CellWithoutWidth {
        val data = getDataFromCell(cell, evaluator)
        return CellWithoutWidth(
            data = data,
            columnIndex = cell.columnIndex,
            height = rowHeight
        )
    }

    private fun getDataFromCell(cell: POICell, evaluator: XSSFFormulaEvaluator): String {
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

    private fun processNumericCell(cell: POICell, cellValue: CellValue): String {
        val isDateCell = DateUtil.isADateFormat(cell.cellStyle.dataFormat.toInt(), cell.cellStyle.dataFormatString)

        return if (isDateCell) {
            val toLocalDate = cell.localDateTimeCellValue.toLocalDate()
            toLocalDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
        } else {
            cellValue.numberValue.toBigDecimal().toString()
        }
    }
}

data class CellWithoutWidth(
    val data: String,
    val columnIndex: Int,
    val height: Float
)

typealias RowWithoutWidth = List<CellWithoutWidth>

data class SheetWrapper(
    val rows: List<RowWithoutWidth>,
    val sheet: XSSFSheet
)

data class Cell(
    val data: String,
    val columnIndex: Int,
    val width: Float,
    val height: Float
)
