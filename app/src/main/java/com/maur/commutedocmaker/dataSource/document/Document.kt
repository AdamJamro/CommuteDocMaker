package com.maur.commutedocmaker.dataSource.document

import androidx.room.*
import androidx.room.OnConflictStrategy.Companion.REPLACE
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.Month

@Entity(tableName = "document_table")
data class Document(
    @PrimaryKey(autoGenerate = false) @ColumnInfo(name = "file_name") val title: String,
    @ColumnInfo(name = "filepath") val path: String,
    @ColumnInfo(name = "creation_date") val date: LocalDate,
    @ColumnInfo(name = "description") val documentSummaryInformation: DocumentSummaryInformation? = null
) {
    data class DocumentSummaryInformation(
        val month: Month,
        val dayRange: String,
        val amountOfTransfers: Int,
        val payableAmount: Float,
    )
}

@Dao
interface DocumentDao {
    @Query("SELECT * FROM document_table")
    fun getAllDocuments(): Flow<List<Document>>

    @Insert(onConflict = REPLACE)
    suspend fun insert(document: Document)

    @Query("SELECT * FROM document_table WHERE file_name = :title")
    suspend fun get(title: String): Document?

    @Query("DELETE FROM document_table WHERE file_name = :title")
    suspend fun delete(title: String)

    @Update
    suspend fun update(document: Document)

    @Query("DELETE FROM document_table")
    suspend fun deleteAll()
}