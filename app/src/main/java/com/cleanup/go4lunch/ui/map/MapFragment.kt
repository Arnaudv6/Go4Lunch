package com.cleanup.go4lunch.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.TextUtilsCompat.getLayoutDirectionFromLocale
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.cleanup.go4lunch.BuildConfig
import com.cleanup.go4lunch.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.TileSystem
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.ScaleBarOverlay
import java.util.Locale.getDefault

@AndroidEntryPoint
class MapFragment : Fragment() {

    /*
    @Inject
    @Singleton
    lateinit var gps: GpsMyLocationProvider
    */

    private val viewModel: MapViewModel by viewModels()
    private lateinit var map: MapView

    companion object {
        fun newInstance(): MapFragment {
            return MapFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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


        /*
        // if given a position, will be in direction mode
        val locationOverlay = MyLocationNewOverlay(gps, map)  // SimpleLocationOverlay is noop
        // no need to de/activate location in onResume() and onPause(), given above GPS throttling
        locationOverlay.enableMyLocation()  // so location pin updates
        locationOverlay.disableFollowLocation()  // so map does not follow
        locationOverlay.setPersonIcon(icon) // used when Location has no bearing
        locationOverlay.setDirectionArrow(icon, icon)  // when Location does have bearing
        map.overlays.add(locationOverlay)
        */


        // POIs
        val poiMarkers = FolderOverlay()
        map.overlays.add(poiMarkers)

        // todo uncomment block above and remove that crappy optim as soon as I inject GPS
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.getLocation().collect {
                poiMarkers.items.clear()
                addPinOnLayer(
                    poiMarkers,
                    "My position",
                    "as seen by last gps fix",
                    it,
                    R.drawable.ic_baseline_my_location_24
                )
                map.postInvalidate()  // force a redraw
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.getPointsOfInterest().collect {
                for (poi in it) {
                    addPinOnLayer(
                        poiMarkers,
                        poi.mType,
                        poi.mDescription,
                        poi.mLocation,
                        R.drawable.ic_baseline_location_on_24
                    )
                }
                map.postInvalidate()  // force a redraw
            }
        }

        // map.setOnClickListener { poiMarkers.closeAllInfoWindows() }

        // add scale bar
        val mScaleBarOverlay = ScaleBarOverlay(map)
        val ltr = getLayoutDirectionFromLocale(getDefault()) == ViewCompat.LAYOUT_DIRECTION_LTR
        mScaleBarOverlay.setAlignRight(ltr)
        mScaleBarOverlay.setAlignBottom(true)
        mScaleBarOverlay.setEnableAdjustLength(true)
        map.overlays.add(mScaleBarOverlay)

        // bind centerOnMe button
        val centerOnMeButton = view.findViewById<AppCompatImageButton>(R.id.center_on_me)
        centerOnMeButton.setOnClickListener { centerOnMe() }

        /*
        // wait for location fix to center the map
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            suspend {
                while (gps.lastKnownLocation == null) {
                    delay(700)
                }
                centerOnMe()
            }.invoke()
        }
        */

        return view
    }

    private fun centerOnMe() {
        map.controller.animateTo(viewModel.getLocation().last(), 15.0, 1)
    }

    private fun addPinOnLayer(
        layer: FolderOverlay,
        title: String,
        description: String,
        location: GeoPoint,
        @DrawableRes icon: Int
    ) {
        val poiMarker = Marker(map)
        poiMarker.title = title
        poiMarker.snippet = description
        poiMarker.position = location
        poiMarker.icon = ResourcesCompat.getDrawable(resources, icon, null)
        layer.add(poiMarker)
    }
}

