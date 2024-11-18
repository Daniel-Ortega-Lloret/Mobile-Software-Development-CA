package com.example.mobiledevca_taskapp.taskDatabase

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class TaskApplication: Application() {

    val applicationScope = CoroutineScope(SupervisorJob())

    //by lazy makes it so that these are only made when they are needed, not as the activity starts
    val database by lazy { TaskRoomDatabase.getDatabase(this, applicationScope)}
    val repository by lazy { TaskRepository(database.taskDao()) }

}