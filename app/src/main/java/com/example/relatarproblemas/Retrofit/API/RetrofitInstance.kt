package com.example.relatarproblemas.Retrofit.API

import com.example.relatarproblemas.Retrofit.Constants.Companion.BASE_URL
import com.example.relatarproblemas.Retrofit.Constants.Companion.LOCAL_URL
import com.example.relatarproblemas.Retrofit.Constants.Companion.OTHER_URL
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitInstance {

    private val client = OkHttpClient.Builder().build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(LOCAL_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: SlimAPI by lazy {
        retrofit.create(SlimAPI::class.java)
    }

    private val retrofitCall = Retrofit.Builder()
            .baseUrl(LOCAL_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

    fun <T> buildService(service: Class<T>): T {
        return retrofitCall.create(service)
    }


}