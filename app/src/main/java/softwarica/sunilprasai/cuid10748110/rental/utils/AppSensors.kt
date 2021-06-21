package softwarica.sunilprasai.cuid10748110.rental.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.PowerManager
import kotlin.math.pow
import kotlin.math.sqrt

class AppSensors private constructor(
    context: Context
) : SensorEventListener {
    private val userSetting: UserSetting = UserSetting.getInstance(context)
    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometerSensor: Sensor =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val wakeLock: PowerManager.WakeLock =
        powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, javaClass.simpleName)
    private var lastUpdate: Long = 0
    private lateinit var callback: SensorEventListener

    fun registerListener(callback: SensorEventListener) {
        this.callback = callback
    }

    fun unRegisterSensors() {
        sensorManager.unregisterListener(this)
        if (wakeLock.isHeld) wakeLock.release()
    }

    fun registerSensors() {
        if (userSetting.shouldUseShakeDevice)
            sensorManager.registerListener(
                this,
                accelerometerSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        else
            sensorManager.unregisterListener(this)

        if (!wakeLock.isHeld && userSetting.shouldUseProximitySensor) {
            wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)
        } else {
            if (wakeLock.isHeld) wakeLock.release()
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val curTime = System.currentTimeMillis()
            if (curTime - lastUpdate > MIN_TIME_BETWEEN_SHAKES) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val acceleration = sqrt(
                    x.toDouble().pow(2.0) +
                            y.toDouble().pow(2.0) +
                            z.toDouble().pow(2.0)
                ) - SensorManager.GRAVITY_EARTH
                if (acceleration > SHAKE_THRESHOLD) {
                    lastUpdate = curTime
                    callback.onDeviceShake()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    companion object {
        private const val SHAKE_THRESHOLD = 8.00f
        private const val MIN_TIME_BETWEEN_SHAKES = 1000

        @Volatile
        private var instance: AppSensors? = null

        @JvmStatic
        fun getInstance(context: Context): AppSensors =
            instance ?: synchronized(this) {
                AppSensors(context).also { instance = it }
            }
    }

    interface SensorEventListener {
        fun onDeviceShake()
    }
}
