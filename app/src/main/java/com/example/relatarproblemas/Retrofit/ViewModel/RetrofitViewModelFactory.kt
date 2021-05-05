package com.example.relatarproblemas.Retrofit.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.relatarproblemas.Retrofit.APIRepository.APIRepository

class RetrofitViewModelFactory(private val repository: APIRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return RetrofitViewModel(repository) as T
    }


}