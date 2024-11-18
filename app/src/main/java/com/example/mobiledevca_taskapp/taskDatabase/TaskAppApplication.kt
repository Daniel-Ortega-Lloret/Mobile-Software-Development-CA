package com.example.mobiledevca_taskapp.taskDatabase

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class TaskAppApplication: Application() {

    val applicationScope = CoroutineScope(SupervisorJob())

    //by lazy makes it so that these are only made when they are needed, not as the activity starts
    val database by lazy { TaskAppRoomDatabase.getDatabase(this, applicationScope) }
    val taskRepository by lazy { TaskAppRepository(database.taskDao()) }
    val habitRepository by lazy { TaskAppRepository(database.habitDao())}

}