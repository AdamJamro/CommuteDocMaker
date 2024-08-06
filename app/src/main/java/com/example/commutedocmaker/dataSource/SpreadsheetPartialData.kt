package com.example.commutedocmaker.dataSource

import android.content.Context
import com.example.commutedocmaker.R
import com.example.commutedocmaker.ui.viewModels.UICollectedData
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

enum class DraftFormField(
    val fieldAsString: String = "",
) {
    BASE_ADDRESS(fieldAsString = R.string.base_address_string_representation.toString()),
    DESTINATION_ADDRESS(fieldAsString = R.string.destination_address_string_representation.toString()),
    DISTANCE_TRAVELLED(fieldAsString = R.string.distance_travelled_string_representation.toString()),
    SHIFT_TIME_LENGTH(fieldAsString = R.string.shift_time_length_string_representation.toString()),
    SHIFT_START_FULL_DATE(fieldAsString = R.string.shift_start_full_date_string_representation.toString())
}

class SpreadsheetPartialData(var title: String, val content: String, val uiCollectedData: UICollectedData) {
    override fun toString(): String {
        return "spreadsheetPartialData(title='$title', content='$content')"
    }
    val filepath: String = "drafts/${title}.txt"

    fun load() {
        // load from file
    }

    fun save() {
        // save to file
    }

    fun generateDoc() {
        // generate doc
    }

    fun writeToFile(context: Context): String? {
        var file: File? = null
        try {
            // Create a file in the internal storage directory
            file = File(context.filesDir, title)

            if (file.exists()) {
                // Handle the case where the file already exists
                // You can return null, throw an exception, or notify the user
                println("File already exists with the name: $title")
                return null
            }

            FileOutputStream(file).use { outputStream ->
                outputStream.write(content.toByteArray())
            }
            return filepath
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return "abc error"
    }
}