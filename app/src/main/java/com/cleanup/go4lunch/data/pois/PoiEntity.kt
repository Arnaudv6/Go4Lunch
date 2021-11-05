package com.cleanup.go4lunch.data.pois

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "poi_pins")
data class PoiEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val cuisine: String,
    @ColumnInfo(name = "image_url")
    val imageUrl: String,
    val phone: String?,
    val site: String?,
    val hours: String?,
    // https://wiki.openstreetmap.org/wiki/Key:opening_hours/specification
    // https://wiki.openstreetmap.org/wiki/Key:opening_hours#Java
    val rating: Float?
)

