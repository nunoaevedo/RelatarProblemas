package com.example.relatarproblemas.Login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.relatarproblemas.Notes.NotesActivity
import com.example.relatarproblemas.R

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        findViewById<Button>(R.id.view_notes_button).setOnClickListener {
            toViewNotes()
        }

    }



    fun toViewNotes(){
        val intent = Intent(this@LoginActivity, NotesActivity::class.java)
        startActivity(intent)
    }

}