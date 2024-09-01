package com.example.commutedocmaker.dataSource

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

class Converters {
    @TypeConverter
    fun valueToList(value: String): List<String> {
        return value.split(",")
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun fromPatchList(patches: List<DraftDataPatch>?): String? {
        if (patches == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<DraftDataPatch>>() {}.type
        return gson.toJson(patches, type)
    }

    @TypeConverter
    fun valueToPatchList(value: String?): ArrayList<DraftDataPatch>? {
        if (value == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<ArrayList<DraftDataPatch>>() {}.type
        return gson.fromJson(value, type)
    }
}

@Database(entities = [DraftEntry::class, AutoDetailsData::class], version = 1)
@TypeConverters(Converters::class)
abstract class DocAppDatabase : RoomDatabase() {
    abstract fun draftEntryDao(): DraftEntryDao
    abstract fun autoDetailsDao(): AutoDetailsDao

    companion object {
        private var INSTANCE: DocAppDatabase? = null

        fun getInstance(
            context: Context,
            scope: CoroutineScope
        ): DocAppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext,
                    DocAppDatabase::class.java, "draft_entry.db")
//                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                //return
                instance
            }
        }
        fun destroyInstance() {
            INSTANCE = null
        }
    }

    private class DatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            if (INSTANCE == null) return

            INSTANCE.let { database ->
                scope.launch {
                    INSTANCE?.draftEntryDao()?.getAllDrafts()?.toList()?.isEmpty() ?: return@launch
                        populateDatabase(database!!.draftEntryDao())
                }
            }
        }

        suspend fun populateDatabase(draftEntryDao: DraftEntryDao) {
//            draftEntryDao.deleteAll()

            val draft = DraftEntry("Example", "Hello! this is an example draft", emptyList())
            draftEntryDao.insert(draft)
        }
    }
}

