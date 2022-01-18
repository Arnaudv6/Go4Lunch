package com.freemyip.go4lunch.data.settings

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SettingsDao {

    // box is a structured object, belongs in SQL, not in (primitives) SharedPreferences
    @Query("SELECT * FROM box LIMIT 1")
    suspend fun getBox(): BoxEntity?
    // OK. result does not have to be a list, nor a Flow

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setBox(boxEntity: BoxEntity)
}



