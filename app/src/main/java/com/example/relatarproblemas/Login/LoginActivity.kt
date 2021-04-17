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
import com.example.relatarproblemas.Retrofit.APIRepository.APIRepository
import com.example.relatarproblemas.Retrofit.User.LoginUser
import com.example.relatarproblemas.Retrofit.ViewModel.RetrofitViewModel
import com.example.relatarproblemas.Retrofit.ViewModel.RetrofitViewModelFactory
import kotlinx.android.synthetic.main.activity_login.*

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
                Toast.makeText(this, "Please fill out all fields!", Toast.LENGTH_SHORT).show()
            }
            else{
                val userId = login(username, password)
                if (userId > 0){
                    saveLogin(userId)
                    toMapActivity()
                }else {
                    Toast.makeText(this, "Username or password incorrect!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        if (isLoggedIn()){
            toMapActivity()
        }

    }

    private fun isLoggedIn() : Boolean{
        val sharedPref = getSharedPreferences("loginInfo", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("userId", 0)
        return userId != 0

    }

    private fun saveLogin(userId : Int){
        val sharedPrefEdit = getSharedPreferences("loginInfo", Context.MODE_PRIVATE).edit()
        sharedPrefEdit.putInt("userId", userId)
        sharedPrefEdit.apply()
    }

    private fun login(username : String, password : String) : Int{
        val login = LoginUser(username, password)

        var userId = 0

        viewModel.login(login)
        viewModel.userResponse.observe(this, Observer { response ->
            if (response.isSuccessful) {
                if (response.body() != null) {
//                    saveLogin(response.body()?.id!!)
//                    toMapActivity()
                    userId = response.body()?.id!!
                }
//                else {
//                    Toast.makeText(this, "Username or password are incorrect", Toast.LENGTH_SHORT).show()
//                }
            }
//            else {
//
//                Toast.makeText(this, response.code(), Toast.LENGTH_SHORT).show()
//            }
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