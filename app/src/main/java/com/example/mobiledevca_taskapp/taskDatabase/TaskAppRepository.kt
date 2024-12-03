package com.example.mobiledevca_taskapp.taskDatabase

import androidx.annotation.WorkerThread
import com.example.mobiledevca_taskapp.taskDatabase.dao.BaseDAO
import kotlinx.coroutines.flow.Flow

//Generic repo parent class, make a child repo for specific query calls to specific DAOs
open class TaskAppRepository<T> (
    private val dao: BaseDAO<T>
){
    val allItems: Flow<List<T>> = dao.getAll()

    @WorkerThread
    suspend fun insert(item: T) {
        dao.insert(item)
    }

    @WorkerThread
    suspend fun deleteAll() {
        dao.deleteAll()
    }
}