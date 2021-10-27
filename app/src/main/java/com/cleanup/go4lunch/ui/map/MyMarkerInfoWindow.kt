package com.cleanup.go4lunch.ui.map

import android.app.Activity
import android.graphics.Paint
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.ui.detail.DetailsActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow

@ExperimentalCoroutinesApi
class MyMarkerInfoWindow(
    private val activity: Activity, // TODO check for leaks (laters)
    private val osmId: Long,
    mapView: MapView,
    layoutResId: Int = org.osmdroid.library.R.layout.bonuspack_bubble
) : MarkerInfoWindow(layoutResId, mapView) {

    // can't setOnClickListener
    override fun onOpen(item: Any?) {
        super.onOpen(item)
        if (mView == null) return
        // to avoid getting parent's parent, have to replace bonuspack_bubble with own layout
        val title = mView.findViewById<TextView>(R.id.bubble_title)
        title.paintFlags = title.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        title.setOnClickListener {
            activity.startActivity(DetailsActivity.navigate(activity, osmId))

            Log.e("TAG", "onOpen: ")
        }
    }
}

