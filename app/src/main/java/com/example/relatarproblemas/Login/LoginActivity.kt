package com.example.relatarproblemas.Login

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.relatarproblemas.Map.MapActivity
import com.example.relatarproblemas.Notes.NotesActivity
import com.example.relatarproblemas.R
import com.example.relatarproblemas.Retrofit.API.RetrofitInstance
import com.example.relatarproblemas.Retrofit.API.SlimAPI
import com.example.relatarproblemas.Retrofit.APIRepository.APIRepository
import com.example.relatarproblemas.Retrofit.Point.Point
import com.example.relatarproblemas.Retrofit.User.LoginUser
import com.example.relatarproblemas.Retrofit.User.User
import com.example.relatarproblemas.Retrofit.ViewModel.RetrofitViewModel
import com.example.relatarproblemas.Retrofit.ViewModel.RetrofitViewModelFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var viewModel: RetrofitViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val repository = APIRepository()
        val viewModelFactory = RetrofitViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(RetrofitViewModel::class.java)

        view_notes_button.setOnClickListener {
            toViewNotes()
        }
        loginButton.setOnClickListener {
            val username = username_editText.text.toString()
            val password = password_editText.text.toString()
            
            if (username == "" || password == "" ){
                Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
            }
            else{

                val login = LoginUser(username, password)

                val request = RetrofitInstance.buildService(SlimAPI::class.java)
                val call = request.login(login)

                call.enqueue(object : retrofit2.Callback<User> {
                    override fun onResponse(call: Call<User>, response: Response<User>) {
                        if (response.body() != null) {
                            val userId = response.body()?.id!!
                            saveLogin(userId)
                            toMapActivity()
                        } else {
                            Toast.makeText(this@LoginActivity, getString(R.string.username_password_incorrect), Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<User>, t: Throwable) {
                        Toast.makeText(this@LoginActivity, t.message, Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
        
        if (isLoggedIn()){
            toMapActivity()
        }

    }

    private fun isLoggedIn() : Boolean{
        val sharedPref = getSharedPreferences(getString(R.string.login_key), Context.MODE_PRIVATE)
        val userId = sharedPref.getInt(getString(R.string.user_login_key), 0)
        return userId != 0

    }

    private fun saveLogin(userId : Int){
        val sharedPrefEdit = getSharedPreferences(getString(R.string.login_key), Context.MODE_PRIVATE).edit()
        sharedPrefEdit.putInt(getString(R.string.user_login_key), userId)
        sharedPrefEdit.apply()
    }

    private fun login(username : String, password : String) : Int{
        val login = LoginUser(username, password)

        var userId = 0

        viewModel.login(login)
        viewModel.userResponse.observe(this, Observer { response ->
            if (response.isSuccessful) {
                if (response.body() != null) {
                    userId = response.body()?.id!!
                }
            }
        })

        return userId
    }

    private fun toMapActivity() {
        val intent = Intent(this@LoginActivity, MapActivity::class.java)
        startActivity(intent)
        finish()
    }


    fun toViewNotes(){
        val intent = Intent(this@LoginActivity, NotesActivity::class.java)
        startActivity(intent)
    }

}