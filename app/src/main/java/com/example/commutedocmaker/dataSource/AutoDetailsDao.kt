package com.example.commutedocmaker.dataSource

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AutoDetailsDao {
    @Query("SELECT * FROM auto_details_table")
    fun getAllAutoDetails(): Flow<List<AutoDetailsData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(details: AutoDetailsData)

    @Update
    suspend fun update(details: AutoDetailsData)

    @Query("SELECT * FROM auto_details_table WHERE id = :id")
    suspend fun getAutoDetailsById(id: Int): AutoDetailsData?

    @Query("DELETE FROM auto_details_table WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM auto_details_table")
    suspend fun deleteAll()
}
