package com.example.mobiledevca_taskapp.taskDatabase

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import android.util.Log
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mobiledevca_taskapp.taskDatabase.entities.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
//Everytime we change the schema (which we will) we update the version number
//Current version: 2
@Database(entities = [Task::class], version = 2)
abstract class TaskRoomDatabase : RoomDatabase() {
    abstract fun taskDao() : TaskDAO

    companion object {
        @Volatile
        private var INSTANCE: TaskRoomDatabase? = null

        fun getDatabase(context: Context, applicationScope: CoroutineScope): TaskRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, TaskRoomDatabase::class.java, "task_database")
                    .fallbackToDestructiveMigration()
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

            val task = Task(0, "Make Database", "For Tasks")
            taskDao.insertTask(task)
        }
    }
}


