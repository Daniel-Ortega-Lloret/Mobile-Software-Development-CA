package com.example.mobiledevca_taskapp.taskDatabase

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mobiledevca_taskapp.taskDatabase.dao.HabitDAO
import com.example.mobiledevca_taskapp.taskDatabase.dao.ScheduleDAO
import com.example.mobiledevca_taskapp.taskDatabase.dao.TaskDAO
import com.example.mobiledevca_taskapp.taskDatabase.entities.Day
import com.example.mobiledevca_taskapp.taskDatabase.entities.Habit
import com.example.mobiledevca_taskapp.taskDatabase.entities.Task
import com.example.mobiledevca_taskapp.taskDatabase.scheduleClasses.TaskConverter
import com.example.mobiledevca_taskapp.taskDatabase.scheduleClasses.TimeSlotListConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//Everytime we change the schema (which we will) we update the version number
@Database(entities = [Task::class, Day::class, Habit::class], version = 22)
@TypeConverters(TaskConverter::class, TimeSlotListConverter::class)
abstract class TaskAppRoomDatabase : RoomDatabase() {
    abstract fun taskDao() : TaskDAO
    abstract fun scheduleDao() : ScheduleDAO
    abstract fun habitDao() : HabitDAO

    companion object {
        @Volatile
        private var INSTANCE: TaskAppRoomDatabase? = null

        fun getDatabase(context: Context, applicationScope: CoroutineScope): TaskAppRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, TaskAppRoomDatabase::class.java, "task_database")
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
                        populateDatabase(database.taskDao(), database.habitDao())
                    }
                }
            }
        }

        suspend fun populateDatabase(taskDao: TaskDAO, habitDao: HabitDAO) {
            taskDao.deleteAll()

            habitDao.deleteAll()
        }
    }
}


