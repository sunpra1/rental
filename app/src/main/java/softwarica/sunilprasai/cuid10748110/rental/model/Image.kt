package softwarica.sunilprasai.cuid10748110.rental.model

import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.security.InvalidParameterException

private const val TAG = "Image"

class Image() {
    var id: String? = null
    var mimeType: String? = null
    var buffer: String? = null

    constructor(image: Image) : this() {
        id = image.id
        mimeType = image.mimeType
        buffer = image.buffer
    }

    constructor(jsonObject: JSONObject) : this() {
        try {
            id = if (jsonObject.has(ID) && !jsonObject.isNull(ID)) {
                jsonObject.getString(ID)
            } else throw InvalidParameterException(
                "Image must contain id parameter"
            )

            mimeType = if (jsonObject.has(MIME_TYPE) && !jsonObject.isNull(MIME_TYPE)) {
                jsonObject.getString(MIME_TYPE)
            } else throw InvalidParameterException(
                "Image must contain mimetype parameter"
            )

            buffer = if (jsonObject.has(BUFFER) && !jsonObject.isNull(BUFFER)) {
                jsonObject.getString(BUFFER)
            } else throw InvalidParameterException(
                "Image must contain buffer parameter"
            )
        } catch (e: JSONException) {

        }
    }

    companion object {
        @JvmStatic
        val ID = "_id"

        @JvmStatic
        val MIME_TYPE = "mimetype"

        @JvmStatic
        val BUFFER = "buffer"

        @JvmStatic
        fun getImagesListFromJSONArray(jsonArray: JSONArray): ArrayList<Image> {
            val imageArrayList: ArrayList<Image> = ArrayList()

            try {
                for (i in 0 until jsonArray.length()) {
                    imageArrayList.add(Image(jsonArray.getJSONObject(i)))
                }
            } catch (e: JSONException) {
                Log.e(TAG, "getImagesListFromJSONArray: ${e.localizedMessage}");
            }

            return imageArrayList
        }
    }
}