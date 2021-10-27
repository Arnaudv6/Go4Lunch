package com.cleanup.go4lunch.ui.map

import android.app.Activity
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.ui.detail.DetailsActivity
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow

class MyMarkerInfoWindow(
    private val activity: Activity,
    private val osmId: Long,
    mapView: MapView,
    layoutResId: Int = org.osmdroid.library.R.layout.bonuspack_bubble
) : MarkerInfoWindow(layoutResId, mapView) {

    // can't setOnClickListener
    override fun onOpen(item: Any?) {
        super.onOpen(item)
        if (mView == null) return
        // to avoid getting parent's parent, have to replace bonuspack_bubble with own layout
        // todo fermer les autres infoWindow
        (mView.findViewById<TextView>(R.id.bubble_title).parent.parent as LinearLayout).setOnClickListener {
            activity.startActivity(DetailsActivity.navigate(activity, osmId))

            // todo Nino : je peux injecter appContext et m'en servir pour startActivity(), ou c'est mal ?
            Log.e("TAG", "onOpen: ")
        }
    }
}

