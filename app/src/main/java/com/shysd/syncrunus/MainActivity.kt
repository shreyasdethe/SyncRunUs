package com.shysd.syncrunus

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly

/*
    Created By: Shreyas Dethe
    https://shreyasdethe.github.io
    Start Date: Jun 22, 2021
 */

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()

        // request permission launcher ActivityResultLauncher
        var permissionStatus = PackageManager.PERMISSION_DENIED

        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                isGranted: Boolean ->
            run {
                if (!isGranted) {
                    permissionStatus = PackageManager.PERMISSION_GRANTED
                    Toast.makeText(this, getString(R.string.permissionDenied), Toast.LENGTH_SHORT)
                        .show()
                }
                else {
                    Toast.makeText(
                        this, getString(R.string.permissionGranted), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        // check if permission is granted
        val perm: Int = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(this,
                "com.google.android.gms.permission.ACTIVITY_RECOGNITION")
        }
        else {
            ContextCompat.checkSelfPermission(this,
                "android.permission.ACTIVITY_RECOGNITION")
        }

        // if not, ask
        if(perm == PackageManager.PERMISSION_DENIED) {
            requestPermissionLauncher.launch("com.google.android.gms.permission.ACTIVITY_RECOGNITION")
        }


        // take values from fields, store and move ahead
        val sp = getSharedPreferences("com.shysd.syncrunus.creds", Context.MODE_PRIVATE)
        if(sp.contains("username") && sp.contains("password") && permissionStatus ==
            PackageManager.PERMISSION_GRANTED) {
                // start HeroActivity
                startActivity(Intent(this, HeroActivity::class.java))
        }

    }

    override fun onResume() {
        super.onResume()
        val sp = getSharedPreferences("com.shysd.syncrunus.creds", Context.MODE_PRIVATE)
        if(sp.contains("username") && sp.contains("password")) {
            // start HeroActivity
            startActivity(Intent(this, HeroActivity::class.java))
        }
    }

    // saving in local SharedPreferences
    // password field is unused yet
    fun registerUser(view: View) {
        val sp = getSharedPreferences("com.shysd.syncrunus.creds", Context.MODE_PRIVATE)

        if(!sp.contains("username") || !sp.contains("password")) {
            val username = findViewById<TextView>(R.id.username).text.toString()
            val password = findViewById<TextView>(R.id.password).text.toString()
            val height   = findViewById<TextView>(R.id.height).text.toString()
            val ed = sp.edit()
            if(username != "" && password != "" && height != "") {
                ed.putString("username", username)
                ed.putString("password", password)  // TODO: Put encrypted value here
                if(height.isDigitsOnly() && height.toInt() < 190 && height.toInt() > 135) {
                    ed.putString("height", height)
                    // using commit() here instead of apply() because
                    // need to make sure SP is edited before starting new activity
                    ed.commit()

                    // start HeroActivity
                    startActivity(Intent(this, HeroActivity::class.java))
                }
                else {
                    Toast.makeText(this, getString(R.string.heightWrong), Toast.LENGTH_SHORT)
                        .show()
                }
            }
            else {
                Toast.makeText(this, getString(R.string.emptyFields), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}