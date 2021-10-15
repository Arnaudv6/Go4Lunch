package com.cleanup.go4lunch.data.settings

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "box",
)

data class BoxEntity(
    val north: Double,
    val south: Double,
    val west: Double,
    val east: Double,

    // deprecated will only work with an explicit constructors
    @Deprecated("Don't set key: single-row table")
    @PrimaryKey(autoGenerate = false)
    val key: Int = 1
)
