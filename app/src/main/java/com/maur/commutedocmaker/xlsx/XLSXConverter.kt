package com.maur.commutedocmaker.xlsx

import android.content.Context
import android.widget.Toast
import com.maur.commutedocmaker.R
import com.maur.commutedocmaker.dataSource.autoDetails.AutoDetails
import com.maur.commutedocmaker.dataSource.autoDetails.Details
import com.maur.commutedocmaker.dataSource.document.Document
import com.maur.commutedocmaker.dataSource.document.Document.DocumentSummaryInformation
import com.maur.commutedocmaker.dataSource.draftEntry.DraftEntry
import com.maur.commutedocmaker.stringRes
import com.maur.commutedocmaker.ui.views.convertMinutesToStringTime
import com.maur.commutedocmaker.ui.views.convertStringTimeToQuarters
import com.maur.commutedocmaker.xlsx.XLSXConverter.CommuteSheetDataType
import com.maur.commutedocmaker.xlsx.XLSXConverter.CommuteSheetDataType.*
import com.maur.commutedocmaker.xlsx.XLSXConverter.CommuteSheetDataType.Companion.addressData
import com.maur.commutedocmaker.xlsx.XLSXConverter.CommuteSheetDataType.Companion.floatPreciseData
import com.maur.commutedocmaker.xlsx.XLSXConverter.CommuteSheetDataType.Companion.primaryData
import com.maur.commutedocmaker.xlsx.XLSXConverter.CommuteSheetDataType.Companion.secondaryData
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.roundToInt

//enum class DraftFormField(
//    val fieldAsString: String = "",
//) {
//    BASE_ADDRESS(fieldAsString = R.string.base_address_string_representation.toString()),
//    DESTINATION_ADDRESS(fieldAsString = R.string.destination_address_string_representation.toString()),
//    DISTANCE_TRAVELLED(fieldAsString = R.string.distance_travelled_string_representation.toString()),
//    SHIFT_TIME_LENGTH(fieldAsString = R.string.shift_time_length_string_representation.toString()),
//    SHIFT_START_FULL_DATE(fieldAsString = R.string.shift_start_full_date_string_representation.toString())
//}
private typealias RowData = Map<CommuteSheetDataType, String>

fun sanitizeFileName(title: String): String {
    val sanitized =  title
        .replace(Regex("(?<=\\s)\\s|^\\s"), "")
        .replace(Regex("[^a-zA-Z0-9.()ĄąĆćĘęŁłŃńÓóŚśŹźŻż \\-]"), "_")
    return sanitized
}

class XLSXConverter(private val context: Context) {
    private var documentSummary: DocumentSummaryInformation = DocumentSummaryInformation(
        dayRange = "",
        amountOfTransfers = 0,
        payableAmount = 0f,
        month = LocalDate.now().month
    )
    private val workbook = XSSFWorkbook()

    fun convertToXLSX(draft: DraftEntry, autoDetails: AutoDetails): Document? {
        val filepath: String = createFileWithUniquePath(draft.title)
        val title = filepath.lastIndexOf('/').let { index ->
            if (index == -1) filepath else filepath.substring(index + 1)
        }

        return if (generateDoc(filepath, draft, autoDetails)) {
            Document(
                title = title,
                path = filepath,
                date = LocalDate.now(),
                documentSummaryInformation = documentSummary
            )
        } else {
            null
        }
    }



    private fun createFileWithUniquePath(title: String): String {
        val filesDir = context.filesDir
        var fileName: String = sanitizeFileName(title)
        var fileNumber = 1
        fileName = fileName
            .removeSuffix(".xlsx")
            .let { "$it.xlsx" }
        var file = File(context.filesDir, fileName)

        while (file.exists()) {
            val lastParenthesisIndex = fileName.lastIndexOf('(')
            val lastCharIndex = fileName.length - ".xlsx".length - 1
            val lastChar = fileName[lastCharIndex]
            if (lastChar == ')' && lastParenthesisIndex != -1) {
                val fileNumberString = fileName.substring(lastParenthesisIndex + 1, lastCharIndex)
                fileNumberString.toIntOrNull()?.also {
                    fileNumber = it
                    fileNumber++

                    fileName = fileName.substring(0, lastParenthesisIndex)
                }
            }
            if (fileName.length > lastCharIndex) {
                fileName = fileName.substring(0, lastCharIndex + 1)
            }
            fileName = "${fileName}(${fileNumber}).xlsx"
            fileNumber++

            file = File(filesDir, fileName)
        }

        file.createNewFile()
        return file.path
    }

