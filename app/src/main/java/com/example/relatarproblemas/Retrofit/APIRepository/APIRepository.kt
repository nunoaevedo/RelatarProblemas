package com.example.relatarproblemas.Retrofit.APIRepository

import com.example.relatarproblemas.Retrofit.API.RetrofitInstance
import com.example.relatarproblemas.Retrofit.Point.Point
import com.example.relatarproblemas.Retrofit.Type_Point.Type_Point
import com.example.relatarproblemas.Retrofit.User.LoginUser
import com.example.relatarproblemas.Retrofit.User.User
import retrofit2.Response

class APIRepository {

    suspend fun getUser() : Response<User> {
        return RetrofitInstance.api.getUser()
    }

    suspend fun login( login : LoginUser) : Response<User> {
        return RetrofitInstance.api.login(login)
    }

    suspend fun getPoints() : Response<List<Point>> {
        return RetrofitInstance.api.getPoints()
    }

    suspend fun getPointTypes() : Response<List<Type_Point>> {
        return RetrofitInstance.api.getPointTypes()
    }

    suspend fun newPoint(point : Point) : Response<Point>{
        return RetrofitInstance.api.newPoint(point)
    }

}