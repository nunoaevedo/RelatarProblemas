package com.example.relatarproblemas.Map

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.relatarproblemas.Login.LoginActivity
import com.example.relatarproblemas.Notes.NotesActivity
import com.example.relatarproblemas.R
import com.example.relatarproblemas.Retrofit.APIRepository.APIRepository
import com.example.relatarproblemas.Retrofit.Type_Point.Type_Point
import com.example.relatarproblemas.Retrofit.ViewModel.RetrofitViewModel
import com.example.relatarproblemas.Retrofit.ViewModel.RetrofitViewModelFactory
import com.example.relatarproblemas.Settings.SettingsActivity
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.create_point.*
import kotlinx.android.synthetic.main.create_point.view.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

//    MAP
    private lateinit var mMap: GoogleMap

//    CURRENT LOCATION
    private lateinit var lastLocation : Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback : LocationCallback
    private lateinit var locationRequest : LocationRequest

//    RETROFIT
    private lateinit var viewModel :RetrofitViewModel

//    POINT TYPE LIST
    private lateinit var point_types : List<Type_Point>
    private lateinit var current_type : Type_Point

//    CURRENT LOGGED USER
    private var userId : Int = 0

    private val pickImage = 100
    private var imageUri: Uri? = null
    private lateinit var imagePlaceholder: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

//        UPDATE CAMERA TO FOLLOW USER
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                lastLocation = p0.lastLocation
                var loc = LatLng(lastLocation.latitude, lastLocation.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLng(loc))
            }
        }

        createLocationRequest()

//        BUILD LOGOUT BUTTON
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_logout)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

//        GET LOGGED USER ID
        val sharedPref = getSharedPreferences("loginInfo", Context.MODE_PRIVATE)
        userId = sharedPref.getInt("userId", 0)

        val repository = APIRepository()
        val viewModelFactory = RetrofitViewModelFactory(repository)

//        GET POINTS THROUGH RETROFIT
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

//        GET POINT TYPES THROUGH RETROFIT
        viewModel.getPointTypes()
        viewModel.typePointListResponse.observe(this, Observer { response ->
            if (response.isSuccessful) {
                point_types = response.body() as List<Type_Point>
            } else {
                Toast.makeText(this, "Error getting point types from API", Toast.LENGTH_SHORT).show()
            }
        })

//        ADD POINT
//        floatingActionButton.setOnClickListener {
//            Toast.makeText(this, "New point", Toast.LENGTH_SHORT).show()
//        }

    }

    fun createPoint(v : View) {
        val inflater = layoutInflater

        val inflate_view = inflater.inflate(R.layout.create_point, null)

        val problemDescriptionEdit = inflate_view.problem_description_edit
        val type_spinner = inflate_view.type_spinner
        imagePlaceholder = inflate_view.image_placeholder
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, point_types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        type_spinner.adapter = adapter

        type_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                current_type = adapter.getItem(position)!!
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Toast.makeText(applicationContext, getString(R.string.no_problem_type), Toast.LENGTH_SHORT).show()
            }

        }

        inflate_view.load_image.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, pickImage)
        }

        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Create new problem:")
        alertDialog.setView(inflate_view)
        alertDialog.setCancelable(true)
        
        alertDialog.setNegativeButton("Cancel") {_, _ ->
            Toast.makeText(this, getString(R.string.action_canceled), Toast.LENGTH_SHORT).show()
        }

        alertDialog.setPositiveButton("Create") {_, _ ->
            Toast.makeText(this, "create", Toast.LENGTH_SHORT).show()
        }
        val dialog = alertDialog.create()
        dialog.show()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data
            imagePlaceholder.setImageURI(imageUri)
        }
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)  != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 101)
            return
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.map_menu, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.note_menu -> {
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

        else -> super.onOptionsItemSelected(item)

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
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(41.69365972953475, -8.846491522141209), 5f))

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)  != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 101)
            return
        }

        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 10f))
            }
        }
    }




}