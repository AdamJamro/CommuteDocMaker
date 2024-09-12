package com.maur.commutedocmaker.dataSource.preference

import androidx.room.*
import kotlinx.coroutines.flow.Flow

sealed class PreferenceType(val key: String) {
    data object ACCESS : PreferenceType("ACCESS") {
        const val GRANTED = "GRANTED"
        const val DENIED = "DENIED"
    }
}

@Entity(tableName = "app_preferences")
data class Preference(
    @PrimaryKey @ColumnInfo(name = "primary_key") val key: String,
    @ColumnInfo(name = "value") var value: String
)

@Dao
interface PreferenceDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPreference(preference: Preference)

    @Query("SELECT * FROM app_preferences")
    fun getAllPreferences(): Flow<List<Preference>>

    @Query("SELECT * FROM app_preferences WHERE primary_key = :key")
    suspend fun getPreference(key: String): Preference?

    @Query("DELETE FROM app_preferences WHERE primary_key = :key")
    suspend fun deletePreference(key: String)

    @Update
    suspend fun updatePreference(preference: Preference)
}

