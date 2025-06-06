package com.maur.commutedocmaker.dataSource.draftEntry

import java.io.Serializable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

data class DraftDataPatch(
    val baseAddress: String = "",
    val destinationAddress: String = "",
    val distanceTravelled: Float = 0f,
    val shiftStartTime: String = "08:00",
    val shiftEndTime: String = "16:00",
    val forthRouteIncluded: Boolean = true,
    val backRouteIncluded: Boolean = true,
    val cashPerKilometer: Float = 0f,
    val dates: List<LocalDate> = emptyList()
): Serializable

@Entity(tableName = "draft_table")
data class DraftEntry(
    var title: String,
    var contentDescription: String,
    var draftDataPatches: List<DraftDataPatch> = emptyList(),
    var filePath: String = "files/${title}.txt",
    @PrimaryKey(autoGenerate = false) @ColumnInfo(name = "primary_key") var draftId: Int = 0
) : Serializable {
    override fun toString(): String {
        return "DraftEntry(title='$title', content='$contentDescription')"
    }
}

