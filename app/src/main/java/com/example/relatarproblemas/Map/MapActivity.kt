package com.example.relatarproblemas.Map

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.relatarproblemas.Login.LoginActivity
import com.example.relatarproblemas.Notes.NotesActivity
import com.example.relatarproblemas.R
import com.example.relatarproblemas.Retrofit.API.RetrofitInstance
import com.example.relatarproblemas.Retrofit.API.SlimAPI
import com.example.relatarproblemas.Retrofit.APIRepository.APIRepository
import com.example.relatarproblemas.Retrofit.Constants.Companion.LOCAL_URL
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
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.create_point.*
import kotlinx.android.synthetic.main.create_point.view.*
import kotlinx.android.synthetic.main.create_point.view.problem_description_edit
import kotlinx.android.synthetic.main.create_point.view.type_spinner
import kotlinx.android.synthetic.main.point_info.view.*
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Response
import java.io.File
import java.util.*


class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, CompoundButton.OnCheckedChangeListener, SensorEventListener, GoogleMap.OnMapLongClickListener {

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
//    private var imageUri: Uri? = null

//    LIGHT SENSOR
    private lateinit var sensorManager: SensorManager

//    LIGHT SENSOR
    private var brightness : Sensor? = null
//    COMPASS SENSOR
    private var sensorAccelerometer: Sensor? = null
    private var sensorMagneticField: Sensor? = null

    private var floatGravity = FloatArray(3)
    private var floatGeoMagnetic = FloatArray(3)

    private val floatOrientation = FloatArray(3)
    private val floatRotationMatrix = FloatArray(9)

//    GEOFENCE
    private var circle: Circle? = null
    private lateinit var geofencingClient : GeofencingClient

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

        geofencingClient = LocationServices.getGeofencingClient(applicationContext)

        createLocationRequest()

//        BUILD LOGOUT BUTTON
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_logout)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

//        GET LOGGED USER ID
        val sharedPref = getSharedPreferences(getString(R.string.login_key), Context.MODE_PRIVATE)
        userId = sharedPref.getInt(getString(R.string.user_login_key), 0)

        val repository = APIRepository()
        val viewModelFactory = RetrofitViewModelFactory(repository)

