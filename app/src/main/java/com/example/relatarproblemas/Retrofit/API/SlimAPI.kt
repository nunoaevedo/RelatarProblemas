package com.example.relatarproblemas.Retrofit.API

import com.example.relatarproblemas.Retrofit.Point.Point
import com.example.relatarproblemas.Retrofit.Point.PointUpdate
import com.example.relatarproblemas.Retrofit.Type_Point.Type_Point
import com.example.relatarproblemas.Retrofit.User.LoginUser
import com.example.relatarproblemas.Retrofit.User.User
import retrofit2.Response
import retrofit2.http.*

interface SlimAPI {

    @GET("user/")
    suspend fun getUser(): Response<User>

    @POST("login/")
    suspend fun login(
            @Body login : LoginUser
    ): Response<User>

    @GET ("points/")
    suspend fun getPoints(): Response<List<Point>>

    @GET("point_types/")
    suspend fun getPointTypes(): Response<List<Type_Point>>

    @POST("new/point/")
    suspend fun newPoint(
            @Body point : Point
    ) : Response<Point>

    @GET("point/{id}")
    suspend fun getPointById(
            @Path("id") id : Int
    ) : Response<Point>

    @POST("delete/point/{id}")
    suspend fun deletePoint(
            @Path("id") id:Int
    ) : Response<String>

    @POST("update/point/{id}")
    suspend fun updatePoint(
            @Path("id") id : Int,
            @Body point : PointUpdate
    ) : Response<String>

}