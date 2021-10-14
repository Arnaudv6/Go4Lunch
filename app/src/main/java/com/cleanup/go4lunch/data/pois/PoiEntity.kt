package com.cleanup.go4lunch.data.pois

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "poi_pins"
)
class PoiEntity(
    // data received from search in a box
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,

    // data hard to extract from search in a box
    val address: String,

    // data that requires an added request
    val type: String,
    @ColumnInfo(name = "image_url")
    val imageUrl: String,
    val phone: String,
    val site: String

    // todo hours
)