    fun save() {
        // save to file
    }

    private fun fillDetailsSheet(dataSheet: XSSFSheet, autoDetails: AutoDetails): Boolean {
        val details = autoDetails.details
        if (details.size < Details.entries.size){
            Toast.makeText(context, "Internal Error.\nData Auto Details Data has been corrupted", Toast.LENGTH_LONG).show()
            return false
        }
        dataSheet.apply{
            for(detail in Details.entries) {
                with(createRow(detail.ordinal)) {
                    createCell(0).setCellValue(stringRes(context, detail.id))
                    createCell(1).setCellValue(details[detail.ordinal])
                }
            }
        }
        return true
    }

    private fun generateDoc(filepath: String, draft: DraftEntry, autoDetails: AutoDetails): Boolean {

        val commuteSheet = workbook.createSheet(stringRes(context, R.string.routes_sheet_name))
        val autoDetailsSheet = workbook.createSheet(stringRes(context, R.string.auto_details_sheet_name))
        fillDetailsSheet(autoDetailsSheet, autoDetails)


        val headerRow = commuteSheet.createRow(0)
        for (header in CommuteSheetDataType.entries) {
            headerRow.createCell(header.ordinal).apply {
                setCellValue(stringRes(context, header.id))
                cellStyle = cellStyles.defaultHeaderData
            }


            val approxMinWidth = 1 + max(
                headerRow.getCell(header.ordinal).stringCellValue.length,
                header.tag.length
            )
            commuteSheet.setColumnWidth(header.ordinal, max(approxMinWidth, header.minColumnWidth) * 256)
        }

        val rows: MutableList<RowData> = mutableListOf()
        for (dataPatch in draft.draftDataPatches) {
            dataPatch.apply {
                commuteSheet.apply {
                    setColumnWidth(
                        /*columnIndex*/FULL_ROUTE.ordinal,
                        /*width*/max(getColumnWidth(FULL_ROUTE.ordinal), "$baseAddress - $destinationAddress".length))
                }

                val precision = 100L
                val prototypeRowData : RowData = mapOf(
                    ENTRY_TAB to ENTRY_TAB.tag,
                    COMMUTE_REASON to COMMUTE_REASON.tag,
                    DISTANCE_TRAVELLED to distanceTravelled.toString().replace('.', ','),
                    EURO_PER_KM_RATE to cashPerKilometer.toString().replace('.', ','),
                    TOTAL_EURO to
                            DecimalFormat(
                                "#0.##",
                                DecimalFormatSymbols(java.util.Locale.getDefault())
                            ).format(
                                ((precision * distanceTravelled).toLong().times(precision * cashPerKilometer).toLong()) /
                                    (precision * precision).toDouble()
                            ),
                    DEPRECATED to DEPRECATED.tag,
                    COMMUTE_TYPE to COMMUTE_TYPE.tag,
                    VEHICLE to autoDetailsSheet.getRow(Details.AutoModel.ordinal).getCell(1).stringCellValue,
                    VEHICLE_REGISTRATION to autoDetailsSheet.getRow(Details.RegistrationNumber.ordinal).getCell(1).stringCellValue,
                    DEPRECATED2 to DEPRECATED2.tag,
                    DEPARTURE_COUNTRY to "DE",
                    DEPRECATED3 to DEPRECATED3.tag,
                    DEPRECATED4 to DEPRECATED4.tag,
                    ARRIVAL_COUNTRY to "DE"
                )
                for (date in dates) {
                    if(forthRouteIncluded){
                        val row: RowData = prototypeRowData + mapOf(
                            DESCRIPTION to stringRes(context, R.string.from_home_to_workplace),
                            FULL_ROUTE to "$baseAddress - $destinationAddress",
                            DEPARTURE_DATETIME to timeFormatter(date, shiftStartTime, -(5 + distanceTravelled.roundToInt())),
                            ARRIVAL_DATETIME to timeFormatter(date, shiftStartTime, -5)
                        )
                        rows.add(row)
                    }
                    if(backRouteIncluded) {
                        val row: RowData = prototypeRowData + mapOf(
                            DESCRIPTION to stringRes(context, R.string.from_workplace_to_home),
                            FULL_ROUTE to "$destinationAddress - $baseAddress",
                            DEPARTURE_DATETIME to timeFormatter(date, shiftEndTime, 5),
                            ARRIVAL_DATETIME to timeFormatter(date, shiftEndTime, 5 + distanceTravelled.roundToInt()),
                        )
                        rows.add(row)
                    }
                }
            }
            rows.sortBy { row ->
                val sortBy = row[DEPARTURE_DATETIME]
                LocalDate.parse(
                    sortBy,
                    DateTimeFormatter.ofPattern(
                        "dd.MM.yyyy HH:mm:ss"
                    )
                )
            }
        }

        // add sorted rows to the sheet
        rows.forEachIndexed { rowIndex, row ->
            val excelRow = commuteSheet.createRow(rowIndex + 1)
            CommuteSheetDataType.entries.forEachIndexed { columnIndex, dataType ->
                excelRow.apply {
                    createCell(columnIndex).apply {
                        setCellValue(row[dataType])
                        when (dataType) {
                            in floatPreciseData -> {
                                setCellValue(
                                    row[dataType]?.replace(',', '.')?.toDoubleOrNull() ?: 0.0
                                )
                                cellStyle = cellStyles.floatData
                            }
                            in addressData -> { cellStyle = cellStyles.addressData }
                            in primaryData -> { cellStyle = cellStyles.highlightData }
                            in secondaryData -> { cellStyle = cellStyles.defaultData }
                            else -> { cellStyle = cellStyles.defaultData }
                        }
                    }
                }
            }
        }

        val commonDateSuffixLength = " HH:mm:ss".length
        documentSummary.apply {
            dayRange =
                "${rows.firstOrNull()?.get(DEPARTURE_DATETIME)?.dropLast(commonDateSuffixLength)} - " +
                        "${rows.lastOrNull()?.get(ARRIVAL_DATETIME)?.dropLast(commonDateSuffixLength)}"
            amountOfTransfers = rows.size
            payableAmount = rows.sumOf {
                rowData -> rowData[TOTAL_EURO]?.replace(",",".")?.toDoubleOrNull() ?: 0.0
            }.toFloat()
            month = rows.lastOrNull()?.get(DEPARTURE_DATETIME)?.let {
                LocalDate.parse(
                    it,
                    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
                ).month
            } ?: LocalDate.now().month
        }

        // add the summary footer
        commuteSheet.createRow(rows.size + 1).apply {
            /* val footerLabelCell = */
            createCell(0).setCellValue(stringRes(context, R.string.footer_payable_amount_label))

            /* val footerPayableAmountSummaryCell = */
            if (rows.size > 0) {
                val sumFromAddress = commuteSheet.getRow(1).getCell(TOTAL_EURO.ordinal).address.formatAsString()
                val sumToAddress = commuteSheet.getRow(rows.size).getCell(TOTAL_EURO.ordinal).address.formatAsString()
                val sumCellFormula = "SUM($sumFromAddress:$sumToAddress)"
                createCell(TOTAL_EURO.ordinal).apply {
                    cellFormula = sumCellFormula
                }
                createCell(1).apply {
                    cellFormula = getCell(TOTAL_EURO.ordinal).address.formatAsString()
                }
            }
        }

        FileOutputStream(filepath).use {
            workbook.write(it)
        }

        workbook.close()

        return true
    }

