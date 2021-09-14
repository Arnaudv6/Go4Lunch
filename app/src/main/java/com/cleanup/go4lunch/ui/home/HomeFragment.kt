package com.cleanup.go4lunch.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.databinding.FragmentHomeBinding
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider

import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay




class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null
    private lateinit var map: MapView

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

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

        map = root.findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)

        map.setMultiTouchControls(true)

        val mapController = map.controller
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        mapController.setZoom(5.5)

        // add current position and center on it
        val mLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), map)
        mLocationOverlay.enableMyLocation()
        map.getOverlays().add(mLocationOverlay)
        var myLoc = mLocationOverlay.myLocation
        mapController.animateTo(myLoc)

        // add scale bar
        val mScaleBarOverlay = ScaleBarOverlay(map)
        mScaleBarOverlay.setAlignRight(true)
        mScaleBarOverlay.setAlignBottom(true)
        mScaleBarOverlay.setEnableAdjustLength(true)
        map.getOverlays().add(mScaleBarOverlay)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

