package com.cleanup.go4lunch.ui.map

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.text.TextUtilsCompat.getLayoutDirectionFromLocale
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.cleanup.go4lunch.BuildConfig
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.collectWithLifecycle
import com.cleanup.go4lunch.data.GpsProviderWrapper
import com.cleanup.go4lunch.exhaustive
import com.cleanup.go4lunch.ui.main.DetailsActivityLauncher
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
import org.osmdroid.views.overlay.CopyrightOverlay
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
    private lateinit var map: MapView
    private lateinit var mClickInterface: WeakReference<DetailsActivityLauncher>

    companion object {
        fun newInstance() = MapFragment()
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
        /* WIKIMEDIA map first appears white until map takes screen's height :/
            val isNightTheme = appContext.resources.configuration.uiMode
            and android.content.res.Configuration.UI_MODE_NIGHT_MASK
            Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_NIGHT_NO
        */

        // search if we can have shadow on drawn map canvas
        map.setMinZoomLevel(null) // null: use Tile Provider's value
        map.setMultiTouchControls(true)
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        map.isTilesScaledToDpi = true
        map.isVerticalMapRepetitionEnabled = false

        // loading grid colors
        map.overlayManager.tilesOverlay.loadingBackgroundColor =
            ContextCompat.getColor(requireContext(), R.color.colorSurface)
        map.overlayManager.tilesOverlay.loadingLineColor =
            ContextCompat.getColor(requireContext(), R.color.androidListDivider)

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

        gpsProviderWrapper.possibleLocation.collectWithLifecycle(viewLifecycleOwner) {
            locationOverlay.enableMyLocation()  // so location pin updates
        }
        // no need to de/activate location in onResume() and onPause(), given GPS throttling
        locationOverlay.enableMyLocation()  // so location pin updates
        locationOverlay.disableFollowLocation()  // so map does not follow
        locationOverlay.setPersonIcon(icon) // used when Location has no bearing
        locationOverlay.setDirectionArrow(icon, icon)  // when Location does have bearing
        map.overlays.add(locationOverlay)

        // copyright
        val copyrightOverlay = CopyrightOverlay(requireContext())
        map.overlays.add(copyrightOverlay)

        // POIs
        val poiMarkers = FolderOverlay()
        // if performance becomes an issue,
        // https://github.com/osmdroid/osmdroid/wiki/Markers,-Lines-and-Polygons-(Java)#fast-overlay
        // map.setOnClickListener { poiMarkers.closeAllInfoWindows() }
        map.overlays.add(0, poiMarkers)

        viewModel.viewStateLiveData.observe(viewLifecycleOwner) {
            poiMarkers.items?.clear()
            // shown marker must be closed and shown again to see new names applied
            for (pin in it.pinList) {
                val poiMarker = Marker(map).apply {
                    this.title = pin.name
                    this.snippet = pin.colleagues
                    this.position = pin.location
                    this.setPanToView(true)  // onClick, animate to map center?
                    this.setInfoWindow(  // null to disable
                        MyMarkerInfoWindow(
                            osmId = pin.id,
                            mapView = map,
                            detailsActivityLauncher = mClickInterface
                        )
                    )
                    this.icon =
                        ResourcesCompat.getDrawable(requireContext().resources, pin.icon, null)
                }
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
        val centerOnMeButton = view.findViewById<FloatingActionButton>(R.id.center_on_me)
        centerOnMeButton.setOnClickListener { viewModel.onCenterOnMeClicked() }

        // bind updatePoiPins button
        val updatePoiPins = view.findViewById<FloatingActionButton>(R.id.update_poi_pins)

        val progress = CircularProgressDrawable(requireContext()).apply {
            setStyle(CircularProgressDrawable.DEFAULT)
            setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.colorOnSecondary))
            // this won't show in emulator if animation scale is off.
        }

        val progressButton = view.findViewById<FloatingActionButton>(R.id.progress_circular)
        progressButton.setImageDrawable(progress)

        updatePoiPins.setOnClickListener {
            // or ExtendedFloatingActionButton, with properties icon, text and methods extend(), shrink()
            progressButton.visibility = View.VISIBLE
            progress.start()
            viewModel.requestPoiPins(map.boundingBox)
        }

        viewModel.viewActionLiveEvent.observe(viewLifecycleOwner) {
            when (it) {
                // null speed gets converted to default speed
                is MapViewAction.CenterOnMe -> map.controller.animateTo(it.geoPoint, 15.0, null)
                is MapViewAction.InitialBox -> {
                    Log.e("TAG", "onCreateView: reset")
                    // fix change theme makes a view reset. calls onStop(), even onDestroy()
                    //  bellow line indeed gets ran, but map is not in a state where it obeys
                    map.zoomToBoundingBox(it.boundingBox, true)
                }
                is MapViewAction.PoiRetrieval -> {
                    progress.stop()
                    progressButton.visibility = View.GONE
                    Snackbar.make(
                        view, "${it.results} POI received and updated on view",
                        Snackbar.LENGTH_SHORT
                    ).setAction("Dismiss") { /* empty action: dismiss.*/ }.show()
                }
                else -> Unit
            }.exhaustive
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onStop() {
        super.onStop()
        viewModel.onStop()
    }
}

