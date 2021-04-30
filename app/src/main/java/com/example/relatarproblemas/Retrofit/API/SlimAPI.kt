package com.example.relatarproblemas.Retrofit.API

import android.media.Image
import android.net.Uri
import com.example.relatarproblemas.Retrofit.Point.Point
import com.example.relatarproblemas.Retrofit.Point.PointUpdate
import com.example.relatarproblemas.Retrofit.Type_Point.Type_Point
import com.example.relatarproblemas.Retrofit.User.LoginUser
import com.example.relatarproblemas.Retrofit.User.User
import okhttp3.MultipartBody
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
            @Part("comment") comment : String,
            @Part("latitude") latitude : Double,
            @Part("longitude") longitude : Double,
            @Part("user_id") user_id : Int,
            @Part("type") type : String
    ) : Response<Point>


    @POST("new/point/")
    suspend fun newPoint(
//            @Part("comment") comment : String,
//            @Part("latitude") latitude : Double,
//            @Part("longitude") longitude : Double,
//            @Part("user_id") user_id : Int,
//            @Part("type") type : String
            @Body point: Point
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