//        GET POINTS THROUGH RETROFIT
        viewModel = ViewModelProvider(this, viewModelFactory).get(RetrofitViewModel::class.java)

        blocked_road_toggle.setOnCheckedChangeListener(this)
        fallen_tree_toggle.setOnCheckedChangeListener(this)
        dead_animal_toggle.setOnCheckedChangeListener(this)
        slippery_toggle.setOnCheckedChangeListener(this)

        setUpSensors()

    }

    private fun setUpSensors() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        brightness = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    fun createPoint(v: View) {
        val inflater = layoutInflater

        val inflate_view = inflater.inflate(R.layout.create_point, null)

        val problemDescriptionEdit = inflate_view.problem_description_edit
        val type_spinner = inflate_view.type_spinner

        viewModel.imageUri.observe(this, Observer {
            inflate_view.image_placeholder.setImageURI(it)
        })


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
        alertDialog.setTitle(getString(R.string.create_new_problem))
        alertDialog.setView(inflate_view)
        alertDialog.setCancelable(false)
        
        alertDialog.setNegativeButton(getString(R.string.cancel)) { _, _ ->
            Toast.makeText(this, getString(R.string.action_canceled), Toast.LENGTH_SHORT).show()
            viewModel.removeUri()
            viewModel.imageUri.removeObservers(this)
        }

        alertDialog.setPositiveButton(getString(R.string.create), null)

        val dialog = alertDialog.create()
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (problemDescriptionEdit.text.isBlank()){
                Toast.makeText(applicationContext, getString(R.string.fill_description), Toast.LENGTH_SHORT).show()
            }
            else {
                val comment = problemDescriptionEdit.text
                if (viewModel.imageUri.value != null){

                    val imageUri :Uri  = viewModel.imageUri.value!!

                    val cursor: Cursor? = this.contentResolver.query(imageUri!!, arrayOf(MediaStore.Images.ImageColumns.DATA), null, null, null)
                    cursor?.moveToFirst()
                    val filePath = cursor?.getString(0)

                    val file  = File(filePath)
                    val requestFile = file.asRequestBody("image".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("photo", file.name, requestFile)

                    val commentBody = comment.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val typeBody = current_type.toRequestBody("text/plain".toMediaTypeOrNull())

                    viewModel.newPointImage(body, commentBody, lastLocation.latitude, lastLocation.longitude, userId, typeBody)
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
                                viewModel.removeUri()
                                viewModel.imageUri.removeObservers(this)
                                dialog.dismiss()
                            } else {
                                Toast.makeText(this, getString(R.string.problem_create_point), Toast.LENGTH_SHORT).show()
                                viewModel.removeUri()
                                viewModel.imageUri.removeObservers(this)
                            }
                        } else {
                            Toast.makeText(this, response.code(), Toast.LENGTH_SHORT).show()
                            viewModel.removeUri()
                            viewModel.imageUri.removeObservers(this)
                        }
                    })
                }else{
                    val commentBody = comment.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val typeBody = current_type.toRequestBody("text/plain".toMediaTypeOrNull())

                    val request = RetrofitInstance.buildService(SlimAPI::class.java)
                    val call = request.newPoint(commentBody, lastLocation.latitude, lastLocation.longitude, userId, typeBody)

                    call.enqueue(object : retrofit2.Callback<Point> {
                        override fun onResponse(call: Call<Point>, response: Response<Point>) {
                            if (response.body() != null) {
                                val position = LatLng(response.body()!!.latitude, response.body()!!.longitude)
                                val marker = mMap.addMarker(MarkerOptions()
                                        .position(position)
                                        .title(response.body()!!.comment)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))
                                marker.tag = response.body()!!.id
                                Toast.makeText(this@MapActivity, getString(R.string.point_creted), Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            } else {
                                Toast.makeText(this@MapActivity, getString(R.string.problem_create_point), Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<Point>, t: Throwable) {
                            Toast.makeText(this@MapActivity, t.message, Toast.LENGTH_SHORT).show()
                        }

                    })
                }

            }

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImage) {
            viewModel.updateUri(data?.data)
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
        sensorManager.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
        sensorManager.registerListener(this, brightness, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, sensorMagneticField, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onRestart() {
        super.onRestart()
        viewModel.getPoints()
        updateMap(getMaxRange())
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
        val sharedPrefEdit = getSharedPreferences(getString(R.string.login_key), Context.MODE_PRIVATE).edit()
        sharedPrefEdit.putInt(getString(R.string.user_login_key), 0)
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
                Log.d(getString(R.string.response_tag), response.errorBody().toString())
            }

        })


        mMap.setOnMarkerClickListener(this)
        mMap.setOnMapLongClickListener(this)
    }

    override fun onMarkerClick(p0: Marker?): Boolean {

        val request = RetrofitInstance.buildService(SlimAPI::class.java)
        val call = request.getPointById(p0?.tag as Int)

        call.enqueue(object : retrofit2.Callback<Point> {
            override fun onResponse(call: Call<Point>, response: Response<Point>) {
                if (response.body() != null) {
                    val point = response.body()
                    pointDialog(point!!, p0)
                } else {
                    Toast.makeText(this@MapActivity, getString(R.string.point_doesnt_exist), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Point>, t: Throwable) {
                Toast.makeText(this@MapActivity, t.message, Toast.LENGTH_SHORT).show()
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


        val image_url = "${LOCAL_URL}${point.photo}".replace("\\", "/")
        Glide.with(this).load(image_url).into(inflate_view.image_placeholder_info)

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

        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle(getString(R.string.problem))
        alertDialog.setView(inflate_view)
        alertDialog.setCancelable(true)

        if(point.user_id != userId){
            problemDescriptionEdit.isEnabled = false
            type_spinner.isEnabled = false
        }
        else{
            alertDialog.setNeutralButton(getString(R.string.delete), null)
            alertDialog.setPositiveButton(getString(R.string.update), null)
        }

        alertDialog.setNegativeButton(getString(R.string.cancel)) { _, _ ->
            Toast.makeText(this, getString(R.string.action_canceled), Toast.LENGTH_SHORT).show()
        }

        val dialog = alertDialog.create()
        dialog.show()



        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
            if(point.user_id == userId){

                val request = RetrofitInstance.buildService(SlimAPI::class.java)
                val call = request.deletePoint(point.id)

                call.enqueue(object : retrofit2.Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if (response.body() != null) {
                            Toast.makeText(this@MapActivity, getString(R.string.point_removed), Toast.LENGTH_SHORT).show()
                            marker.remove()
                            dialog.dismiss()
                        } else {
                            Toast.makeText(this@MapActivity, getString(R.string.point_doesnt_exist), Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        Toast.makeText(this@MapActivity, t.message, Toast.LENGTH_SHORT).show()
                    }
                })
            }else{
                Toast.makeText(this, getString(R.string.cant_delete_point), Toast.LENGTH_SHORT).show()
            }
        }

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (point.user_id == userId){
                val comment = problemDescriptionEdit.text
                val point_update = PointUpdate(comment.toString(), current_type)

                val request = RetrofitInstance.buildService(SlimAPI::class.java)
                val call = request.updatePoint(id = point.id, point = point_update)

                call.enqueue(object : retrofit2.Callback<Point> {
                    override fun onResponse(call: Call<Point>, response: Response<Point>) {
                        if (response.body() != null) {
                            Toast.makeText(this@MapActivity, getString(R.string.point_updated), Toast.LENGTH_SHORT).show()
                            marker.title = response.body()!!.comment
                            dialog.dismiss()
                        } else {
                            Toast.makeText(this@MapActivity, getString(R.string.point_doesnt_exist), Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Point>, t: Throwable) {
                        Toast.makeText(this@MapActivity, t.message, Toast.LENGTH_SHORT).show()
                    }
                })
            }
            else{
                Toast.makeText(this, getString(R.string.cant_update_point), Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if(isChecked) type_filter.add(buttonView?.text.toString())
        else type_filter.remove(buttonView?.text.toString())

        updateMap(getMaxRange())

    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT){
            val light = event.values[0]
            if (light < 100) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }else if ( event?.sensor?.type == Sensor.TYPE_ACCELEROMETER){
            floatGravity = event.values;

            SensorManager.getRotationMatrix(floatRotationMatrix, null, floatGravity, floatGeoMagnetic);
            SensorManager.getOrientation(floatRotationMatrix, floatOrientation);

            compass_view.setRotation((-floatOrientation[0]*180/3.14159).toFloat());
        }else if ( event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD){
            floatGeoMagnetic = event.values;

            SensorManager.getRotationMatrix(floatRotationMatrix, null, floatGravity, floatGeoMagnetic);
            SensorManager.getOrientation(floatRotationMatrix, floatOrientation);

            compass_view.setRotation((-floatOrientation[0]*180/3.14159).toFloat());
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

    fun getMaxRange() : Int {
        val sharedPref = getSharedPreferences(getString(R.string.settings_key), Context.MODE_PRIVATE)
        val maxRange = sharedPref.getInt(getString(R.string.settings_maxRange), 0)
        return maxRange
    }

    fun getNotifications() : Boolean {
        val sharedPref = getSharedPreferences(getString(R.string.settings_key), Context.MODE_PRIVATE)
        val notifications = sharedPref.getBoolean(getString(R.string.settings_notifications), false)
        return notifications
    }

    fun getGeofenceRadius() : Int {
        val sharedPref = getSharedPreferences(getString(R.string.settings_key), Context.MODE_PRIVATE)
        val radius = sharedPref.getInt(getString(R.string.settings_geofence), 100)
        return radius
    }

    fun updateMap(maxRange: Int) {
        mMap.clear()
        viewModel.pointListResponse.observe(this, Observer { response ->
            if (response.isSuccessful) {
                if (response.body() != null) {
                    response.body()!!.forEach {
                        if ((type_filter.contains(it.type) || type_filter.isEmpty())) {
                            if (maxRange > calculateDistance(lastLocation.latitude, lastLocation.longitude, it.latitude, it.longitude) || maxRange <= 0) {
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
                }
            } else {
                Log.d("Response", response.errorBody().toString())
            }
        })
    }

    override fun onMapLongClick(p0: LatLng?) {
        if(getNotifications()){
            circle?.remove()
            lifecycleScope.launch {
                circle = mMap.addCircle(CircleOptions().center(p0).radius(getGeofenceRadius().toDouble())
                        .strokeColor(R.color.red)
                        .fillColor(R.color.transparent_red))
                startGeofence(p0!!.latitude, p0!!.longitude, getGeofenceRadius())
            }
        }else{
            Toast.makeText(this, getString(R.string.enable_notifications_toast), Toast.LENGTH_SHORT).show()
        }

    }

    private fun setPendingIntent(geoId: Int) : PendingIntent {
        val intent = Intent(applicationContext, GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(
                applicationContext,
                geoId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    @SuppressLint("MissingPermission")
    private fun startGeofence(latitude: Double, longitude: Double, radius: Int) {
        val geofence = Geofence.Builder()
                .setRequestId(1.toString())
                .setCircularRegion(latitude, longitude, radius.toFloat())
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(
                        Geofence.GEOFENCE_TRANSITION_ENTER
                                or Geofence.GEOFENCE_TRANSITION_EXIT
                )
                .setLoiteringDelay(5000)
                .build()

        val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER or GeofencingRequest.INITIAL_TRIGGER_EXIT)
                .addGeofence(geofence)
                .build()

        geofencingClient.addGeofences(geofencingRequest, setPendingIntent(1))

    }

}