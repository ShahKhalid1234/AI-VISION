package com.example.data

import android.content.Context
import androidx.room.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class DatabaseTypeConverters {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    @TypeConverter
    fun stringToStringList(value: String?): List<String>? {
        if (value == null) return null
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        return moshi.adapter<List<String>>(type).fromJson(value)
    }

    @TypeConverter
    fun stringListToString(list: List<String>?): String? {
        if (list == null) return null
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        return moshi.adapter<List<String>>(type).toJson(list)
    }
}

@Database(
    entities = [
        ConceptEntity::class,
        ConceptRelationshipEntity::class,
        UserProgressEntity::class,
        ChatMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DatabaseTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conceptDao(): ConceptDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "visionmind_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
