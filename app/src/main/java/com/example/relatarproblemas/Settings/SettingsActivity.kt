package com.example.relatarproblemas.Settings

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.relatarproblemas.R
import com.example.relatarproblemas.Retrofit.Type_Point.Type_Point
import com.example.relatarproblemas.Settings.TypeDataModel.DataModel


class SettingsActivity : AppCompatActivity() {

    private lateinit var point_types : List<Type_Point>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

//        point_types = intent.extras?.get("types") as List<Type_Point>

//        val dataModel : List<DataModel>

    }


}