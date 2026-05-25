package com.example.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class AccelerometerActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    // State for UI
    private var rawX by mutableFloatStateOf(0f)
    private var rawY by mutableFloatStateOf(0f)
    private var rawZ by mutableFloatStateOf(0f)
    private var linearX by mutableFloatStateOf(0f)
    private var linearY by mutableFloatStateOf(0f)
    private var linearZ by mutableFloatStateOf(0f)

    // Low-pass filter for gravity separation
    private val gravity = FloatArray(3)
    private val alpha = 0.8f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        setContent {
            // FIXED: Use MaterialTheme directly instead of AccelerometerTaskTheme
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AccelerometerDisplay(
                        rawX = rawX,
                        rawY = rawY,
                        rawZ = rawZ,
                        linearX = linearX,
                        linearY = linearY,
                        linearZ = linearZ
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val x = it.values[0]
                val y = it.values[1]
                val z = it.values[2]

                // Update raw values
                rawX = x
                rawY = y
                rawZ = z

                // Low-pass filter to isolate gravity
                gravity[0] = alpha * gravity[0] + (1 - alpha) * x
                gravity[1] = alpha * gravity[1] + (1 - alpha) * y
                gravity[2] = alpha * gravity[2] + (1 - alpha) * z

                // Linear acceleration = raw - gravity
                linearX = x - gravity[0]
                linearY = y - gravity[1]
                linearZ = z - gravity[2]
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed
    }
}

@Composable
fun AccelerometerDisplay(
    rawX: Float,
    rawY: Float,
    rawZ: Float,
    linearX: Float,
    linearY: Float,
    linearZ: Float
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Raw Accelerometer Data", fontSize = 20.sp, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("X: %.2f".format(rawX))
        Text("Y: %.2f".format(rawY))
        Text("Z: %.2f".format(rawZ))

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()

        Spacer(modifier = Modifier.height(24.dp))
        Text("Linear Acceleration (Gravity Filtered)", fontSize = 20.sp, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("X: %.2f".format(linearX))
        Text("Y: %.2f".format(linearY))
        Text("Z: %.2f".format(linearZ))
    }
}