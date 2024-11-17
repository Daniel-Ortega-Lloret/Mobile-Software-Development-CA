package com.example.mobiledevca_taskapp.taskDatabase

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mobiledevca_taskapp.taskDatabase.entities.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Task::class], version = 1)
abstract class TaskRoomDatabase : RoomDatabase() {
    abstract fun taskDao() : TaskDAO

    companion object {
        @Volatile
        private var INSTANCE: TaskRoomDatabase? = null

        fun getDatabase(context: Context, applicationScope: CoroutineScope): TaskRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, TaskRoomDatabase::class.java, "task_database")
                    .fallbackToDestructiveMigrationFrom()
                    .addCallback(TaskDatabaseCallback(applicationScope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class TaskDatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
            //Populates database with data from room database
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                //Add extra stuff here if you want to have hard coded inserts on the db
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.taskDao())
                    }
                }
            }
        }

        suspend fun populateDatabase(taskDao: TaskDAO) {
            taskDao.deleteAllTasks()

            var task = Task(1, "Make Database")
            taskDao.insertTask(task)
            task = Task(2, "Destroy Dylan")
            taskDao.insertTask(task)
        }
    }
}


