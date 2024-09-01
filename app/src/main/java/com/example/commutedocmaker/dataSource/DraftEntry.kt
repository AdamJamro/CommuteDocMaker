package com.example.commutedocmaker.dataSource

import java.io.Serializable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate

data class DraftDataPatch(
    val baseAddress: String = "",
    val destinationAddress: String = "",
    val distanceTravelled: String = "",
    val shiftStartTime: String = "",
    val shiftEndTime: String = "",
    val forthRouteIncluded: Boolean = true,
    val backRouteIncluded: Boolean = true,
    val dates: List<LocalDate> = emptyList()
): Serializable

@Entity(tableName = "draft_table")
data class DraftEntry(
    var title: String,
    var content: String,
    var draftDataPatches: List<DraftDataPatch>,
    var filePath: String = "drafts/${title}.txt"
) : Serializable {
    @PrimaryKey(autoGenerate = false) @ColumnInfo(name = "primary_key") var draftId: Int = 0
    override fun toString(): String {
        return "DraftEntry(title='$title', content='$content')"
    }
}

