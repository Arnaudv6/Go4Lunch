package com.cleanup.go4lunch.data.settings

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setBox(boxEntity: BoxEntity)

    @Query("SELECT * FROM box LIMIT 1")
    suspend fun getBox(): BoxEntity?
    // OK: result does not have to be a list.

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setNavNum(intEntity: IntEntity)

    @Query("SELECT * FROM int_store WHERE key_string=:key LIMIT 1")
    fun getNavNum(key: String=IntEntity.NAV_NUM): Flow<IntEntity?>
}

