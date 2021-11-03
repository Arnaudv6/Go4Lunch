package com.cleanup.go4lunch.data.pois

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "poi_pins"
)
data class PoiEntity(
    // data received from search in a box
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,

    // can be recomposed from either search in a box or detail search queries
    val address: String,

    // data that requires an added request
    val cuisine: String,
    @ColumnInfo(name = "image_url")
    val imageUrl: String,
    val phone: String?,
    val site: String?,
    val hours: String?,
    val rating: Float?
    // https://wiki.openstreetmap.org/wiki/Key:opening_hours/specification
    // https://wiki.openstreetmap.org/wiki/Key:opening_hours#Java
)