    private fun timeFormatter(date: LocalDate, time: String, delay: Int): String  {
        val minutes = convertStringTimeToQuarters(time) * 15
        var newDate = date
        var newTime = minutes + delay
        if (newTime / 60 >= 24) {
             newDate = date.plusDays(1)
            newTime -= 24 * 60
        }
        if (minutes < 0) {
            newDate = date.minusDays(1)
            newTime += 24 * 60
        }

        val stringTime = convertMinutesToStringTime(newTime)
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        return "${newDate.format(formatter)} ${stringTime}:00"
    }


    enum class CommuteSheetDataType(val id: Int, val minColumnWidth: Int = 0, val tag: String = "") {
        ENTRY_TAB(R.string.sheet_entry),
        COMMUTE_REASON(R.string.sheet_commute_reason,tag = """PRZEJAZDZ\ZLECENIE"""),
        DISTANCE_TRAVELLED(R.string.sheet_distance_travelled),
        EURO_PER_KM_RATE(R.string.sheet_euro_per_km_rate),
        TOTAL_EURO(R.string.sheet_total_euro),
        DEPRECATED(R.string.sheet_tab),
        COMMUTE_TYPE(R.string.sheet_commute_type, tag = """SAM\PRYWATNY"""),
        VEHICLE(R.string.sheet_vehicle),
        VEHICLE_REGISTRATION(R.string.sheet_vehicle_registration),
        DEPRECATED2(R.string.sheet_tab2),
        DESCRIPTION(R.string.sheet_description),
        FULL_ROUTE(R.string.sheet_full_route),
        DEPARTURE_DATETIME(R.string.sheet_departure_datetime, tag = "XX.XX.XX xx:xx:xx"),
        ARRIVAL_DATETIME(R.string.sheet_arrival_datetime, tag = "XX.XX.XX xx:xx:xx"),
        DEPARTURE_COUNTRY(R.string.sheet_departure_country),
        DEPRECATED3(R.string.sheet_tab3),
        DEPRECATED4(R.string.sheet_tab4),
        ARRIVAL_COUNTRY(R.string.sheet_arrival_country);

