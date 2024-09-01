package com.example.commutedocmaker.dataSource

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.Serializable

//enum class DraftFormField(
//    val fieldAsString: String = "",
//) {
//    BASE_ADDRESS(fieldAsString = R.string.base_address_string_representation.toString()),
//    DESTINATION_ADDRESS(fieldAsString = R.string.destination_address_string_representation.toString()),
//    DISTANCE_TRAVELLED(fieldAsString = R.string.distance_travelled_string_representation.toString()),
//    SHIFT_TIME_LENGTH(fieldAsString = R.string.shift_time_length_string_representation.toString()),
//    SHIFT_START_FULL_DATE(fieldAsString = R.string.shift_start_full_date_string_representation.toString())
//}

class DraftSaveAdapter(val title: String, val content: String, val draftDataPatches: List<DraftDataPatch>): Serializable {
    override fun toString(): String {
        return "spreadsheetDraftRawData(title='$title', content='$content')"
    }
    private val filepath: String = "drafts/${title}.txt"

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
        try {
            // Create a file in the internal storage directory
            var file = File(context.filesDir, title)

            var counter = 1
            while (file.exists()) {
                val newTitle = "$title${counter}"
                counter++
                file = File(context.filesDir, newTitle)
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