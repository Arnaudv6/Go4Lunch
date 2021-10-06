package com.cleanup.go4lunch.data.pois

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cleanup.go4lunch.data.settings.BoxEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PoiDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun setBox(boxEntity: BoxEntity)

    @Query("SELECT * FROM box")
    fun getBox(): Flow<BoxEntity>
}