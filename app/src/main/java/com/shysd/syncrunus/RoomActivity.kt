package com.shysd.syncrunus

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.*

class RoomActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var client: MqttClient

    private var code = ""
    private var topic = ""
    private var sessionStartSteps = -1
    private var username = ""
    private var height = ""
    private var steps = ""
    private var kms = ""
    private var otherUser = ""
    private var otherSteps = ""
    private var otherKms = ""
    private var running: Boolean = false

    // ------------------ Default methods ---------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)
        client = MqttClient(this)
    }

    override fun onStart() {
        super.onStart()
        // request sensor
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    }

    override fun onResume() {
        super.onResume()

        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)

        var sp = getSharedPreferences("com.shysd.syncrunus.creds", Context.MODE_PRIVATE)
        username = sp.getString("username", "")!!
        height = sp.getString("height", "")!!

        sp = getSharedPreferences("com.shysd.syncrunus.code", Context.MODE_PRIVATE)
        code = sp.getString("code", "")!!
        findViewById<TextView>(R.id.roomHeader).text =
            getString(R.string.roomHeader, code, "not running")

        topic = "com.shysd.syncrunus/$code"

        // Connect to MQTT Broker
        Log.d("Debug", client.toString())
        client.connect(arrayOf(topic), onMessageReceived)

        // Publish the introduction message, which shares username across
        Timer().schedule(object: TimerTask() {
            override fun run() {
                val introMsg = "intro-$username"
                client.publishMessage(topic, introMsg)
            }
        }, 2000)
    }

    override fun onPause() {
        super.onPause()
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.unregisterListener(this)
        client.close()
    }

    // ------------------ Sensor methods ---------------------------------
    override fun onSensorChanged(event: SensorEvent?) {
        // everytime sensor reports value, calculate session steps
        event ?: return

        if(running) {
            val newSteps = event.values[0].toInt()
            if(sessionStartSteps == -1) {
                sessionStartSteps = newSteps
            }

            steps = (newSteps - sessionStartSteps).toString()
            kms = String.format("%.3f", height.toInt() * 0.415 * 0.01 * 0.001 * steps.toInt())

            // publish our session steps and kms
            val msg = "steps-$username-$steps-$kms"
            client.publishMessage(topic, msg)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // ------------------ Handler methods ---------------------------------
    fun onStartSessionButtonPressed(view: View) {
        if(running) {
            // if running
            running = false
        }
        else {
            // if not running
            if(otherUser == "") {
                // if other user hasn't joined yet
                Toast.makeText(this, getString(R.string.otherNotJoined), Toast.LENGTH_SHORT)
                    .show()
                client.publishMessage(topic, "askIntro-please")
            }
            else {
                // if other user has joined then
                running = true
            }
        }
        updateRunningStatusGUI(running)
        client.publishMessage(topic, "running-$running")
    }


    // ------------------ Private methods ---------------------------------
    private fun messageParser(topic: String, msg: String) {
        Log.d("Debug", "Message received: $msg on topic: $topic")

        val brokenMsg = msg.split("-").toTypedArray()

        // check if it is an introduction message
        if(brokenMsg[0] == "intro") {
            if(brokenMsg[1] == username) {
                findViewById<TextView>(R.id.currentUsername).text = username
            }
            else {
                otherUser = brokenMsg[1]
                findViewById<TextView>(R.id.otherUsername).text = otherUser
            }
        }

        // check if it is asking for introduction
        else if(brokenMsg[0] == "askIntro") {
            // send our introduction, again if necessary
            val introMsg = "intro-$username"
            client.publishMessage(topic, introMsg)
        }

        // check if it is a steps data message
        else if(brokenMsg[0] == "steps") {
            if(brokenMsg[1] == otherUser) {
                otherSteps = brokenMsg[2]
                otherKms = brokenMsg[3]

                findViewById<TextView>(R.id.otherSteps).text = otherSteps
                findViewById<TextView>(R.id.otherKms).text = otherKms
            }

            else if(brokenMsg[1] == username) {
                findViewById<TextView>(R.id.currentSteps).text = steps
                findViewById<TextView>(R.id.currentKms).text = kms
            }
        }

        // check if it is a running status broadcast
        else if(brokenMsg[0] == "running") {
            running = brokenMsg[1].toBoolean()
            updateRunningStatusGUI(running)
        }
    }


    private fun updateRunningStatusGUI(updated: Boolean) {
        if(!updated) {
            findViewById<Button>(R.id.startSession).text =
                getString(R.string.startSession)
            findViewById<TextView>(R.id.roomHeader).text =
                getString(R.string.roomHeader, code, "not running")
        }
        else {
            findViewById<Button>(R.id.startSession).text =
                getString(R.string.stopSession)
            findViewById<TextView>(R.id.roomHeader).text =
                getString(R.string.roomHeader, code, "running")
        }
    }


    private val onMessageReceived: ((topic: String, message: MqttMessage) -> Unit)? = {
            topic: String, msg: MqttMessage -> messageParser(topic, msg.toString())
    }

}