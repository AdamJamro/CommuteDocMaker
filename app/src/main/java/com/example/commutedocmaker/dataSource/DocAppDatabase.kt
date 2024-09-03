package com.example.commutedocmaker.dataSource

import android.content.Context
import android.util.Log
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.commutedocmaker.dataSource.autoDetailsData.AutoDetailsDao
import com.example.commutedocmaker.dataSource.autoDetailsData.AutoDetailsData
import com.example.commutedocmaker.dataSource.draftEntry.DraftDataPatch
import com.example.commutedocmaker.dataSource.draftEntry.DraftEntry
import com.example.commutedocmaker.dataSource.draftEntry.DraftEntryDao
import com.example.commutedocmaker.dataSource.preference.Preference
import com.example.commutedocmaker.dataSource.preference.PreferenceDao
import com.example.commutedocmaker.dataSource.preference.PreferenceType
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.reflect.KClass

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
        AutoDetailsData::class,
        Preference::class
    ],
    version = 1)
@TypeConverters(Converters::class)
abstract class DocAppDatabase : RoomDatabase() {
    abstract fun draftEntryDao(): DraftEntryDao
    abstract fun autoDetailsDao(): AutoDetailsDao
    abstract fun preferenceDao(): PreferenceDao

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
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
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
                for (preference in PreferenceType::class.sealedSubclasses) {

                    preference.simpleName
                        ?.let {
                            Log.d("DEBUG", "DatabaseCallback.onCreate: inserting preference $it")
                            Preference(it, "")
                        }
                        ?.let {
                            scope.launch {
                                database.preferenceDao().insertPreference(it)
                            }
                        }
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

