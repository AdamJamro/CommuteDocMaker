package com.maur.commutedocmaker.dataSource.draftEntry

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DraftEntryDao {

    @Query("SELECT * FROM draft_table")
    fun getAllDrafts(): Flow<List<DraftEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(draft: DraftEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(drafts: List<DraftEntry>)

    @Query("DELETE FROM draft_table WHERE primary_key =:id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM draft_table")
    suspend fun deleteAll()
}