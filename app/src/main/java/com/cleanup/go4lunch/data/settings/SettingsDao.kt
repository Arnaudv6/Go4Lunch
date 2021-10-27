package com.cleanup.go4lunch.data.settings

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SettingsDao {

    // box is a structured object, not a _single_ primitive: use SQL instead of shared Preferences
    @Query("SELECT * FROM box LIMIT 1")
    suspend fun getBox(): BoxEntity?
    // OK: result does not have to be a list, nor a Flow

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setBox(boxEntity: BoxEntity)
}



