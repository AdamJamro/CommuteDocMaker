package com.example.commutedocmaker.XLSX

import android.content.Context
import com.example.commutedocmaker.R
import com.example.commutedocmaker.dataSource.document.Document
import com.example.commutedocmaker.dataSource.document.Document.DocumentSummaryInformation
import com.example.commutedocmaker.dataSource.draftEntry.DraftEntry
import com.example.commutedocmaker.stringRes
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import kotlin.math.max

//enum class DraftFormField(
//    val fieldAsString: String = "",
//) {
//    BASE_ADDRESS(fieldAsString = R.string.base_address_string_representation.toString()),
//    DESTINATION_ADDRESS(fieldAsString = R.string.destination_address_string_representation.toString()),
//    DISTANCE_TRAVELLED(fieldAsString = R.string.distance_travelled_string_representation.toString()),
//    SHIFT_TIME_LENGTH(fieldAsString = R.string.shift_time_length_string_representation.toString()),
//    SHIFT_START_FULL_DATE(fieldAsString = R.string.shift_start_full_date_string_representation.toString())
//}

class XLSXConverter(private val context: Context) {
    private var documentSummary: DocumentSummaryInformation? = null

    fun convertToXLSX(draft: DraftEntry): Document? {
        val filepath: String = createFileWithUniquePath(draft.title)
        val title = filepath.lastIndexOf('/').let { index ->
            if (index == -1) filepath else filepath.substring(index + 1)
        }

        return if (generateDoc(filepath, draft)) {
            Document(
                title = title,
                path = filepath,
                date = LocalDate.now(),
                documentSummaryInformation = DocumentSummaryInformation(
                    dayRange = "A - B",
                    amountOfTransfers = 123,
                    payableAmount = 250.0f,
                    month = LocalDate.now().month
                )
            )
        } else {
            null
        }
    }

    private fun sanitizeFileName(title: String): String {
        val sanitized =  title.replace(Regex("[^a-zA-Z0-9.-]"), "_")
        return if (sanitized.endsWith(".xlsx")) sanitized else "$sanitized.xlsx"
    }

    private fun createFileWithUniquePath(title: String): String {
        val filesDir = context.filesDir
        var fileName: String = sanitizeFileName(title)
        var fileNumber = 1
        var file = File(context.filesDir, fileName)

        while (file.exists()) {
            val lastParenthesisIndex = fileName.lastIndexOf('(')
            val lastChar = fileName.last()
            if (lastChar == ')' && lastParenthesisIndex != -1) {
                val fileNumberString = fileName.substring(lastParenthesisIndex + 1, fileName.length - 1)
                fileName = fileName.substring(0, lastParenthesisIndex)
                try {
                    fileNumber = fileNumberString.toInt()
                    fileNumber++
                } catch (e: NumberFormatException) {
                    // it was not an int thus do nothing
                }
            }
            fileName = "${fileName}(${fileNumber})"
            fileNumber++

            file = File(filesDir, fileName)
        }

        file.createNewFile()
        return file.path
    }

    fun save() {
        // save to file
    }

    private fun generateDoc(filepath: String, draft: DraftEntry): Boolean {
        val workbook = XSSFWorkbook()
        val commuteSheet = workbook.createSheet(stringRes(context, R.string.routes_sheet_name))

        val headerRow = commuteSheet.createRow(0)
        for (header in CommuteSheetDataType.entries) {
            headerRow.createCell(header.ordinal)
                .setCellValue(stringRes(context, header.id))

            val approxMinWidth = (headerRow.getCell(header.ordinal).stringCellValue.length + 2) * 256
            commuteSheet.setColumnWidth(header.ordinal, max(approxMinWidth, header.columnWidth))
        }

        FileOutputStream(filepath).use {
            workbook.write(it)
        }

        workbook.close()

        return true
    }

    private enum class CommuteSheetDataType(val id: Int, val columnWidth: Int) {
        ENTRY_TAB(R.string.sheet_entry, 0),
        COMMUTE_REASON(R.string.sheet_commute_reason, 0),
        DISTANCE_TRAVELLED(R.string.sheet_distance_travelled, 0),
        EURO_PER_KM_RATE(R.string.sheet_euro_per_km_rate, 0),
        TOTAL_EURO(R.string.sheet_total_euro, 0),
        DEPRECATED(R.string.sheet_tab, 0),
        COMMUTE_TYPE(R.string.sheet_commute_type, 0),
        VEHICLE(R.string.sheet_vehicle, 0),
        VEHICLE_REGISTRATION(R.string.sheet_vehicle_registration, 0),
        DEPRECATED2(R.string.sheet_tab2, 0),
        DESCRIPTION(R.string.sheet_description, 0),
        FULL_ROUTE(R.string.sheet_full_route, 0),
        DEPARTURE_DATETIME(R.string.sheet_departure_datetime, 0),
        ARRIVAL_DATETIME(R.string.sheet_arrival_datetime, 0),
        DEPARTURE_COUNTRY(R.string.sheet_departure_country, 0),
        DEPRECATED3(R.string.sheet_tab3, 0),
        DEPRECATED4(R.string.sheet_tab4, 0),
        ARRIVAL_COUNTRY(R.string.sheet_arrival_country, 0),
    }
}
