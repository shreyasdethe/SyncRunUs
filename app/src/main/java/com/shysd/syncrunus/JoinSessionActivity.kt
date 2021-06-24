package com.shysd.syncrunus

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast

class JoinSessionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_session)
    }

    fun onJoinSessionButtonClicked(view: View) {
        val code = findViewById<EditText>(R.id.shareCode).text.toString()
        if(code.length != 5) {
            Toast.makeText(this, "Please enter valid code", Toast.LENGTH_SHORT).show()
        }
        else {
            val sp = getSharedPreferences("com.shysd.syncrunus.code", Context.MODE_PRIVATE)
            sp.edit().putString("code", code).commit()
        }

        Toast.makeText(this, "Joining room", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, RoomActivity::class.java))
    }
}