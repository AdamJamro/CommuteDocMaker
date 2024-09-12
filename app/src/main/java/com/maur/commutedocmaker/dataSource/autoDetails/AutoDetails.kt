package com.maur.commutedocmaker.dataSource.autoDetails

import androidx.annotation.StringRes
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.maur.commutedocmaker.R

enum class Details(@StringRes val id: Int = 0) {
    OwnerFirstName(R.string.owner_first_name),
    OwnerLastName(R.string.owner_last_name),
    Address(R.string.address),
    AutoBrand(R.string.auto_brand),
    AutoModel(R.string.auto_model),
    MotorCapacity(R.string.motor_capacity),
    RegistrationNumber(R.string.registration_number)
}

@Entity(tableName = "auto_details_table")
data class AutoDetails(
    @PrimaryKey(autoGenerate = false) val id: Int = 0,
    @ColumnInfo(name = "details") var details: List<String> = List(Details.entries.size) { "" }
)