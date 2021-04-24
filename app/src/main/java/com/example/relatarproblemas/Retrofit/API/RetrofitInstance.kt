package com.example.relatarproblemas.Retrofit.API

//import com.example.relatarproblemas.Retrofit.Constants.Companion.API_URL2
import com.example.relatarproblemas.Retrofit.Constants.Companion.BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitInstance {

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: SlimAPI by lazy {
        retrofit.create(SlimAPI::class.java)
    }


}