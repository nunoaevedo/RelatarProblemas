package com.example.relatarproblemas.Retrofit.API

import com.example.relatarproblemas.Retrofit.Point.Point
import com.example.relatarproblemas.Retrofit.User.LoginUser
import com.example.relatarproblemas.Retrofit.User.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SlimAPI {

    @GET("user/")
    suspend fun getUser(): Response<User>

    @POST("login/")
    suspend fun login(
            @Body login : LoginUser
//            @Body username : String,
//            @Body password : String
    ): Response<User>

    @GET ("points/")
    suspend fun getPoints(): Response<List<Point>>

}