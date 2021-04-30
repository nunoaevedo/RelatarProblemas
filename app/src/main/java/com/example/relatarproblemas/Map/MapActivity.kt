    package com.example.relatarproblemas.Map

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.FileUtils
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.relatarproblemas.Login.LoginActivity
import com.example.relatarproblemas.Notes.NotesActivity
import com.example.relatarproblemas.R
import com.example.relatarproblemas.Retrofit.APIRepository.APIRepository
import com.example.relatarproblemas.Retrofit.Point.Point
import com.example.relatarproblemas.Retrofit.Point.PointUpdate
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
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.create_point.*
import kotlinx.android.synthetic.main.create_point.view.*
import kotlinx.android.synthetic.main.create_point.view.problem_description_edit
import kotlinx.android.synthetic.main.create_point.view.type_spinner
import kotlinx.android.synthetic.main.point_info.view.*
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.*


class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, CompoundButton.OnCheckedChangeListener {

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
//    private lateinit var point_types : List<Type_Point>
    private lateinit var current_type : String
    private var type_filter : MutableList<String> = ArrayList()

//    CURRENT LOGGED USER
    private var userId : Int = 0

    private val pickImage = 100
    private var imageUri: Uri? = null

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
                val loc = LatLng(lastLocation.latitude, lastLocation.longitude)
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