        companion object {
            val floatPreciseData = setOf(TOTAL_EURO, EURO_PER_KM_RATE)
            val addressData = setOf(FULL_ROUTE)
            val primaryData = setOf(COMMUTE_TYPE, TOTAL_EURO, DISTANCE_TRAVELLED, EURO_PER_KM_RATE, DEPARTURE_DATETIME, ARRIVAL_DATETIME)
            val secondaryData = setOf(CommuteSheetDataType.entries) - primaryData - addressData
        }
    }

    private val cellStyles = object {
        val addressData by lazy {
            workbook.createCellStyle().apply {
//                setFillBackgroundColor(XSSFColor(byteArrayOf(150.toByte(), 255.toByte(), 190.toByte())))
                setFillBackgroundColor(context.getColor(R.color.worksheet_address_background).toXSSFColor())
            }
        }
        val highlightData by lazy {
            workbook.createCellStyle().apply {
//                setFillBackgroundColor(XSSFColor(byteArrayOf(255.toByte(), 200.toByte(), 100.toByte())))
                setFillBackgroundColor(context.getColor(R.color.worksheet_highlight_background).toXSSFColor())
            }
        }
        val floatData by lazy {
            workbook.createCellStyle()
                .apply { dataFormat = workbook.creationHelper.createDataFormat().getFormat("####0.00") }
        }
        val defaultHeaderData by lazy {
            workbook.createCellStyle()
                .apply{ setFont(workbook.createFont().apply { bold = true; italic = true }) }
        }
        val defaultData by lazy {
            workbook.createCellStyle()
                .apply { setFillBackgroundColor(context.getColor(R.color.worksheet_secondary_background).toXSSFColor()) }
        }
    }
}

private fun Int.toXSSFColor(): XSSFColor {
    val androidColor = this
    val red = android.graphics.Color.red(androidColor)
    val green = android.graphics.Color.green(androidColor)
    val blue = android.graphics.Color.blue(androidColor)
    val xssfColor = XSSFColor(byteArrayOf(red.toByte(), green.toByte(), blue.toByte()))
    return xssfColor
}
