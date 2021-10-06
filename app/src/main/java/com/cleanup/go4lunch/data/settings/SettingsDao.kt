package com.cleanup.go4lunch.data.settings

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import javax.inject.Singleton

@Dao
interface SettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setBox(boxEntity: BoxEntity)

    @Query("SELECT * FROM box")
    fun getBox(): Flow<BoxEntity>

}
