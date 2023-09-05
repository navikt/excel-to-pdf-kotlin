package no.nav.exceltopdf.filkonvertering.excel

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
    fun hentDataFraSource(source: ByteArray): List<SheetWrapper> {

        val byteArrayInputStream = ByteArrayInputStream(source)
        val workbook = XSSFWorkbook(byteArrayInputStream)
        val evaluator = workbook.creationHelper.createFormulaEvaluator()

        return behandleSheets(workbook.sheetIterator(), evaluator)
    }

    private fun behandleSheets(sheetIterator: Iterator<Sheet>, evaluator: XSSFFormulaEvaluator): List<SheetWrapper> {
        return IteratorUtils.toList(sheetIterator)
            .map { SheetWrapper(behandleRows(it.rowIterator(), evaluator), it as XSSFSheet) }
            .toList()
    }

    private fun behandleRows(rowIterator: Iterator<Row>, evaluator: XSSFFormulaEvaluator): List<RowWrapper> {
        return IteratorUtils.toList(rowIterator)
            .map { RowWrapper(behandleCeller(it.cellIterator(), evaluator), it as XSSFRow) }
            .toList()
    }

    private fun behandleCeller(cellIterator: Iterator<Cell>, evaluator: XSSFFormulaEvaluator): List<CellWrapper> {
        return IteratorUtils.toList(cellIterator)
            .map { createCellWrapper(it as XSSFCell, evaluator) }
            .toList()
    }

    private fun createCellWrapper(cell: XSSFCell, evaluator: XSSFFormulaEvaluator): CellWrapper {
        return CellWrapper(
            hentDataFraCelle(cell, evaluator),
            cell
        )
    }

    private fun hentDataFraCelle(cell: Cell, evaluator: XSSFFormulaEvaluator): String {
        val cellValue = evaluator.evaluate(cell) ?: return ""
        return when (cellValue.cellType) {
            CellType.STRING -> cellValue.stringValue
            CellType.NUMERIC -> behandleNumeriskCelle(cell, cellValue)
            CellType.BOOLEAN -> cellValue.booleanValue.toString()
            CellType.BLANK -> ""
            CellType.FORMULA -> cell.cellFormula
            CellType.ERROR -> "error"
            else -> throw IllegalArgumentException("Unknown cell type")
        }
    }

    private fun behandleNumeriskCelle(cell: Cell, cellValue: CellValue): String {
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
