package com.shysd.syncrunus

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.icu.text.DateFormat.getDateInstance
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.lang.Math.floor
import java.util.*
import kotlin.math.floor

class HeroActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var height: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hero)
    }

    override fun onStart() {
        super.onStart()

        // update name in welcome TextView
        val sp = getSharedPreferences("com.shysd.syncrunus.creds", Context.MODE_PRIVATE)
        val username = sp.getString("username", "User")
        height = sp.getString("height", "170")!!
        findViewById<TextView>(R.id.welcomeText).text = getString(R.string.welcome, username)

        // request sensor
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    }

    override fun onResume() {
        super.onResume()
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.unregisterListener(this)
    }

    // this annotation is to suppress apply() call warning
    // need to use commit() for best speed
    @SuppressLint("ApplySharedPref")
    override fun onSensorChanged(event: SensorEvent?) {
        // sensor reports steps taken since last reboot
        // everytime sensor reports a value, calculate today's steps
        event ?: return

        val sp = getPreferences(Context.MODE_PRIVATE)

        // daily step calculation algo
        var everyDayStepCount = sp.getInt("everyDayStepCount", 0)
        val totalStepsSinceReboot = event.values[0].toInt()
        val todaySteps = sp.getInt(today(), 0)
        if(todaySteps == 0) {
            everyDayStepCount = totalStepsSinceReboot
            sp.edit().putInt(today(), 1).commit()
        }
        else {
            val addStep = totalStepsSinceReboot - everyDayStepCount
            sp.edit().putInt(today(), todaySteps+addStep).commit()
            everyDayStepCount = totalStepsSinceReboot
        }

        sp.edit().putInt("everyDayStepCount", everyDayStepCount).commit()

        // step length = 0.415*height for males, 0.413*height for females (not implemented)
        val kms = String.format("%.3f", height.toInt() * 0.415 * 0.01 * 0.001 * todaySteps)
        // update GUI steps and kms
        findViewById<TextView>(R.id.heroSteps).text = todaySteps.toString()
        findViewById<TextView>(R.id.heroSteps2).text = kms
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    private fun today(): String {
        return getDateInstance().format(Calendar.getInstance().time)
    }

    fun createNewSession(view: View) {
        startActivity(Intent(this, CreateSessionActivity::class.java))
    }

    fun joinSession(view: View) {
        startActivity(Intent(this, JoinSessionActivity::class.java))
    }
}