package softwarica.sunilprasai.cuid10748110.rental.utils

import android.content.Context
import android.content.SharedPreferences

private const val USER_SETTING = "USER_SETTING"
private const val USE_SHAKE_DEVICE = "USE_SHAKE_DEVICE"
private const val USE_IN_APP_VIBRATION = "USE_IN_APP_VIBRATION"
const val USE_PROXIMITY_SENSOR = "USE_PROXIMITY_SENSOR"

class UserSetting private constructor(context: Context) {
    companion object {
        @Volatile
        private var instance: UserSetting? = null

        @JvmStatic
        fun getInstance(context: Context): UserSetting =
            instance ?: synchronized(this) {
                UserSetting(context).also { instance = it }
            }
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(USER_SETTING, Context.MODE_PRIVATE)

    var shouldUseInAppVibration: Boolean = false
        set(value) {
            sharedPreferences.edit().putBoolean(USE_IN_APP_VIBRATION, value).apply()
            field = value
        }
    var shouldUseShakeDevice: Boolean = false
        set(value) {
            sharedPreferences.edit().putBoolean(USE_SHAKE_DEVICE, value).apply()
            field = value
        }

    var shouldUseProximitySensor: Boolean = false
        set(value) {
            sharedPreferences.edit().putBoolean(USE_PROXIMITY_SENSOR, value).apply()
            field = value
        }

    init {
        shouldUseInAppVibration = sharedPreferences.getBoolean(USE_SHAKE_DEVICE, false)
        shouldUseShakeDevice = sharedPreferences.getBoolean(USE_IN_APP_VIBRATION, false)
        shouldUseProximitySensor = sharedPreferences.getBoolean(USE_PROXIMITY_SENSOR, false)
    }
}