        blocked_road_toggle.setOnCheckedChangeListener(this)
        fallen_tree_toggle.setOnCheckedChangeListener(this)
        dead_animal_toggle.setOnCheckedChangeListener(this)
        slippery_toggle.setOnCheckedChangeListener(this)


    }

    fun createPoint(v: View) {
        val inflater = layoutInflater

        val inflate_view = inflater.inflate(R.layout.create_point, null)

        val problemDescriptionEdit = inflate_view.problem_description_edit
        val type_spinner = inflate_view.type_spinner
//        imagePlaceholder = inflate_view.image_placeholder
//        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, point_types)
//        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, R.array.type_array)
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        type_spinner.adapter = adapter

        val adapter = ArrayAdapter.createFromResource(
                this,
                R.array.type_array,
                android.R.layout.simple_spinner_item
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        type_spinner.adapter = adapter



        type_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                current_type = adapter.getItem(position).toString()!!
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
        
        alertDialog.setNegativeButton("Cancel") { _, _ ->
            Toast.makeText(this, getString(R.string.action_canceled), Toast.LENGTH_SHORT).show()
        }

        alertDialog.setPositiveButton("Create", null)


        val dialog = alertDialog.create()
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (problemDescriptionEdit.text.isBlank()){
                Toast.makeText(applicationContext, "please fill out description", Toast.LENGTH_SHORT).show()
            }
            else {
                val comment = problemDescriptionEdit.text
                if (imageUri != null){
//                    val point = Point(0, comment.toString(), Date(), lastLocation.latitude, lastLocation.longitude, userId, current_type.id, "")

                    val file = File(imageUri!!.path?.split(":")?.get(1))
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
//                    val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
//                    val requestFile = RequestBody.create(contentResolver.getType(imageUri!!)!!.toMediaTypeOrNull(), file)
                    val body = MultipartBody.Part.createFormData("image", file.name, requestFile)
                    


                    viewModel.newPointImage(body, comment.toString(), lastLocation.latitude, lastLocation.longitude, userId, current_type)
                    viewModel.pointResponse.observe(this, Observer { response ->
                        if (response.isSuccessful) {
                            if (response.body() != null) {
                                val position = LatLng(response.body()!!.latitude, response.body()!!.longitude)
                                val marker = mMap.addMarker(MarkerOptions()
                                        .position(position)
                                        .title(response.body()!!.comment)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))
                                marker.tag = response.body()!!.id
                                Toast.makeText(this, getString(R.string.point_creted), Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            } else {
                                Toast.makeText(this, getString(R.string.problem_create_point), Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this, response.code(), Toast.LENGTH_SHORT).show()
                        }
                    })
                }else{
                    val newPoint = Point(0, comment.toString(), Date(), lastLocation.latitude, lastLocation.longitude, userId, current_type, "")
                    viewModel.newPoint(newPoint)
                    viewModel.pointResponse.observe(this, Observer { response ->
                        if (response.isSuccessful) {
                            if (response.body() != null) {
                                val position = LatLng(response.body()!!.latitude, response.body()!!.longitude)
                                val marker = mMap.addMarker(MarkerOptions()
                                        .position(position)
                                        .title(response.body()!!.comment)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))
                                marker.tag = response.body()!!.id
                                Toast.makeText(this, getString(R.string.point_creted), Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            } else {
                                Toast.makeText(this, getString(R.string.problem_create_point), Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this, response.code(), Toast.LENGTH_SHORT).show()
                        }
                    })
                }

            }

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data
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
//        intent.putExtra("types", point_types as Serializable)
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

//        locationRequest
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 10f))
            }
        }

        viewModel.getPoints()
        viewModel.pointListResponse.observe(this, Observer { response ->
            if (response.isSuccessful) {
                if (response.body() != null) {
                    response.body()!!.forEach {
                        val position = LatLng(it.latitude, it.longitude)
                        if (it.user_id == userId) {
                            val marker = mMap.addMarker(MarkerOptions().position(position).title(it.comment).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))
                            marker.tag = it.id
                        } else {
                            val marker = mMap.addMarker(MarkerOptions().position(position).title(it.comment))
                            marker.tag = it.id

                        }


                    }
                }
            } else {
                Log.d("Response", response.errorBody().toString())
            }

        })


        mMap.setOnMarkerClickListener(this)

    }

    override fun onMarkerClick(p0: Marker?): Boolean {

        viewModel.getPointById(p0?.tag as Int)
        viewModel.pointResponse.observe(this, Observer { response ->
            if (response.isSuccessful) {
                if (response.body() != null) {
                    val point = response.body()
                    pointDialog(point!!, p0)
                } else {
                    Toast.makeText(this, getString(R.string.point_doesnt_exist), Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, response.code(), Toast.LENGTH_SHORT).show()
            }
        })


        return false
    }

    fun calculateDistance(curLat: Double, curLng: Double, pointLat: Double, pointLng: Double) : Float {
        val results = FloatArray(1)
        Location.distanceBetween(curLat, curLng, pointLat, pointLng, results)
        return results[0]
    }

    fun pointDialog(point: Point, marker: Marker) {

        val inflater = layoutInflater

        val inflate_view = inflater.inflate(R.layout.point_info, null)

        val problemDescriptionEdit = inflate_view.problem_description_edit
        problemDescriptionEdit.setText(point.comment)

        val distance_text = inflate_view.distance_text
        distance_text.text = (calculateDistance(lastLocation.latitude, lastLocation.longitude, point.latitude, point.longitude)/1000).toString() + " KM"

        val type_spinner = inflate_view.type_spinner
//        imagePlaceholder = inflate_view.image_placeholder
//        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, point_types)
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        type_spinner.adapter = adapter

        val adapter = ArrayAdapter.createFromResource(
                this,
                R.array.type_array,
                android.R.layout.simple_spinner_item
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        type_spinner.adapter = adapter
        type_spinner.setSelection(adapter.getPosition(point.type))

        type_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                current_type = adapter.getItem(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Toast.makeText(applicationContext, getString(R.string.no_problem_type), Toast.LENGTH_SHORT).show()
            }

        }

//        inflate_view.load_image.setOnClickListener {
//            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
//            startActivityForResult(gallery, pickImage)
//        }

        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Problem:")
        alertDialog.setView(inflate_view)
        alertDialog.setCancelable(true)

        if(point.user_id != userId){
            problemDescriptionEdit.isEnabled = false
            type_spinner.isEnabled = false
        }
        else{
            alertDialog.setNeutralButton("Delete", null)
            alertDialog.setPositiveButton("Update", null)
        }

        alertDialog.setNegativeButton("Cancel") { _, _ ->
            Toast.makeText(this, getString(R.string.action_canceled), Toast.LENGTH_SHORT).show()
        }

        val dialog = alertDialog.create()
        dialog.show()



        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
            if(point.user_id == userId){
                viewModel.deletePoint(point.id)
                viewModel.stringResponse.observe(this, Observer { response ->
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            Toast.makeText(this, response.body(), Toast.LENGTH_SHORT).show()
                            marker.remove()
                            dialog.dismiss()
                        } else {
                            Toast.makeText(this, getString(R.string.point_doesnt_exist), Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, response.code(), Toast.LENGTH_SHORT).show()
                    }
                })
            }else{
                Toast.makeText(this, "You can't delete this point!", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (point.user_id == userId){
                val comment = problemDescriptionEdit.text
                val point_update = PointUpdate(comment.toString(), current_type)

                viewModel.updatePoint(point.id, point_update)
                viewModel.pointResponse.observe(this, Observer { response ->
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            Toast.makeText(this, "Point Updated", Toast.LENGTH_SHORT).show()
                            marker.title = response.body()!!.comment
                            dialog.dismiss()
                        } else {
                            Toast.makeText(this, getString(R.string.point_doesnt_exist), Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, response.code(), Toast.LENGTH_SHORT).show()
                    }
                })
            }
            else{
                Toast.makeText(this, "You can't update this point!", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if(isChecked) type_filter.add(buttonView?.text.toString())
        else type_filter.remove(buttonView?.text.toString())

        mMap.clear()

        viewModel.pointListResponse.observe(this, Observer { response ->
            if (response.isSuccessful) {
                if (response.body() != null) {
                    response.body()!!.forEach {
                        if (type_filter.contains(it.type) || type_filter.isEmpty()) {
                            val position = LatLng(it.latitude, it.longitude)
                            if (it.user_id == userId) {
                                val marker = mMap.addMarker(MarkerOptions().position(position).title(it.comment).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))
                                marker.tag = it.id
                            } else {
                                val marker = mMap.addMarker(MarkerOptions().position(position).title(it.comment))
                                marker.tag = it.id
                            }
                        }
                    }
                }
            } else {
                Log.d("Response", response.errorBody().toString())
            }
        })

    }


}