package com.example.relatarproblemas.Retrofit.APIRepository

import com.example.relatarproblemas.Retrofit.API.RetrofitInstance
import com.example.relatarproblemas.Retrofit.Point.Point
import com.example.relatarproblemas.Retrofit.Point.PointUpdate
import com.example.relatarproblemas.Retrofit.Type_Point.Type_Point
import com.example.relatarproblemas.Retrofit.User.LoginUser
import com.example.relatarproblemas.Retrofit.User.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response

class APIRepository {

    suspend fun getUser() : Response<User> {
        return RetrofitInstance.api.getUser()
    }

    suspend fun login( login : LoginUser) : Call<User> {
        return RetrofitInstance.api.login(login)
    }

    suspend fun getPoints() : Response<List<Point>> {
        return RetrofitInstance.api.getPoints()
    }

    suspend fun getPointTypes() : Response<List<Type_Point>> {
        return RetrofitInstance.api.getPointTypes()
    }

    suspend fun newPointImage(photo : MultipartBody.Part, comment : RequestBody, latitude: Double, longitude : Double, user_id : Int, type : RequestBody) : Response<Point>{
        return RetrofitInstance.api.newPointImage(photo, comment, latitude, longitude, user_id, type)
    }

//    suspend fun newPoint(point : Point) : Call<Point>{
//        return RetrofitInstance.api.newPoint(point)
//    }

    fun getPointById(id : Int) : Call<Point> {
        return RetrofitInstance.api.getPointById(id)
    }

    suspend fun deletePoint(id: Int) : Call<String>{
        return RetrofitInstance.api.deletePoint(id)
    }

    suspend fun updatePoint(id : Int, point : PointUpdate) : Call<Point>{
        return RetrofitInstance.api.updatePoint(id, point)
    }

}