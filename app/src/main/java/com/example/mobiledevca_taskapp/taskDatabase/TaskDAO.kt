package com.example.mobiledevca_taskapp.taskDatabase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.mobiledevca_taskapp.taskDatabase.entity.Profile

@Dao //Data Access Object pattern for accessing DB
interface TaskDAO {
    @Insert //Inserts given profile into DB
    fun insertTaskProfile(profile: Profile)

    @Update //Updates 0 or more profiles
    fun updateProfile(vararg profiles: Profile)

    @Delete //Deletes 0 or more profiles
    fun deleteTaskProfile(vararg profiles: Profile)

    @Query("SELECT * FROM Profile WHERE pId == :pId") //Selects given profile
    fun getAllTaskProfiles(pId: Int)
}