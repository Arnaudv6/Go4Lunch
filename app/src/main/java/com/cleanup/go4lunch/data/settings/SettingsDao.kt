package com.cleanup.go4lunch.data.settings

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {

    @Query("SELECT * FROM box LIMIT 1")
    suspend fun getBox(): BoxEntity?
    // OK: result does not have to be a list, nor a Flow

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setBox(boxEntity: BoxEntity)

    @Query("SELECT * FROM int_store WHERE key_string=:key LIMIT 1")
    suspend fun getNavNum(key: String=IntEntity.NAV_NUM): IntEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setNavNum(intEntity: IntEntity)
}

