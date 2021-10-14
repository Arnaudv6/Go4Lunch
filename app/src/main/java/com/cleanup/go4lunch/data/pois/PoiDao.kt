package com.cleanup.go4lunch.data.pois

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cleanup.go4lunch.data.settings.BoxEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PoiDao {

    // replace our (possibly dated) data with fresh, readily downloaded data
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoi(poiEntity: PoiEntity)

    @Query("SELECT * FROM poi_pins")
    fun getPoiEntities(): Flow<List<PoiEntity>>

}