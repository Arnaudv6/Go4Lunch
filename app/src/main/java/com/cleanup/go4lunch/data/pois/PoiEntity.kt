package com.cleanup.go4lunch.data.pois

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "poi_pins"
)
class PoiEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val latitude: Double,
    val longitude: Double,
    val type: String,
    val category: String,
    val description: String
)
