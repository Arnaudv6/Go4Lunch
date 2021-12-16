package com.cleanup.go4lunch.ui.map

import android.graphics.Paint
import android.widget.TextView
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.ui.main.DetailsActivityLauncher
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow
import java.lang.ref.WeakReference

class MyMarkerInfoWindow(
    private val osmId: Long,
    mapView: MapView,
    private val detailsActivityLauncher: WeakReference<DetailsActivityLauncher>,
    layoutResId: Int = org.osmdroid.library.R.layout.bonuspack_bubble
) : MarkerInfoWindow(layoutResId, mapView) {

    // can't setOnClickListener from mapFragment, as infoWindow is not inflated yet.
    override fun onOpen(item: Any?) {
        super.onOpen(item)
        if (mView == null) return
        val title = mView.findViewById<TextView>(R.id.bubble_title)
        title.paintFlags = title.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        title.setOnClickListener {
            detailsActivityLauncher.get()?.launch(osmId)
        }
    }
}

