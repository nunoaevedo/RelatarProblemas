package com.example.relatarproblemas.Map

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.relatarproblemas.Login.LoginActivity
import com.example.relatarproblemas.Notes.NotesActivity
import com.example.relatarproblemas.R
import com.example.relatarproblemas.Retrofit.APIRepository.APIRepository
import com.example.relatarproblemas.Retrofit.ViewModel.RetrofitViewModel
import com.example.relatarproblemas.Retrofit.ViewModel.RetrofitViewModelFactory
import com.example.relatarproblemas.Settings.SettingsActivity

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlin.properties.Delegates

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

//    private lateinit var locationPermissionGranted

    private lateinit var viewModel :RetrofitViewModel

    private var userId : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_logout)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        // Construct a PlacesClient
//        Places.initialize(applicationContext, getString(R.string.maps_api_key))
//        placesClient = Places.createClient(this)
//
//        // Construct a FusedLocationProviderClient.
//        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val sharedPref = getSharedPreferences("loginInfo", Context.MODE_PRIVATE)
        userId = sharedPref.getInt("userId", 0)

        val repository = APIRepository()
        val viewModelFactory = RetrofitViewModelFactory(repository)

        viewModel = ViewModelProvider(this, viewModelFactory).get(RetrofitViewModel::class.java)
        viewModel.getPoints()
        viewModel.pointListResponse.observe(this, Observer { response ->
            if (response.isSuccessful) {
                if (response.body() != null){
                    response.body()!!.forEach {
                        Log.d("Response Point ID: ", it.id.toString())
                        Log.d("Response Comment: ", it.comment)
                        Log.d("Response Date: ", it.date.toString())
                        Log.d("Response Longitude: ", it.longitude.toString())
                        Log.d("Response Latitude: ", it.latitude.toString())
                        Log.d("Response User ID: ", it.user_id.toString())
                        Log.d("Response Type ID: ", it.type_id.toString())

                        val position = LatLng(it.latitude.toDouble(), it.longitude.toDouble())

                        if (it.user_id == userId)
                            mMap.addMarker(MarkerOptions().position(position).title(it.comment).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))
                        else
                            mMap.addMarker(MarkerOptions().position(position).title(it.comment))

                    }
                }
            } else {
                Log.d("Response", response.errorBody().toString())
            }

        })




    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.map_menu, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.note_menu -> {
            // User chose the "Settings" item, show the app settings UI...
            toNotesActivity()
            true
        }

        android.R.id.home -> {
            logout()
            true
        }

        R.id.settings_menu -> {
            toSettingsActivity()
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    private fun toSettingsActivity(){
        val intent = Intent(this@MapActivity, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun toNotesActivity(){
        val intent = Intent(this@MapActivity, NotesActivity::class.java)
        startActivity(intent)
    }


    private fun logout(){
        val sharedPrefEdit = getSharedPreferences("loginInfo", Context.MODE_PRIVATE).edit()
        sharedPrefEdit.putInt("userId", 0)
        sharedPrefEdit.apply()
        toLoginActivity()
    }


    private fun toLoginActivity() {
        val intent = Intent(this@MapActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
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

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(41.457099754424235, -8.56223810309999)
//        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 10f))
    }


//    private fun getLocationPermission() {
//        /**
//         * Request location permission, so that we can get the location of the
//         * device. The result of the permission request is handled by a callback,
//         * onRequestPermissionsResult.
//         */
//        if (ContextCompat.checkSelfPermission(this.applicationContext,
//                        Manifest.permission.ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED) {
//            locationPermissionGranted = true
//        } else {
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
//        }
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int,
//                                            permissions: Array<String>,
//                                            grantResults: IntArray) {
//        locationPermissionGranted = false
//        when (requestCode) {
//            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
//
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.isNotEmpty() &&
//                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    locationPermissionGranted = true
//                }
//            }
//        }
//        updateLocationUI()
//    }





}