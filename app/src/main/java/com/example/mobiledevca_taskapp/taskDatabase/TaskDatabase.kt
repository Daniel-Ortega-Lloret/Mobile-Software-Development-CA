package com.example.mobiledevca_taskapp.taskDatabase

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mobiledevca_taskapp.taskDatabase.entity.Profile
import android.content.Context

@Database(entities = [Profile::class], version = 1)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao() : TaskDAO

    companion object {
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        fun getDatabase(context: Context): TaskDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context, TaskDatabase::class.java, "task_database")
                    .fallbackToDestructiveMigrationFrom()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}


