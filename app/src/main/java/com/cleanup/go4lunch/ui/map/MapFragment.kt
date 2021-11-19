package com.cleanup.go4lunch.ui.map

import android.content.Context
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
import com.cleanup.go4lunch.exhaustive
import com.cleanup.go4lunch.ui.main.DetailsActivityLauncher
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.TileSystem
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.lang.ref.WeakReference
import java.util.Locale.getDefault
import javax.inject.Inject

@AndroidEntryPoint
class MapFragment : Fragment() {
    @Inject
    lateinit var gpsProviderWrapper: GpsProviderWrapper
    private val viewModel: MapViewModel by viewModels()
    private lateinit var map: MapView // init in onCreateView, not constructor...
    private lateinit var mClickInterface: WeakReference<DetailsActivityLauncher>

    companion object {
        fun newInstance(): MapFragment {
            return MapFragment()
        }
    }

    // onAttach() gives us the context for free
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mClickInterface = WeakReference(context as DetailsActivityLauncher)
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
        map.setDestroyMode(false)  // https://github.com/osmdroid/osmdroid/issues/277
        map.setTileSource(TileSourceFactory.MAPNIK)
        // WIKIMEDIA map first appears white until map takes screen's height :/

        map.setMinZoomLevel(null) // null: use Tile Provider's value
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

        // todo make it livedata? put some of this in VM?
        gpsProviderWrapper.possibleLocation.collectWithLifecycle(viewLifecycleOwner) {
            locationOverlay.enableMyLocation()  // so location pin updates
        }
        // no need to de/activate location in onResume() and onPause(), given GPS throttling
        locationOverlay.enableMyLocation()  // so location pin updates
        locationOverlay.disableFollowLocation()  // so map does not follow
        locationOverlay.setPersonIcon(icon) // used when Location has no bearing
        locationOverlay.setDirectionArrow(icon, icon)  // when Location does have bearing
        map.overlays.add(locationOverlay)

        // POIs
        val poiMarkers = FolderOverlay()
        // if performance becomes an issue,
        // https://github.com/osmdroid/osmdroid/wiki/Markers,-Lines-and-Polygons-(Java)#fast-overlay
        // map.setOnClickListener { poiMarkers.closeAllInfoWindows() }
        map.overlays.add(0, poiMarkers)

        viewModel.viewStateLiveData.observe(viewLifecycleOwner) {
            poiMarkers.items?.clear()
            for (pin in it.pinList) {
                val poiMarker = Marker(map)
                poiMarker.title = pin.name
                poiMarker.snippet = pin.colleagues
                poiMarker.position = pin.location
                poiMarker.setPanToView(true)  // onClick, animate to map center?
                poiMarker.setInfoWindow(
                    MyMarkerInfoWindow(
                        osmId = pin.id,
                        mapView = map,
                        detailsActivityLauncher = mClickInterface
                    )
                )  // null to disable
                poiMarker.icon =
                    ResourcesCompat.getDrawable(requireContext().resources, pin.icon, null)
                // don't inject appContext here in Fragment : we're not injecting for instrumented tests.
                // don't worry about the loop: getDrawable is cached by Android
                poiMarkers.add(poiMarker)
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

        // bind updatePoiPins button
        val updatePoiPins = view.findViewById<AppCompatImageButton>(R.id.update_poi_pins)
        updatePoiPins.setOnClickListener { viewModel.requestPoiPins(map.boundingBox) }
        // todo use swipe-to-refresh busy spindle.
        // map.addMapListener(object : MapListener {    override onScroll() and onZoom()    })

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.viewActionLiveEvent.observe(viewLifecycleOwner) {
            when (it) {
                // animations are stub as of OSM-Droid 6.1.11
                is MapViewAction.CenterOnMe -> map.controller.animateTo(it.geoPoint, 15.0, 1)
                is MapViewAction.InitialBox -> map.zoomToBoundingBox(it.boundingBox, false)
                // todo: fix change theme makes a view reset
                is MapViewAction.PoiRetrieval -> Snackbar
                    .make(
                        view,
                        "${it.results} POI received and updated on view",
                        Snackbar.LENGTH_SHORT
                    )
                    .setAction("Dismiss") {}.show() // empty action will dismiss.
                else -> Unit // better notation than {}
            }.exhaustive
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.onStop()
    }
}

