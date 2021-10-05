package com.cleanup.go4lunch.ui.map

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.text.TextUtilsCompat.getLayoutDirectionFromLocale
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.cleanup.go4lunch.BuildConfig
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.collectWithLifecycle
import com.cleanup.go4lunch.data.GpsProviderWrapper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.TileSystem
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Locale.getDefault
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MapFragment : Fragment() {
    @Inject
    lateinit var gpsProviderWrapper: GpsProviderWrapper
    private val viewModel: MapViewModel by viewModels()
    private lateinit var map: MapView
    private var icon: Drawable? = null

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
        icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_location_on_24, null)

        // map settings
        map = view.findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        map.isTilesScaledToDpi = true
        map.isVerticalMapRepetitionEnabled = false

        @Suppress("DEPRECATION") // This is just because of bad naming for this CONSTANT
        map.setScrollableAreaLimitLatitude(
            TileSystem.MaxLatitude,
            -TileSystem.MaxLatitude,
            0
        )

        viewModel.viewActionFlow.collectWithLifecycle(viewLifecycleOwner) {
            when (it) {
                is MapViewAction.CenterOnMe -> map.controller.animateTo(it.geoPoint, 15.0, 1)
                is MapViewAction.InitialMapBox -> map.zoomToBoundingBox(it.boundingBox, true)
            }
        }

        map.addMapListener(object : MapListener {
            override fun onScroll(event: ScrollEvent?): Boolean {
                viewModel.mapBoxChanged(map.boundingBox)
                return true
            }

            override fun onZoom(event: ZoomEvent?): Boolean {
                viewModel.mapBoxChanged(map.boundingBox)
                return true
            }
        })

        // display user location on map
        val icon =
            ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_my_location_24, null)
                ?.toBitmap(64, 64)
        // SimpleLocationOverlay is noop
        val locationOverlay = MyLocationNewOverlay(gpsProviderWrapper, map)

        // no need to de/activate location in onResume() and onPause(), given GPS throttling
        locationOverlay.enableMyLocation()  // so location pin updates
        locationOverlay.disableFollowLocation()  // so map does not follow
        locationOverlay.setPersonIcon(icon) // used when Location has no bearing
        locationOverlay.setDirectionArrow(icon, icon)  // when Location does have bearing
        map.overlays.add(locationOverlay)

        // POIs
        val poiMarkers = FolderOverlay()
        // map.setOnClickListener { poiMarkers.closeAllInfoWindows() }
        map.overlays.add(poiMarkers)
        viewModel.pointOfInterestListStateFlow.collectWithLifecycle(viewLifecycleOwner) {
            poiMarkers.items.clear()
            for (poi in it) {
                addPinOnLayer(
                    poiMarkers,
                    poi.mType,
                    poi.mDescription,
                    poi.mLocation
                )
            }
            map.postInvalidate()  // force a redraw
        }

        // add scale bar
        val mScaleBarOverlay = ScaleBarOverlay(map)
        val ltr = getLayoutDirectionFromLocale(getDefault()) == ViewCompat.LAYOUT_DIRECTION_LTR
        mScaleBarOverlay.setAlignRight(ltr)
        mScaleBarOverlay.setAlignBottom(true)
        mScaleBarOverlay.setEnableAdjustLength(true)
        map.overlays.add(mScaleBarOverlay)

        // bind centerOnMe button
        val centerOnMeButton = view.findViewById<AppCompatImageButton>(R.id.center_on_me)
        centerOnMeButton.setOnClickListener { viewModel.onCenterOnMeClicked() }

        return view
    }

    private fun addPinOnLayer(
        layer: FolderOverlay,
        title: String,
        description: String,
        location: GeoPoint,
    ) {
        val poiMarker = Marker(map)
        poiMarker.title = title
        poiMarker.snippet = description
        poiMarker.position = location
        poiMarker.icon = icon
        layer.add(poiMarker)
    }
}

