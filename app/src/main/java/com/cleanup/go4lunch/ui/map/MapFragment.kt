package com.cleanup.go4lunch.ui.map

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.cleanup.go4lunch.BuildConfig
import com.cleanup.go4lunch.R
import kotlinx.coroutines.delay
import org.osmdroid.bonuspack.location.NominatimPOIProvider
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.TileSystem
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.*


class MapFragment() : Fragment() {

    private lateinit var viewModel: MapViewModel
    private lateinit var map: MapView
    private lateinit var gps: GpsMyLocationProvider

    companion object {
        fun newInstance(context: Context): MapFragment {
            // val args = Bundle()
            val fragment = MapFragment()
            // fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // todo ViewModel Factory
        viewModel = ViewModelProvider(this).get(MapViewModel::class.java)

        // set user agent and map-cache
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        Configuration.getInstance().load(
            context,
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
        )

        val view: View = inflater.inflate(R.layout.fragment_map, container, false)

        // map settings
        map = view.findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        map.isTilesScaledToDpi = true
        map.isVerticalMapRepetitionEnabled = false
        map.setScrollableAreaLimitLatitude(
            TileSystem.MaxLatitude,
            -TileSystem.MaxLatitude,
            0/*map.getHeight()/2*/
        )

        map.controller.setZoom(4.0)
        // var loc = GeoPoint(48.8583, 2.2944)

        // todo this ultimately belongs in activity, if list-fragment is to update on its own.
        // set location updates throttling, and subscribe to new locations
        gps = GpsMyLocationProvider(context)
        gps.locationUpdateMinDistance = 10F  // float, meters
        gps.locationUpdateMinTime = 5000 // long, milliseconds
        gps.startLocationProvider({ location, source -> viewModel.updateLocation(location) })

        val icon =
            ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_my_location_24, null)
                ?.toBitmap(64, 64)

        // if given a position, will be in direction mode
        val locationOverlay = MyLocationNewOverlay(gps, map)  // SimpleLocationOverlay is noop
        // no need to de/activate location in onResume() and onPause(), given above GPS throttling
        locationOverlay.enableMyLocation()  // so location pin updates
        locationOverlay.disableFollowLocation()  // so map does not follow
        locationOverlay.setPersonIcon(icon) // used when Location has no bearing
        locationOverlay.setDirectionArrow(icon, icon)  // when Location does have bearing
        map.overlays.add(locationOverlay)

        val poiProvider = NominatimPOIProvider(BuildConfig.APPLICATION_ID)
        /* val pois = poiProvider.getPOICloseTo(
                when (myLoc) {
                    null -> GeoPoint(48.8583, 2.2944)
                    else -> myLoc
                }, "restaurant", 50, 0.1
        )
        val poiMarkers = FolderOverlay()
        map.overlays.add(poiMarkers)

        val poiIcon =
                ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_location_on_24, null)
        for (poi in pois) {
            val poiMarker = Marker(map)
            poiMarker.title = poi.mType
            poiMarker.snippet = poi.mDescription
            poiMarker.position = poi.mLocation
            poiMarker.icon = poiIcon
            poiMarkers.add(poiMarker)
        }
*/

        // add scale bar
        val mScaleBarOverlay = ScaleBarOverlay(map)
        mScaleBarOverlay.setAlignRight(
            TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault())
                    == ViewCompat.LAYOUT_DIRECTION_LTR
        )
        mScaleBarOverlay.setAlignBottom(true)
        mScaleBarOverlay.setEnableAdjustLength(true)
        map.overlays.add(mScaleBarOverlay)

        // bind centerOnMe button
        val centerOnMeButton = view.findViewById<AppCompatImageButton>(R.id.center_on_me)
        centerOnMeButton.setOnClickListener { centerOnMe() }

        // wait for location fix to center the map
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            suspend {
                while (gps.lastKnownLocation == null) {
                    delay(700)
                }
                centerOnMe()
            }.invoke()
        }

        return view
    }

    private fun centerOnMe() {
        val loc: GeoPoint = GeoPoint(gps.lastKnownLocation)
        map.controller.animateTo(loc, 15.0, 1)
    }
}

