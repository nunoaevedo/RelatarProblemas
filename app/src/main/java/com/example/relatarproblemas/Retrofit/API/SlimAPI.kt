package com.example.relatarproblemas.Retrofit.API

import android.media.Image
import android.net.Uri
import com.example.relatarproblemas.Retrofit.Point.Point
import com.example.relatarproblemas.Retrofit.Point.PointUpdate
import com.example.relatarproblemas.Retrofit.Type_Point.Type_Point
import com.example.relatarproblemas.Retrofit.User.LoginUser
import com.example.relatarproblemas.Retrofit.User.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import java.io.File

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

    @Multipart
    @POST("new/point/")
    suspend fun newPointImage(
            @Part photo : MultipartBody.Part,
            @Part("comment") comment : RequestBody,
            @Part("latitude") latitude : Double,
            @Part("longitude") longitude : Double,
            @Part("user_id") user_id : Int,
            @Part("type") type : RequestBody
    ) : Response<Point>

    @Multipart
    @POST("new/point/")
    fun newPoint(
            @Part("comment") comment : RequestBody,
            @Part("latitude") latitude : Double,
            @Part("longitude") longitude : Double,
            @Part("user_id") user_id : Int,
            @Part("type") type : RequestBody
    ) : Call<Point>

    @GET("point/{id}")
    fun getPointById(
            @Path("id") id : Int
    ) : Call<Point>

    @POST("delete/point/{id}")
    fun deletePoint(
            @Path("id") id:Int
    ) : Call<String>

    @POST("update/point/{id}")
    fun updatePoint(
            @Path("id") id : Int,
            @Body point : PointUpdate
    ) : Call<Point>

}