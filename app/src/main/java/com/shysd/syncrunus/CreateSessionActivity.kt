package com.shysd.syncrunus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.floor

class CreateSessionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_session)
    }

    override fun onStart() {
        super.onStart()
        // random code generator
        // removing O, I for non-ambiguity
        val characters: Array<Char> = arrayOf('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J',
            'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z')

        var randomString = ""
        for(i in 0..4) {
            randomString += characters[floor(Math.random() * characters.size).toInt()]
        }

        findViewById<TextView>(R.id.shareCode).text = randomString
        val sp = getSharedPreferences("com.shysd.syncrunus.code", Context.MODE_PRIVATE)
        sp.edit().putString("code", randomString).commit()
    }

    fun onCreateNewSessionButtonClicked(view: View) {
        Toast.makeText(this, "Joining room", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, RoomActivity::class.java))
    }
}