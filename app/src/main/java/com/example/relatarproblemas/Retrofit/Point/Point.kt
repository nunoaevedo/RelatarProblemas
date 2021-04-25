package com.example.relatarproblemas.Retrofit.Point

import android.net.Uri
import java.util.*

data class Point(
        val id: Int,
        val comment: String,
        val date: Date,
        val latitude: Double,
        val longitude: Double,
        val user_id: Int,
        val type_id: Int,
        val photo: String

        )
