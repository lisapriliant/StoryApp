package com.lisapriliant.storyapp.ui.maps

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.lisapriliant.storyapp.databinding.ActivityMapsBinding
import com.lisapriliant.storyapp.R
import com.lisapriliant.storyapp.data.response.StoriesResponse
import com.lisapriliant.storyapp.ui.ViewModelFactory
import com.lisapriliant.storyapp.ui.showToastFromLiveData
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val mapsViewModel by viewModels<MapsViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private val boundsBuilder = LatLngBounds.Builder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getString(R.string.maps)

        mapsViewModel.isLoading.observe(this) {
            showLoading(it)
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.map_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.normal_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                showToastFromLiveData(mapsViewModel.toast)
                true
            }
            R.id.satellite_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                showToastFromLiveData(mapsViewModel.toast)
                true
            }
            R.id.terrain_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                showToastFromLiveData(mapsViewModel.toast)
                true
            }
            R.id.hybrid_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                showToastFromLiveData(mapsViewModel.toast)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        setStoriesLocation()
    }

    private fun setStoriesLocation() {
        mapsViewModel.getApiServiceWithToken().observe(this) { apiService ->
            lifecycleScope.launch {
                if (apiService != null) {
                    mapsViewModel.getStoriesLocation(apiService)
                } else {
                    showToastFromLiveData(mapsViewModel.toast)
                }
            }
        }

        mapsViewModel.storiesLocation.observe(this) { listStories ->
            addManyMarker(listStories)
        }
    }

    private fun addManyMarker(data: StoriesResponse) {
        data.listStory.forEach { location ->
            val latLng = LatLng(location.lat, location.lon)
            mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(location.name)
                    .snippet(location.description)
            )
            boundsBuilder.include(latLng)
        }
        mMap.moveCamera(
            CameraUpdateFactory.newLatLng(
                LatLng(-0.7893, 113.9213)
            )
        )
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
    }
}