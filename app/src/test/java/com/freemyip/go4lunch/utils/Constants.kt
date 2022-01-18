package com.freemyip.go4lunch.utils

import com.freemyip.go4lunch.data.pois.PoiEntity

class Constants {


    companion object {
        val POI_ENTITY = PoiEntity(
            id = 44,
            name = "la Chaummière",
            latitude = 44.5,
            longitude = 5.7,
            address = "2 rue de la gueuse",
            cuisine = "Regional",
            imageUrl = "https://ma.super.image/de/restaurant.extension",
            phone = "+33485469585",
            site = "https://mon.super.site/",
            hours = "Mo-Sa 10:15",
            rating = 2.0F
        )
    }


}