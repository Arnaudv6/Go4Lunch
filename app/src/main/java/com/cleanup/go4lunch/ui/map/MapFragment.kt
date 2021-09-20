package com.cleanup.go4lunch.ui.map

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.cleanup.go4lunch.BuildConfig
import com.cleanup.go4lunch.R
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


class MapFragment : Fragment() {

    private lateinit var mMapViewModel: MapViewModel
    private lateinit var map: MapView

    // This property is only valid between onCreateView and
    // onDestroyView.

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mMapViewModel = ViewModelProvider(this).get(MapViewModel::class.java)

        val view: View = inflater.inflate(R.layout.fragment_home, container, false)
        val context = requireActivity()

        //load/initialize the osmdroid configuration, this can be done
        // This won't work unless you have imported this: org.osmdroid.config.Configuration.*
        // Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, if you abuse osm's
        //tile servers will get you banned based on this string.
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID

        map = view.findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)

        map.setMultiTouchControls(true)

        val mapController = map.controller
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        mapController.setZoom(5.5)

        // add current position and center on it
        val mLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), map)

        val options = BitmapFactory.Options()
        options.outHeight = 32
        options.outWidth = 32

        val icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_my_location_24, null)?.toBitmap()
        // mLocationOverlay.setPersonIcon(icon)
        mLocationOverlay.setDirectionArrow(icon, icon)

        mLocationOverlay.enableMyLocation()
        mLocationOverlay.enableFollowLocation()

        map.getOverlays().add(mLocationOverlay)
        var myLoc = mLocationOverlay.myLocation
        mapController.animateTo(myLoc)

        // todo  il faut un bouton de centrage sur ma position

        // add scale bar
        val mScaleBarOverlay = ScaleBarOverlay(map)
        mScaleBarOverlay.setAlignRight(true)
        mScaleBarOverlay.setAlignBottom(true)
        mScaleBarOverlay.setEnableAdjustLength(true)
        map.getOverlays().add(mScaleBarOverlay)

        return view
    }

    override fun onResume() {
        super.onResume()
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume() //needed for compass, my location overlays, v6.0.0 and up
    }

    override fun onPause() {
        super.onPause()
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause()  //needed for compass, my location overlays, v6.0.0 and up
    }
}

