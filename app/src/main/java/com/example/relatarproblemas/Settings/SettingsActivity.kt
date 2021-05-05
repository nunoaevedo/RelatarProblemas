package com.example.relatarproblemas.Settings

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.relatarproblemas.R
import com.example.relatarproblemas.Retrofit.Type_Point.Type_Point
import com.example.relatarproblemas.Settings.TypeDataModel.DataModel
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.fragment_add_notes.*


class SettingsActivity : AppCompatActivity() {

    private lateinit var point_types : List<Type_Point>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        save.setOnClickListener {
//            Toast.makeText(this, distance_edit.text.toString(), Toast.LENGTH_SHORT).show()
            saveMaxRange()
        }

    }


    fun saveMaxRange(){
        if (distance_edit.text.toString() != ""){
            val max_range = distance_edit.text.toString().toIntOrNull()
            val sharedPrefEdit = getSharedPreferences("settings", Context.MODE_PRIVATE).edit()
            sharedPrefEdit.putInt("maxRange", max_range!!)
            sharedPrefEdit.apply()
            finish()
        }
        else Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show()
    }


}