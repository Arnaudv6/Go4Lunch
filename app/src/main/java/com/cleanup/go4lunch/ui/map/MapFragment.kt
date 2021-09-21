package com.cleanup.go4lunch.ui.map

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.cleanup.go4lunch.BuildConfig
import com.cleanup.go4lunch.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import org.osmdroid.bonuspack.location.NominatimPOIProvider
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.TileSystem
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


class MapFragment() : Fragment() {

    private lateinit var mMapViewModel: MapViewModel
    private lateinit var map: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay  // SimpleLocationOverlay is noop

    fun newInstance(context: Context): MapFragment {
        // val args = Bundle()
        val fragment = MapFragment()
        // fragment.arguments = args
        return fragment
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mMapViewModel = ViewModelProvider(this).get(MapViewModel::class.java)

        val view: View = inflater.inflate(R.layout.fragment_home, container, false)

        Configuration.getInstance().load(
            context,
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
        )
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID

        map = view.findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        val mapController = map.controller
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

        map.isTilesScaledToDpi = true
        map.isVerticalMapRepetitionEnabled = false


        map.setScrollableAreaLimitLatitude(
            TileSystem.MaxLatitude,
            -TileSystem.MaxLatitude,
            0/*map.getHeight()/2*/
        )

//        GpsMyLocationProvider(context)


        // if given a position, will be in direction mode
        val icon =
            ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_my_location_24, null)
                ?.toBitmap(64, 64)

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

        // todo  il faut un bouton de centrage sur ma position

        // add scale bar
        val mScaleBarOverlay = ScaleBarOverlay(map)
        mScaleBarOverlay.setAlignRight(true)
        mScaleBarOverlay.setAlignBottom(true)
        mScaleBarOverlay.setEnableAdjustLength(true)
        map.overlays.add(mScaleBarOverlay)

        return view
    }

    suspend fun getLocationUpdates (): GeoPoint{
        delay(1000)
        return locationOverlay.myLocation
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val centerOnMe = view.findViewById<AppCompatImageButton>(R.id.center_on_me)

        var loc = GeoPoint(48.8583, 2.2944);

        viewLifecycleOwner.lifecycleScope.launchWhenStarted() {
            flow {
                emit(getLocationUpdates())
            }.collect {
                while (true) {
                    loc = it
                }
            }
        }

        centerOnMe.setOnClickListener(View.OnClickListener { v -> centerOnMe(loc) })

    }

    fun centerOnMe(myLocation: GeoPoint?) {
        if (myLocation != null)
            map.controller.animateTo(myLocation, 3.0, 2)
    }


    override fun onResume() {
        super.onResume()
        locationOverlay.enableMyLocation()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        locationOverlay.enableMyLocation()
        map.onPause()  //needed for compass, my location overlays, v6.0.0 and up
    }
}

