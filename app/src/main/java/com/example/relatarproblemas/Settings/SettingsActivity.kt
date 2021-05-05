package com.example.relatarproblemas.Settings

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import com.example.relatarproblemas.R
import kotlinx.android.synthetic.main.activity_settings.*


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        distance_edit.setText(getMaxRange().toString())
        toggle_notifications.isChecked = getNotifications()
        geofence_edit.setText(getGeofenceRadius().toString())

        toggle_notifications.setOnCheckedChangeListener { buttonView, isChecked ->
            geofence_edit.isEnabled = isChecked
        }

        save.setOnClickListener {
            save()
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun save(){
        if (distance_edit.text.toString() != "" && geofence_edit.text.toString() != ""){
            val max_range = distance_edit.text.toString().toIntOrNull()
            val notifications = toggle_notifications.isChecked
            val radius = geofence_edit.text.toString().toIntOrNull()
            setMaxRange(max_range!!)
            setNotifications(notifications)
            setGeoRadius(radius!!)
            finish()
        }
        else Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show()
    }

    private fun getMaxRange() : Int {
        val sharedPref = getSharedPreferences(getString(R.string.settings_key), Context.MODE_PRIVATE)
        val maxRange = sharedPref.getInt(getString(R.string.settings_maxRange), 0)
        return maxRange
    }

    private fun setMaxRange(maxRange : Int) {
        val sharedPrefEdit = getSharedPreferences(getString(R.string.settings_key), Context.MODE_PRIVATE).edit()
        sharedPrefEdit.putInt(getString(R.string.settings_maxRange), maxRange)
        sharedPrefEdit.apply()
    }

    private fun getNotifications() : Boolean {
        val sharedPref = getSharedPreferences(getString(R.string.settings_key), Context.MODE_PRIVATE)
        val notifications = sharedPref.getBoolean(getString(R.string.settings_notifications), false)
        return notifications
    }

    private fun setNotifications(notifications : Boolean) {
        val sharedPrefEdit = getSharedPreferences(getString(R.string.settings_key), Context.MODE_PRIVATE).edit()
        sharedPrefEdit.putBoolean(getString(R.string.settings_notifications), notifications)
        sharedPrefEdit.apply()
    }

    private fun setGeoRadius(radius : Int) {
        val sharedPrefEdit = getSharedPreferences(getString(R.string.settings_key), Context.MODE_PRIVATE).edit()
        sharedPrefEdit.putInt(getString(R.string.settings_geofence), radius)
        sharedPrefEdit.apply()
    }

    private fun getGeofenceRadius() : Int {
        val sharedPref = getSharedPreferences(getString(R.string.settings_key), Context.MODE_PRIVATE)
        val radius = sharedPref.getInt(getString(R.string.settings_geofence), 100)
        return radius
    }

}