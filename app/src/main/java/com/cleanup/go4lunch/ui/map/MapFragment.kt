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
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.*


class MapFragment() : Fragment() {

    private lateinit var mMapViewModel: MapViewModel
    private lateinit var map: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay  // SimpleLocationOverlay is noop

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
        mMapViewModel = ViewModelProvider(this).get(MapViewModel::class.java)

        Configuration.getInstance().load(
            context,
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
        )
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID

        val view: View = inflater.inflate(R.layout.fragment_map, container, false)

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

//        GpsMyLocationProvider(context)

        val icon =
            ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_my_location_24, null)
                ?.toBitmap(64, 64)

        // if given a position, will be in direction mode
        locationOverlay = MyLocationNewOverlay(map)
        // todo consider setting location providers
        locationOverlay.disableFollowLocation()
        // locationOverlay.setPersonIcon(icon)
        locationOverlay.setDirectionArrow(icon, icon)
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
            TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_LTR
        )
        mScaleBarOverlay.setAlignBottom(true)
        mScaleBarOverlay.setEnableAdjustLength(true)
        map.overlays.add(mScaleBarOverlay)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        centerOnMe()
        val centerOnMeButton = view.findViewById<AppCompatImageButton>(R.id.center_on_me)
        centerOnMeButton.setOnClickListener { centerOnMe() }
    }

    private fun centerOnMe() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            suspend {
                do {
                    val loc: GeoPoint? = locationOverlay.myLocation
                    map.controller.animateTo(loc, 15.0, 1)
                    delay(500)
                } while (loc == null)
            }.invoke()
        }
    }

    override fun onResume() {
        super.onResume()
        locationOverlay.enableMyLocation()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        locationOverlay.enableMyLocation()
        map.onPause()
    }
}

