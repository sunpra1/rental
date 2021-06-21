package softwarica.sunilprasai.cuid10748110.rental.model

import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.security.InvalidParameterException

private const val TAG = "Location"

class Location() {
    companion object {
        @JvmStatic
        val LATITUDE = "latitude"

        @JvmStatic
        val LONGITUDE = "longitude"
    }

    var latitude: Double? = null
    var longitude: Double? = null

    constructor(jsonObject: JSONObject) : this() {
        try {
            latitude = if (jsonObject.has(LATITUDE) && !jsonObject.isNull(LATITUDE))
                jsonObject.getDouble(LATITUDE)
            else throw InvalidParameterException("Location must contain latitude parameter")
            longitude = if (jsonObject.has(LONGITUDE) && !jsonObject.isNull(LONGITUDE))
                jsonObject.getDouble(LONGITUDE)
            else throw InvalidParameterException("Location must contain longitude parameter")
        } catch (e: JSONException) {
            Log.d(TAG, "constructor: ${e.localizedMessage}")
        }
    }

    constructor(location: Location) : this() {
        latitude = location.latitude
        longitude = location.longitude
    }

    fun getJSONObject(): JSONObject {
        val jsonObject = JSONObject()
        try {
            latitude?.let {
                jsonObject.put(LATITUDE, it)
            }

            longitude?.let {
                jsonObject.put(LONGITUDE, it)
            }
        } catch (e: JSONException) {
            Log.d(TAG, "getJSONObject: ${e.localizedMessage}")
        }

        return jsonObject
    }
}