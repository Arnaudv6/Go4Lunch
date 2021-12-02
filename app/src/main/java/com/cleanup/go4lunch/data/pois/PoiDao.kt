package com.cleanup.go4lunch.data.pois

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PoiDao {

    // replace our (possibly dated) data with fresh, readily downloaded data
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoi(poiEntity: PoiEntity)

    @Query("SELECT * FROM poi_pins")
    fun getPoiEntities(): Flow<List<PoiEntity>>

    @Query("SELECT * FROM poi_pins WHERE id=:osmId LIMIT 1")
    suspend fun getPoiById(osmId: Long): PoiEntity?

    // @Query("UPDATE poi_pins SET rating=:rating WHERE id = :osmId")
    // suspend fun updatePoiRating(osmId: Long, rating: Int)

    @Query("SELECT id FROM poi_pins")
    suspend fun getPoiIds(): List<Long>
}
