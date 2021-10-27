package com.cleanup.go4lunch.data.pois

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
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
    var address: String,

    // data that requires an added request
    var cuisine: String,
    @ColumnInfo(name = "image_url")
    var imageUrl: String,
    var phone: String,
    var site: String,
    var hours: String
    // https://wiki.openstreetmap.org/wiki/Key:opening_hours/specification
    // https://wiki.openstreetmap.org/wiki/Key:opening_hours#Java
) {

    @Ignore
    // Todo Nino, @ignore : inutile pour les functions?
    fun cuisineAndAddress(): String {
        return listOfNotNull(
            this.cuisine.ifEmpty { null },
            this.address.split(" - ")[0].ifEmpty { null }
        ).joinToString(" - ")
    }

}

