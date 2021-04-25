package com.example.relatarproblemas.Retrofit.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.relatarproblemas.Retrofit.APIRepository.APIRepository
import com.example.relatarproblemas.Retrofit.Point.Point
import com.example.relatarproblemas.Retrofit.Point.PointUpdate
import com.example.relatarproblemas.Retrofit.Type_Point.Type_Point
import com.example.relatarproblemas.Retrofit.User.LoginUser
import com.example.relatarproblemas.Retrofit.User.User
import kotlinx.coroutines.launch
import retrofit2.Response

class RetrofitViewModel(private val repository: APIRepository) : ViewModel() {

    val userResponse: MutableLiveData<Response<User>> = MutableLiveData()
    val pointListResponse: MutableLiveData<Response<List<Point>>> = MutableLiveData()
    val typePointListResponse: MutableLiveData<Response<List<Type_Point>>> = MutableLiveData()
    val pointResponse: MutableLiveData<Response<Point>> = MutableLiveData()
    val stringResponse : MutableLiveData<Response<String>> = MutableLiveData()
    val intResponse : MutableLiveData<Response<Int>> = MutableLiveData()

    fun login(login : LoginUser){
        viewModelScope.launch {
            val response = repository.login(login)
            userResponse.value = response
        }
    }

    fun getUser() {
        viewModelScope.launch {
            val response = repository.getUser()
            userResponse.value = response
        }
    }
    
    fun getPoints() {
        viewModelScope.launch {
            val response = repository.getPoints()
            pointListResponse.value = response
        }
    }

    fun getPointTypes() {
        viewModelScope.launch {
            val response = repository.getPointTypes()
            typePointListResponse.value = response
        }
    }

    fun newPoint(point : Point) {
        viewModelScope.launch {
            val response = repository.newPoint(point)
            pointResponse.value = response
        }
    }

    fun getPointById(id : Int) {
        viewModelScope.launch {
            val response = repository.getPointById(id)
            pointResponse.value = response
        }
    }

    fun deletePoint(id:Int) {
        viewModelScope.launch {
            val response = repository.deletePoint(id)
            stringResponse.value = response
        }
    }

    fun updatePoint(id: Int, point : PointUpdate) {
        viewModelScope.launch {
            val response = repository.updatePoint(id, point)
            stringResponse.value = response
        }
    }



}