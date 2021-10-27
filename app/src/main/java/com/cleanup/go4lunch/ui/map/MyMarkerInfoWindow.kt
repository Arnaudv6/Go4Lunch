package com.cleanup.go4lunch.ui.map

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Paint
import android.widget.TextView
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.ui.detail.DetailsActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow

@ExperimentalCoroutinesApi
class MyMarkerInfoWindow(
    private val osmId: Long,
    mapView: MapView,
    layoutResId: Int = org.osmdroid.library.R.layout.bonuspack_bubble
) : MarkerInfoWindow(layoutResId, mapView) {

    // can't setOnClickListener from mapFragment, as infoWindow is not inflated yet.
    override fun onOpen(item: Any?) {
        super.onOpen(item)
        if (mView == null) return
        val title = mView.findViewById<TextView>(R.id.bubble_title)
        title.paintFlags = title.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        title.setOnClickListener {
            val activity = mapView.context.getActivity() as Activity // todo, Nino, mieux que de donner l'activity dans le constructeur?
            activity.startActivity(DetailsActivity.navigate(activity, osmId))
        }
    }

    private fun Context.getActivity(): Activity? {
        return when (this) {
            is Activity -> this
            is ContextWrapper -> this.baseContext.getActivity()
            else -> null
        }
    }
}

