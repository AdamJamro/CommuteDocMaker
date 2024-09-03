package com.example.commutedocmaker.dataSource.draftEntry

import android.content.Context
import com.example.commutedocmaker.R
import com.example.commutedocmaker.stringRes
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

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
    fun convertToXLSX(entry: DraftEntry): String? {

        val filepath: String = createFileWithUniquePath(entry.title)
        generateDoc(entry, filepath)


        return null
    }


    private fun createFileWithUniquePath(title: String): String {
        val filesDir = context.filesDir
        var fileName: String = title
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
        return file.path
    }

    fun save() {
        // save to file
    }

    private fun generateDoc(entry: DraftEntry, filepath: String): String {
        val workbook = XSSFWorkbook()
        val commuteSheet = workbook.createSheet(stringRes(context, R.string.routes_sheet_name))

        val headerRow = commuteSheet.createRow(0)
        for (header in CommuteSheetDataType.entries) {
            headerRow.createCell(header.ordinal)
                .setCellValue(stringRes(context, header.id))
        }
        headerRow.createCell(0)
            .setCellValue(stringRes(context, R.string.base_address_string_representation))

        for (i in 0..CommuteSheetDataType.entries.size){
            commuteSheet.autoSizeColumn(i)
        }

        FileOutputStream(filepath).use {
            workbook.write(it)
        }

        workbook.close()

        return "abc"
    }

    private enum class CommuteSheetDataType(val id: Int) {
        ENTRY_TAB(R.string.sheet_entry),
        COMMUTE_REASON(R.string.sheet_commute_reason),
        DISTANCE_TRAVELLED(R.string.sheet_distance_travelled),
        EURO_PER_KM_RATE(R.string.sheet_euro_per_km_rate),
        TOTAL_EURO(R.string.sheet_total_euro),
        DEPRECATED(R.string.sheet_tab),
        COMMUTE_TYPE(R.string.sheet_commute_type),
        VEHICLE(R.string.sheet_vehicle),
        VEHICLE_REGISTRATION(R.string.sheet_vehicle_registration),
        DEPRECATED2(R.string.sheet_tab2),
        DESCRIPTION(R.string.sheet_description),
        FULL_ROUTE(R.string.sheet_full_route),
        DEPARTURE_DATETIME(R.string.sheet_departure_datetime),
        ARRIVAL_DATETIME(R.string.sheet_arrival_datetime),
        DEPARTURE_COUNTRY(R.string.sheet_departure_country),
        DEPRECATED3(R.string.sheet_tab3),
        DEPRECATED4(R.string.sheet_tab4),
        ARRIVAL_COUNTRY(R.string.sheet_arrival_country),
    }
}
