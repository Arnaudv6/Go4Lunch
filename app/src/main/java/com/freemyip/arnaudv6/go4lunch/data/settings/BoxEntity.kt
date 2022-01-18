package com.freemyip.arnaudv6.go4lunch.data.settings

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "box")
data class BoxEntity @Deprecated("Room constructor") constructor(
    @PrimaryKey(autoGenerate = false)
    val key: Int,
    val north: Double,
    val south: Double,
    val west: Double,
    val east: Double,
) {
    @Suppress("DEPRECATION")
    constructor(north: Double, south: Double, west: Double, east: Double) :
            this(0, north, south, west, east)
}

