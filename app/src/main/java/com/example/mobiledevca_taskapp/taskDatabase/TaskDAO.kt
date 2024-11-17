package com.example.mobiledevca_taskapp.taskDatabase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mobiledevca_taskapp.taskDatabase.entities.Profile
import com.example.mobiledevca_taskapp.taskDatabase.entities.Task
import kotlinx.coroutines.flow.Flow

@Dao //Data Access Object pattern for accessing DB
interface TaskDAO {
//    @Insert //Inserts given profile into DB
//    fun insertProfile(profile: Profile)
//
//    @Update //Updates 0 or more profiles
//    fun updateProfile(vararg profiles: Profile)
//
//    @Delete //Deletes 0 or more profiles
//    fun deleteProfile(vararg profiles: Profile)
//
//    @Query("SELECT * FROM Profile WHERE pId == :pId") //Selects given profile
//    fun getAllProfiles(pId: Int): Flow<List<Profile>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTask(task: Task)

    @Query("SELECT * FROM Task")
    fun getAllTasks(): Flow<List<Task>>

    @Query("DELETE FROM Task")
    suspend fun deleteAllTasks()
}