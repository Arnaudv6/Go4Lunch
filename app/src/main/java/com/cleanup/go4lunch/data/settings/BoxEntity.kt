package com.cleanup.go4lunch.data.settings

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "box")
class BoxEntity {
    // can not make this a data class: we want to force key value. And explicit this.
    //  @Deprecated decorator will only apply with an explicit constructors
    @PrimaryKey(autoGenerate = false)
    val key: Int = 1

    val north: Double
    val south: Double
    val west: Double
    val east: Double

    @Ignore
    constructor(north: Double, south: Double, west: Double, east: Double) {
        this.north = north
        this.south = south
        this.west = west
        this.east = east
    }

    // todo Nino: how to remove this warning the right way?
    @Deprecated("Don't set key: single-row table")
    constructor(north: Double, south: Double, west: Double, east: Double, key: Int) {
        this.north = north
        this.south = south
        this.west = west
        this.east = east
    }

}
