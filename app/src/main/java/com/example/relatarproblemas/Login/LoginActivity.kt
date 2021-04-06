package com.example.relatarproblemas.Login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.relatarproblemas.Map.MapActivity
import com.example.relatarproblemas.Notes.NotesActivity
import com.example.relatarproblemas.R
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        view_notes_button.setOnClickListener {
            toViewNotes()
        }
        loginButton.setOnClickListener {
            toMapActivity()
        }


    }

    private fun toMapActivity() {
        val intent = Intent(this@LoginActivity, MapActivity::class.java)
        startActivity(intent)
    }


    fun toViewNotes(){
        val intent = Intent(this@LoginActivity, NotesActivity::class.java)
        startActivity(intent)
    }

}