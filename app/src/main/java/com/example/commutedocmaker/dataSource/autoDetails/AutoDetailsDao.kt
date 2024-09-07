package com.example.commutedocmaker.dataSource.autoDetails

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AutoDetailsDao {
    @Query("SELECT * FROM auto_details_table")
    fun getAllAutoDetails(): Flow<List<AutoDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(details: AutoDetails)

    @Update
    suspend fun update(details: AutoDetails)

    @Query("SELECT * FROM auto_details_table WHERE id = :id")
    suspend fun getAutoDetailsById(id: Int): AutoDetails?

    @Query("DELETE FROM auto_details_table WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM auto_details_table")
    suspend fun deleteAll()
}
