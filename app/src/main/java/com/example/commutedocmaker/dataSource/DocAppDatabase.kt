package com.example.commutedocmaker.dataSource

import android.content.Context
import android.util.Log
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.commutedocmaker.dataSource.autoDetails.AutoDetails
import com.example.commutedocmaker.dataSource.autoDetails.AutoDetailsDao
import com.example.commutedocmaker.dataSource.document.Document
import com.example.commutedocmaker.dataSource.document.DocumentDao
import com.example.commutedocmaker.dataSource.draftEntry.DraftDataPatch
import com.example.commutedocmaker.dataSource.draftEntry.DraftEntry
import com.example.commutedocmaker.dataSource.draftEntry.DraftEntryDao
import com.example.commutedocmaker.dataSource.preference.Preference
import com.example.commutedocmaker.dataSource.preference.PreferenceDao
import com.example.commutedocmaker.dataSource.preference.PreferenceType
import com.example.commutedocmaker.shouldRevokeAccess
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class Converters {
    private val gson: Gson

    init {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
        gson = gsonBuilder.create()
    }

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
        return patches?.let {
            val type = object : TypeToken<List<DraftDataPatch>>() {}.type
            gson.toJson(patches, type)
        }
    }

    @TypeConverter
    fun valueToPatchList(value: String?): ArrayList<DraftDataPatch>? {
        return value?.let{
            val type = object : TypeToken<ArrayList<DraftDataPatch>>() {}.type
            gson.fromJson(value, type)
        }
    }

    @TypeConverter
    fun toDate(value: String?): LocalDate? {
        return value?.let{
            val type = object : TypeToken<LocalDate>() {}.type
            gson.fromJson(value, type)
        }
    }

    @TypeConverter
    fun toDateString(date: LocalDate?): String? {
        return date?.let {
            val type = object : TypeToken<LocalDate>() {}.type
            gson.toJson(date, type)
        }
    }

    @TypeConverter
    fun toString(value: Document.DocumentSummaryInformation?): String? {
        return value?.let{
            val type = object : TypeToken<Document.DocumentSummaryInformation>() {}.type
            gson.toJson(value, type)
        }
    }

    @TypeConverter
    fun fromString(value: String?): Document.DocumentSummaryInformation? {
        return value?.let{
            val type = object : TypeToken<Document.DocumentSummaryInformation>() {}.type
            gson.fromJson(value, type)
        }
    }
}

class LocalDateAdapter : JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun serialize(src: LocalDate?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(formatter.format(src))
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): LocalDate {
        return LocalDate.parse(json?.asString, formatter)
    }
}

@Database(
    entities = [
        DraftEntry::class,
        AutoDetails::class,
        Preference::class,
        Document::class
    ],
    version = 1)
@TypeConverters(Converters::class)
abstract class DocAppDatabase : RoomDatabase() {
    abstract fun draftEntryDao(): DraftEntryDao
    abstract fun autoDetailsDao(): AutoDetailsDao
    abstract fun preferenceDao(): PreferenceDao
    abstract fun documentDao(): DocumentDao

    companion object {
        private var INSTANCE: DocAppDatabase? = null

        fun getInstance(
            context: Context,
            scope: CoroutineScope
        ): DocAppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DocAppDatabase::class.java,
                    "draftApp.db"
                )
                    .addCallback(DatabaseCallback(scope))
//                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                //return
                instance
            }
        }
        fun destroyInstance() {
            if(INSTANCE?.isOpen == true) {
                INSTANCE?.close()
            }

            INSTANCE = null
        }
    }

    private class DatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.draftEntryDao())
                }

                for (preference in PreferenceType::class.sealedSubclasses) {

                    preference.simpleName
                        ?.let {
                            val key = it
                            val value = when(it) {
                                PreferenceType.ACCESS.key -> PreferenceType.ACCESS.GRANTED
                                else -> ""
                            }
                            Preference(key, value)
                        }
                        ?.let {
                            scope.launch {
                                database.preferenceDao().insertPreference(it)
                            }
                        }
                }
            }
        }

//        override fun onOpen(db: SupportSQLiteDatabase) {
//            super.onOpen(db)
//        }

        suspend fun populateDatabase(draftEntryDao: DraftEntryDao) {
            draftEntryDao.insert(
                DraftEntry("Example", "Hello! this is an example draft", draftId = 0),
            )
            draftEntryDao.insert(
                DraftEntry("Another Example", "Hello! this is another example draft", draftId = 1)
            )
        }
    }
}

